package com.android.server.display;

import android.os.SystemClock;
import android.os.SystemProperties;
import android.util.Slog;
import android.util.TimeUtils;
import android.util.Xml;
import com.android.server.input.HwCircleAnimation;
import com.android.server.jankshield.TableJankBd;
import com.android.server.jankshield.TableJankEvent;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.android.server.security.trustcircle.IOTController;
import com.android.server.wifipro.WifiProCommonDefs;
import com.android.server.wifipro.WifiProCommonUtils;
import huawei.com.android.server.policy.HwGlobalActionsData;
import huawei.com.android.server.policy.HwGlobalActionsView;
import huawei.com.android.server.policy.fingersense.CustomGestureDetector;
import huawei.cust.HwCfgFilePolicy;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class HwAmbientLuxFilterAlgo {
    private static final int AMBIENT_LIGHT_HORIZON = 20000;
    private static final int AMBIENT_MAX_LUX = 40000;
    private static final boolean DEBUG = false;
    private static final int EXTRA_DELAY_TIME = 100;
    private static final String HW_LABC_CONFIG_FILE = "LABCConfig.xml";
    private static final long POWER_ON_BRIGHTENING_LIGHT_DEBOUNCE = 500;
    private static final long POWER_ON_DARKENING_LIGHT_DEBOUNCE = 1000;
    private static final int PROXIMITY_NEGATIVE = 0;
    private static final int PROXIMITY_POSITIVE = 1;
    private static final int PROXIMITY_UNKNOWN = -1;
    private static final String TAG = "HwAmbientLuxFilterAlgo";
    private boolean mAllowLabcUseProximity;
    private HwRingBuffer mAmbientLightRingBuffer;
    private HwRingBuffer mAmbientLightRingBufferFilter;
    protected float mAmbientLux;
    public boolean mAutoBrightnessIntervened;
    private int mBrighenDebounceTime;
    private int mBrighenDebounceTimeForSmallThr;
    private float mBrightenDebounceTimeParaBig;
    private float mBrightenDeltaLuxMax;
    private float mBrightenDeltaLuxMin;
    private float mBrightenDeltaLuxPara;
    List<Point> mBrightenlinePoints;
    private String mConfigFilePath;
    private long mCoverModeBrightenResponseTime;
    private long mCoverModeDarkenResponseTime;
    private float mCoverModeFirstLux;
    private boolean mCoverState;
    private float mDarkTimeDelay;
    private boolean mDarkTimeDelayEnable;
    private float mDarkTimeDelayLuxThreshold;
    private int mDarkenDebounceTime;
    private int mDarkenDebounceTimeForSmallThr;
    private float mDarkenDebounceTimeParaBig;
    private float mDarkenDeltaLuxMax;
    private float mDarkenDeltaLuxMin;
    private float mDarkenDeltaLuxPara;
    List<Point> mDarkenlinePoints;
    private final int mDeviceActualBrightnessLevel;
    private boolean mFirstAmbientLux;
    private boolean mIsCoverModeFastResponseFlag;
    private boolean mIsclosed;
    private boolean mLastCloseScreenEnable;
    private float mLastCloseScreenLux;
    private float mLastObservedLux;
    private final int mLightSensorRate;
    private final Object mLock;
    private float mLuxBufferAvg;
    private float mLuxBufferAvgMax;
    private float mLuxBufferAvgMin;
    private boolean mNeedToSendProximityDebounceMsg;
    private boolean mNeedToUpdateBrightness;
    public long mNextTransitionTime;
    protected long mNormBrighenDebounceTime;
    protected long mNormBrighenDebounceTimeForSmallThr;
    protected long mNormDarkenDebounceTime;
    protected long mNormDarkenDebounceTimeForSmallThr;
    private int mPendingProximity;
    private long mPendingProximityDebounceTime;
    private int mPostMMAFilterNoFilterNum;
    private int mPostMMAFilterNum;
    private int mPowerOnFastResponseLuxNum;
    private boolean mPowerStatus;
    private long mPrintLogTime;
    private int mProximity;
    private int mProximityNegativeDebounceTime;
    private int mProximityPositiveDebounceTime;
    private boolean mProximityPositiveStatus;
    private float mRatioForBrightnenSmallThr;
    private float mRatioForDarkenSmallThr;
    private boolean mReportValueWhenSensorOnChange;
    private float mStability;
    private float mStabilityBrightenConstant;
    private float mStabilityBrightenConstantForSmallThr;
    private int mStabilityConstant;
    private float mStabilityDarkenConstant;
    private float mStabilityDarkenConstantForSmallThr;
    private float mStabilityForSmallThr;
    private int mStabilityTime1;
    private int mStabilityTime2;
    private int mlightSensorRateMills;

    public interface Callbacks {
        void updateBrightness();
    }

    private static class Point {
        float x;
        float y;

        public Point(float inx, float iny) {
            this.x = inx;
            this.y = iny;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.display.HwAmbientLuxFilterAlgo.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.display.HwAmbientLuxFilterAlgo.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.display.HwAmbientLuxFilterAlgo.<clinit>():void");
    }

    public HwAmbientLuxFilterAlgo(int lightSensorRate, int deviceActualBrightnessLevel) {
        this.mFirstAmbientLux = true;
        this.mStability = 0.0f;
        this.mStabilityForSmallThr = 0.0f;
        this.mLuxBufferAvg = 0.0f;
        this.mLuxBufferAvgMax = 0.0f;
        this.mLuxBufferAvgMin = 0.0f;
        this.mPowerStatus = DEBUG;
        this.mPrintLogTime = 0;
        this.mAutoBrightnessIntervened = DEBUG;
        this.mNextTransitionTime = -1;
        this.mBrighenDebounceTimeForSmallThr = 4000;
        this.mDarkenDebounceTimeForSmallThr = 8000;
        this.mConfigFilePath = null;
        this.mReportValueWhenSensorOnChange = true;
        this.mStabilityBrightenConstant = 101.0f;
        this.mStabilityDarkenConstant = 101.0f;
        this.mRatioForBrightnenSmallThr = HwCircleAnimation.SMALL_ALPHA;
        this.mRatioForDarkenSmallThr = HwCircleAnimation.SMALL_ALPHA;
        this.mDarkTimeDelayEnable = DEBUG;
        this.mDarkTimeDelay = 10000.0f;
        this.mDarkTimeDelayLuxThreshold = 50.0f;
        this.mCoverState = DEBUG;
        this.mIsclosed = DEBUG;
        this.mIsCoverModeFastResponseFlag = DEBUG;
        this.mCoverModeFirstLux = 2210.0f;
        this.mLastCloseScreenEnable = DEBUG;
        this.mLastCloseScreenLux = 0.0f;
        this.mCoverModeBrightenResponseTime = POWER_ON_DARKENING_LIGHT_DEBOUNCE;
        this.mCoverModeDarkenResponseTime = POWER_ON_DARKENING_LIGHT_DEBOUNCE;
        this.mPostMMAFilterNoFilterNum = 6;
        this.mPostMMAFilterNum = 5;
        this.mPowerOnFastResponseLuxNum = 8;
        this.mBrightenlinePoints = null;
        this.mDarkenlinePoints = null;
        this.mLock = new Object();
        this.mProximity = PROXIMITY_UNKNOWN;
        this.mPendingProximity = PROXIMITY_UNKNOWN;
        this.mAllowLabcUseProximity = DEBUG;
        this.mProximityPositiveDebounceTime = 150;
        this.mProximityNegativeDebounceTime = 3000;
        this.mPendingProximityDebounceTime = -1;
        this.mNeedToSendProximityDebounceMsg = DEBUG;
        this.mLightSensorRate = lightSensorRate;
        this.mDeviceActualBrightnessLevel = deviceActualBrightnessLevel;
        this.mNeedToUpdateBrightness = DEBUG;
        this.mAmbientLightRingBuffer = new HwRingBuffer(50);
        this.mAmbientLightRingBufferFilter = new HwRingBuffer(50);
        try {
            if (!getConfig()) {
                Slog.e(TAG, "getConfig failed! loadDefaultConfig");
                loadDefaultConfig();
            }
        } catch (Exception e) {
            e.printStackTrace();
            loadDefaultConfig();
        }
    }

    private boolean getConfig() throws IOException {
        FileNotFoundException e;
        IOException e2;
        Exception e3;
        Throwable th;
        String version = SystemProperties.get("ro.build.version.emui", null);
        if (version == null || version.length() == 0) {
            Slog.e(TAG, "get ro.build.version.emui failed!");
            return DEBUG;
        }
        String[] versionSplited = version.split("EmotionUI_");
        if (versionSplited.length < 2) {
            Slog.e(TAG, "split failed! version = " + version);
            return DEBUG;
        }
        String emuiVersion = versionSplited[PROXIMITY_POSITIVE];
        if (emuiVersion == null || emuiVersion.length() == 0) {
            Slog.e(TAG, "get emuiVersion failed!");
            return DEBUG;
        }
        File xmlFile = HwCfgFilePolicy.getCfgFile(String.format("/xml/lcd/%s/%s", new Object[]{emuiVersion, HW_LABC_CONFIG_FILE}), PROXIMITY_NEGATIVE);
        if (xmlFile == null) {
            Object[] objArr = new Object[PROXIMITY_POSITIVE];
            objArr[PROXIMITY_NEGATIVE] = HW_LABC_CONFIG_FILE;
            String xmlPath = String.format("/xml/lcd/%s", objArr);
            xmlFile = HwCfgFilePolicy.getCfgFile(xmlPath, PROXIMITY_NEGATIVE);
            if (xmlFile == null) {
                Slog.e(TAG, "get xmlFile :" + xmlPath + " failed!");
                return DEBUG;
            }
        }
        FileInputStream fileInputStream = null;
        try {
            FileInputStream inputStream = new FileInputStream(xmlFile);
            try {
                if (getConfigFromXML(inputStream)) {
                    if (checkConfigLoadedFromXML() && DEBUG) {
                        printConfigFromXML();
                    }
                    this.mConfigFilePath = xmlFile.getAbsolutePath();
                    if (DEBUG) {
                        Slog.i(TAG, "get xmlFile :" + this.mConfigFilePath);
                    }
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
                return DEBUG;
            } catch (FileNotFoundException e4) {
                e = e4;
                fileInputStream = inputStream;
                e.printStackTrace();
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                return DEBUG;
            } catch (IOException e5) {
                e2 = e5;
                fileInputStream = inputStream;
                e2.printStackTrace();
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                return DEBUG;
            } catch (Exception e6) {
                e3 = e6;
                fileInputStream = inputStream;
                try {
                    e3.printStackTrace();
                    if (fileInputStream != null) {
                        fileInputStream.close();
                    }
                    return DEBUG;
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
            return DEBUG;
        } catch (IOException e8) {
            e2 = e8;
            e2.printStackTrace();
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            return DEBUG;
        } catch (Exception e9) {
            e3 = e9;
            e3.printStackTrace();
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            return DEBUG;
        }
    }

    private boolean checkConfigLoadedFromXML() {
        if (this.mBrighenDebounceTime <= 0 || this.mDarkenDebounceTime <= 0) {
            loadDefaultConfig();
            Slog.e(TAG, "LoadXML false for DebounceTime <= 0, LoadDefaultConfig!");
            return DEBUG;
        } else if (this.mBrighenDebounceTimeForSmallThr <= 0 || this.mDarkenDebounceTimeForSmallThr <= 0) {
            loadDefaultConfig();
            Slog.e(TAG, "LoadXML false for DebounceTimeForSmallThr <= 0, LoadDefaultConfig!");
            return DEBUG;
        } else if (this.mRatioForBrightnenSmallThr <= 0.0f || this.mRatioForDarkenSmallThr <= 0.0f) {
            loadDefaultConfig();
            Slog.e(TAG, "LoadXML false for Ratio <= 0, LoadDefaultConfig!");
            return DEBUG;
        } else if (!checkPointsListIsOK(this.mBrightenlinePoints)) {
            loadDefaultConfig();
            Slog.e(TAG, "checkPointsList mBrightenlinePoints is wrong, LoadDefaultConfig!");
            return DEBUG;
        } else if (!checkPointsListIsOK(this.mDarkenlinePoints)) {
            loadDefaultConfig();
            Slog.e(TAG, "checkPointsList mDarkenlinePoints is wrong, LoadDefaultConfig!");
            return DEBUG;
        } else if (this.mCoverModeBrightenResponseTime < 0 || this.mCoverModeDarkenResponseTime < 0) {
            loadDefaultConfig();
            Slog.e(TAG, "error coverModeResponseTime: BrightenResponseTime=" + this.mCoverModeBrightenResponseTime + ",DarkenResponseTime=" + this.mCoverModeDarkenResponseTime);
            return DEBUG;
        } else if (this.mPostMMAFilterNum <= 0 || this.mPostMMAFilterNoFilterNum < this.mPostMMAFilterNum) {
            loadDefaultConfig();
            Slog.e(TAG, "error filterPara: PostMMVFilterNoFilterNum=" + this.mPostMMAFilterNoFilterNum + ",PostMMVFilterNum=" + this.mPostMMAFilterNum);
            return DEBUG;
        } else if (this.mPowerOnFastResponseLuxNum <= 0) {
            loadDefaultConfig();
            Slog.e(TAG, "error filterPara: mPowerOnFastResponseLuxNum=" + this.mPowerOnFastResponseLuxNum);
            return DEBUG;
        } else {
            if (DEBUG) {
                Slog.i(TAG, "checkConfigLoadedFromXML success!");
            }
            return true;
        }
    }

    private boolean checkPointsListIsOK(List<Point> LinePointsList) {
        List<Point> mLinePointsList = LinePointsList;
        if (LinePointsList == null) {
            Slog.e(TAG, "LoadXML false for mLinePointsList == null");
            return DEBUG;
        } else if (LinePointsList.size() <= 2 || LinePointsList.size() >= EXTRA_DELAY_TIME) {
            Slog.e(TAG, "LoadXML false for mLinePointsList number is wrong");
            return DEBUG;
        } else {
            int mDrkenNum = PROXIMITY_NEGATIVE;
            Point lastPoint = null;
            for (Point tmpPoint : LinePointsList) {
                if (mDrkenNum == 0) {
                    lastPoint = tmpPoint;
                } else if (lastPoint == null || lastPoint.x < tmpPoint.x) {
                    lastPoint = tmpPoint;
                } else {
                    loadDefaultConfig();
                    Slog.e(TAG, "LoadXML false for mLinePointsList is wrong");
                    return DEBUG;
                }
                mDrkenNum += PROXIMITY_POSITIVE;
            }
            return true;
        }
    }

    private void loadDefaultConfig() {
        this.mlightSensorRateMills = HwGlobalActionsView.VIBRATE_DELAY;
        this.mBrighenDebounceTime = IOTController.TYPE_MASTER;
        this.mDarkenDebounceTime = 4000;
        this.mBrighenDebounceTimeForSmallThr = 4000;
        this.mDarkenDebounceTimeForSmallThr = 8000;
        this.mRatioForBrightnenSmallThr = HwCircleAnimation.SMALL_ALPHA;
        this.mRatioForDarkenSmallThr = HwCircleAnimation.SMALL_ALPHA;
        this.mBrightenDebounceTimeParaBig = 0.0f;
        this.mDarkenDebounceTimeParaBig = HwCircleAnimation.SMALL_ALPHA;
        this.mBrightenDeltaLuxPara = 0.0f;
        this.mDarkenDeltaLuxPara = HwCircleAnimation.SMALL_ALPHA;
        this.mStabilityConstant = 5;
        this.mStabilityTime1 = 20;
        this.mStabilityTime2 = 10;
        this.mDarkTimeDelayEnable = DEBUG;
        this.mDarkTimeDelay = 10000.0f;
        this.mDarkTimeDelayLuxThreshold = 50.0f;
        this.mCoverModeBrightenResponseTime = POWER_ON_DARKENING_LIGHT_DEBOUNCE;
        this.mCoverModeDarkenResponseTime = POWER_ON_DARKENING_LIGHT_DEBOUNCE;
        this.mPostMMAFilterNoFilterNum = 6;
        this.mPostMMAFilterNum = 5;
        this.mPowerOnFastResponseLuxNum = 8;
        if (this.mBrightenlinePoints != null) {
            this.mBrightenlinePoints.clear();
        } else {
            this.mBrightenlinePoints = new ArrayList();
        }
        this.mBrightenlinePoints.add(new Point(0.0f, 15.0f));
        this.mBrightenlinePoints.add(new Point(2.0f, 15.0f));
        this.mBrightenlinePoints.add(new Point(10.0f, 19.0f));
        this.mBrightenlinePoints.add(new Point(20.0f, 219.0f));
        this.mBrightenlinePoints.add(new Point(100.0f, 539.0f));
        this.mBrightenlinePoints.add(new Point(1000.0f, 989.0f));
        this.mBrightenlinePoints.add(new Point(40000.0f, 989.0f));
        if (this.mDarkenlinePoints != null) {
            this.mDarkenlinePoints.clear();
        } else {
            this.mDarkenlinePoints = new ArrayList();
        }
        this.mDarkenlinePoints.add(new Point(0.0f, HwCircleAnimation.SMALL_ALPHA));
        this.mDarkenlinePoints.add(new Point(HwCircleAnimation.SMALL_ALPHA, HwCircleAnimation.SMALL_ALPHA));
        this.mDarkenlinePoints.add(new Point(20.0f, 20.0f));
        this.mDarkenlinePoints.add(new Point(40.0f, 20.0f));
        this.mDarkenlinePoints.add(new Point(100.0f, 80.0f));
        this.mDarkenlinePoints.add(new Point(600.0f, 580.0f));
        this.mDarkenlinePoints.add(new Point(1180.0f, 580.0f));
        this.mDarkenlinePoints.add(new Point(1200.0f, 600.0f));
        this.mDarkenlinePoints.add(new Point(1800.0f, 600.0f));
        this.mDarkenlinePoints.add(new Point(40000.0f, 38800.0f));
        if (DEBUG) {
            printConfigFromXML();
        }
    }

    private void printConfigFromXML() {
        Slog.i(TAG, "LoadXMLConfig_lightSensorRateMills=" + this.mlightSensorRateMills + ",BrighenDebounceTime=" + this.mBrighenDebounceTime + ",DarkenDebounceTime=" + this.mDarkenDebounceTime + ",mBrightenDebounceTimeParaBig=" + this.mBrightenDebounceTimeParaBig + ",mDarkenDebounceTimeParaBig=" + this.mDarkenDebounceTimeParaBig + ",mBrightenDeltaLuxPara=" + this.mBrightenDeltaLuxPara + ",mDarkenDeltaLuxPara=" + this.mDarkenDeltaLuxPara + ",mStabilityConstant=" + this.mStabilityConstant + ",mStabilityTime1=" + this.mStabilityTime1 + ",mStabilityTime2=" + this.mStabilityTime2 + ",mAllowLabcUseProximity=" + this.mAllowLabcUseProximity + ",mProximityPositiveDebounceTime=" + this.mProximityPositiveDebounceTime + ",mProximityNegativeDebounceTime=" + this.mProximityNegativeDebounceTime);
        Slog.i(TAG, "BTForSmallThr=" + this.mBrighenDebounceTimeForSmallThr + ",DTForSmallThr=" + this.mDarkenDebounceTimeForSmallThr + ",RatioForBSmallThr=" + this.mRatioForBrightnenSmallThr + ",RatioForDSmallThr=" + this.mRatioForDarkenSmallThr);
        Slog.i(TAG, "DarkTimeDelayEnable=" + this.mDarkTimeDelayEnable + ",DarkTimeDelay=" + this.mDarkTimeDelay + ",DarkTimeDelayLuxThreshold=" + this.mDarkTimeDelayLuxThreshold + ",mCoverModeFirstLux=" + this.mCoverModeFirstLux + ",CoverModeDarkenResponseTime=" + this.mCoverModeDarkenResponseTime + ",CoverModeBrightenResponseTime=" + this.mCoverModeBrightenResponseTime + ",mPowerOnFastResponseLuxNum=" + this.mPowerOnFastResponseLuxNum);
        Slog.i(TAG, "PostMMVFilterNoFilterNum=" + this.mPostMMAFilterNoFilterNum + ",PostMMVFilterNum=" + this.mPostMMAFilterNum);
        for (Point temp : this.mBrightenlinePoints) {
            Slog.i(TAG, "LoadXMLConfig_BrightenlinePoints x = " + temp.x + ", y = " + temp.y);
        }
        for (Point temp2 : this.mDarkenlinePoints) {
            Slog.i(TAG, "LoadXMLConfig_DarkenlinePoints x = " + temp2.x + ", y = " + temp2.y);
        }
    }

    private boolean getConfigFromXML(InputStream inStream) {
        if (DEBUG) {
            Slog.i(TAG, "getConfigFromeXML");
        }
        boolean lightSensorRateMillsLoaded = DEBUG;
        boolean BrighenDebounceTimeLoaded = DEBUG;
        boolean DarkenDebounceTimeLoaded = DEBUG;
        boolean BrightenDebounceTimeParaBigLoaded = DEBUG;
        boolean DarkenDebounceTimeParaBigLoaded = DEBUG;
        boolean BrightenDeltaLuxParaLoaded = DEBUG;
        boolean DarkenDeltaLuxParaLoaded = DEBUG;
        boolean StabilityConstantLoaded = DEBUG;
        boolean StabilityTime1Loaded = DEBUG;
        boolean StabilityTime2Loaded = DEBUG;
        boolean BrightenlinePointsLoadStarted = DEBUG;
        boolean BrightenlinePointsLoaded = DEBUG;
        boolean DarkenlinePointsLoadStarted = DEBUG;
        boolean DarkenlinePointsLoaded = DEBUG;
        boolean configGroupLoadStarted = DEBUG;
        boolean loadFinished = DEBUG;
        XmlPullParser parser = Xml.newPullParser();
        try {
            List<Boolean> xmlLoadedList;
            parser.setInput(inStream, "UTF-8");
            for (int eventType = parser.getEventType(); eventType != PROXIMITY_POSITIVE; eventType = parser.next()) {
                String name;
                switch (eventType) {
                    case HwGlobalActionsData.FLAG_AIRPLANEMODE_OFF /*2*/:
                        name = parser.getName();
                        if (!name.equals("LABCConfig")) {
                            if (configGroupLoadStarted) {
                                if (!name.equals("lightSensorRateMills")) {
                                    if (!name.equals("BrighenDebounceTime")) {
                                        if (!name.equals("DarkenDebounceTime")) {
                                            if (!name.equals("BrightenDebounceTimeParaBig")) {
                                                if (!name.equals("DarkenDebounceTimeParaBig")) {
                                                    if (!name.equals("BrightenDeltaLuxPara")) {
                                                        if (!name.equals("DarkenDeltaLuxPara")) {
                                                            if (!name.equals("StabilityConstant")) {
                                                                if (!name.equals("StabilityTime1")) {
                                                                    if (!name.equals("StabilityTime2")) {
                                                                        if (!name.equals("BrighenDebounceTimeForSmallThr")) {
                                                                            if (!name.equals("DarkenDebounceTimeForSmallThr")) {
                                                                                if (!name.equals("RatioForBrightnenSmallThr")) {
                                                                                    if (!name.equals("RatioForDarkenSmallThr")) {
                                                                                        if (!name.equals("DarkTimeDelayEnable")) {
                                                                                            if (!name.equals("DarkTimeDelay")) {
                                                                                                if (!name.equals("DarkTimeDelayLuxThreshold")) {
                                                                                                    if (!name.equals("CoverModeFirstLux")) {
                                                                                                        if (!name.equals("LastCloseScreenEnable")) {
                                                                                                            if (!name.equals("CoverModeBrightenResponseTime")) {
                                                                                                                if (!name.equals("CoverModeDarkenResponseTime")) {
                                                                                                                    if (!name.equals("PostMMVFilterNoFilterNum")) {
                                                                                                                        if (!name.equals("PostMMVFilterNum")) {
                                                                                                                            if (!name.equals("PowerOnFastResponseLuxNum")) {
                                                                                                                                if (!name.equals("BrightenlinePoints")) {
                                                                                                                                    Point currentPoint;
                                                                                                                                    String s;
                                                                                                                                    if (!name.equals("Point") || !BrightenlinePointsLoadStarted) {
                                                                                                                                        if (!name.equals("DarkenlinePoints")) {
                                                                                                                                            if (!name.equals("Point") || !DarkenlinePointsLoadStarted) {
                                                                                                                                                if (!name.equals("ReportValueWhenSensorOnChange")) {
                                                                                                                                                    if (!name.equals("AllowLabcUseProximity")) {
                                                                                                                                                        if (!name.equals("ProximityPositiveDebounceTime")) {
                                                                                                                                                            if (name.equals("ProximityNegativeDebounceTime")) {
                                                                                                                                                                this.mProximityNegativeDebounceTime = Integer.parseInt(parser.nextText());
                                                                                                                                                                break;
                                                                                                                                                            }
                                                                                                                                                        }
                                                                                                                                                        this.mProximityPositiveDebounceTime = Integer.parseInt(parser.nextText());
                                                                                                                                                        break;
                                                                                                                                                    }
                                                                                                                                                    this.mAllowLabcUseProximity = Boolean.parseBoolean(parser.nextText());
                                                                                                                                                    break;
                                                                                                                                                }
                                                                                                                                                this.mReportValueWhenSensorOnChange = Boolean.parseBoolean(parser.nextText());
                                                                                                                                                break;
                                                                                                                                            }
                                                                                                                                            currentPoint = new Point();
                                                                                                                                            s = parser.nextText();
                                                                                                                                            currentPoint.x = Float.parseFloat(s.split(",")[PROXIMITY_NEGATIVE]);
                                                                                                                                            currentPoint.y = Float.parseFloat(s.split(",")[PROXIMITY_POSITIVE]);
                                                                                                                                            if (this.mDarkenlinePoints == null) {
                                                                                                                                                this.mDarkenlinePoints = new ArrayList();
                                                                                                                                            }
                                                                                                                                            this.mDarkenlinePoints.add(currentPoint);
                                                                                                                                            break;
                                                                                                                                        }
                                                                                                                                        DarkenlinePointsLoadStarted = true;
                                                                                                                                        break;
                                                                                                                                    }
                                                                                                                                    currentPoint = new Point();
                                                                                                                                    s = parser.nextText();
                                                                                                                                    currentPoint.x = Float.parseFloat(s.split(",")[PROXIMITY_NEGATIVE]);
                                                                                                                                    currentPoint.y = Float.parseFloat(s.split(",")[PROXIMITY_POSITIVE]);
                                                                                                                                    if (this.mBrightenlinePoints == null) {
                                                                                                                                        this.mBrightenlinePoints = new ArrayList();
                                                                                                                                    }
                                                                                                                                    this.mBrightenlinePoints.add(currentPoint);
                                                                                                                                    break;
                                                                                                                                }
                                                                                                                                BrightenlinePointsLoadStarted = true;
                                                                                                                                break;
                                                                                                                            }
                                                                                                                            this.mPowerOnFastResponseLuxNum = Integer.parseInt(parser.nextText());
                                                                                                                            break;
                                                                                                                        }
                                                                                                                        this.mPostMMAFilterNum = Integer.parseInt(parser.nextText());
                                                                                                                        break;
                                                                                                                    }
                                                                                                                    this.mPostMMAFilterNoFilterNum = Integer.parseInt(parser.nextText());
                                                                                                                    break;
                                                                                                                }
                                                                                                                this.mCoverModeDarkenResponseTime = Long.parseLong(parser.nextText());
                                                                                                                break;
                                                                                                            }
                                                                                                            this.mCoverModeBrightenResponseTime = Long.parseLong(parser.nextText());
                                                                                                            break;
                                                                                                        }
                                                                                                        this.mLastCloseScreenEnable = Boolean.parseBoolean(parser.nextText());
                                                                                                        break;
                                                                                                    }
                                                                                                    this.mCoverModeFirstLux = Float.parseFloat(parser.nextText());
                                                                                                    break;
                                                                                                }
                                                                                                this.mDarkTimeDelayLuxThreshold = Float.parseFloat(parser.nextText());
                                                                                                break;
                                                                                            }
                                                                                            this.mDarkTimeDelay = Float.parseFloat(parser.nextText());
                                                                                            break;
                                                                                        }
                                                                                        this.mDarkTimeDelayEnable = Boolean.parseBoolean(parser.nextText());
                                                                                        break;
                                                                                    }
                                                                                    this.mRatioForDarkenSmallThr = Float.parseFloat(parser.nextText());
                                                                                    break;
                                                                                }
                                                                                this.mRatioForBrightnenSmallThr = Float.parseFloat(parser.nextText());
                                                                                break;
                                                                            }
                                                                            this.mDarkenDebounceTimeForSmallThr = Integer.parseInt(parser.nextText());
                                                                            break;
                                                                        }
                                                                        this.mBrighenDebounceTimeForSmallThr = Integer.parseInt(parser.nextText());
                                                                        break;
                                                                    }
                                                                    this.mStabilityTime2 = Integer.parseInt(parser.nextText());
                                                                    StabilityTime2Loaded = true;
                                                                    break;
                                                                }
                                                                this.mStabilityTime1 = Integer.parseInt(parser.nextText());
                                                                StabilityTime1Loaded = true;
                                                                break;
                                                            }
                                                            this.mStabilityConstant = Integer.parseInt(parser.nextText());
                                                            StabilityConstantLoaded = true;
                                                            break;
                                                        }
                                                        this.mDarkenDeltaLuxPara = Float.parseFloat(parser.nextText());
                                                        DarkenDeltaLuxParaLoaded = true;
                                                        break;
                                                    }
                                                    this.mBrightenDeltaLuxPara = Float.parseFloat(parser.nextText());
                                                    BrightenDeltaLuxParaLoaded = true;
                                                    break;
                                                }
                                                this.mDarkenDebounceTimeParaBig = Float.parseFloat(parser.nextText());
                                                DarkenDebounceTimeParaBigLoaded = true;
                                                break;
                                            }
                                            this.mBrightenDebounceTimeParaBig = Float.parseFloat(parser.nextText());
                                            BrightenDebounceTimeParaBigLoaded = true;
                                            break;
                                        }
                                        this.mDarkenDebounceTime = Integer.parseInt(parser.nextText());
                                        DarkenDebounceTimeLoaded = true;
                                        break;
                                    }
                                    this.mBrighenDebounceTime = Integer.parseInt(parser.nextText());
                                    BrighenDebounceTimeLoaded = true;
                                    break;
                                }
                                this.mlightSensorRateMills = Integer.parseInt(parser.nextText());
                                lightSensorRateMillsLoaded = true;
                                break;
                            }
                        } else if (this.mDeviceActualBrightnessLevel != 0) {
                            configGroupLoadStarted = checkDeviceLevelString(parser.getAttributeValue(null, MemoryConstant.MEM_FILECACHE_ITEM_LEVEL));
                            break;
                        } else {
                            configGroupLoadStarted = true;
                            break;
                        }
                        break;
                    case WifiProCommonDefs.WIFI_SECURITY_PHISHING_FAILED /*3*/:
                        name = parser.getName();
                        if (name.equals("LABCConfig") && configGroupLoadStarted) {
                            loadFinished = true;
                            break;
                        } else if (configGroupLoadStarted) {
                            if (name.equals("BrightenlinePoints")) {
                                BrightenlinePointsLoadStarted = DEBUG;
                                if (this.mBrightenlinePoints != null) {
                                    BrightenlinePointsLoaded = true;
                                    break;
                                }
                                Slog.e(TAG, "no BrightenlinePoints loaded!");
                                return DEBUG;
                            }
                            if (name.equals("DarkenlinePoints")) {
                                DarkenlinePointsLoadStarted = DEBUG;
                                if (this.mDarkenlinePoints != null) {
                                    DarkenlinePointsLoaded = true;
                                    break;
                                }
                                Slog.e(TAG, "no DarkenlinePoints loaded!");
                                return DEBUG;
                            }
                        }
                        break;
                }
                if (loadFinished) {
                    xmlLoadedList = new ArrayList();
                    xmlLoadedList.add(Boolean.valueOf(lightSensorRateMillsLoaded));
                    xmlLoadedList.add(Boolean.valueOf(BrighenDebounceTimeLoaded));
                    xmlLoadedList.add(Boolean.valueOf(DarkenDebounceTimeLoaded));
                    xmlLoadedList.add(Boolean.valueOf(BrightenDebounceTimeParaBigLoaded));
                    xmlLoadedList.add(Boolean.valueOf(DarkenDebounceTimeParaBigLoaded));
                    xmlLoadedList.add(Boolean.valueOf(BrightenDeltaLuxParaLoaded));
                    xmlLoadedList.add(Boolean.valueOf(DarkenDeltaLuxParaLoaded));
                    xmlLoadedList.add(Boolean.valueOf(StabilityConstantLoaded));
                    xmlLoadedList.add(Boolean.valueOf(StabilityTime1Loaded));
                    xmlLoadedList.add(Boolean.valueOf(StabilityTime2Loaded));
                    xmlLoadedList.add(Boolean.valueOf(DarkenlinePointsLoaded));
                    xmlLoadedList.add(Boolean.valueOf(BrightenlinePointsLoaded));
                    if (checkXmlLoadedList(xmlLoadedList)) {
                        if (!configGroupLoadStarted) {
                            Slog.e(TAG, "actualDeviceLevel = " + this.mDeviceActualBrightnessLevel + ", can't find matched level in XML, load failed!");
                            return DEBUG;
                        }
                        Slog.e(TAG, "getConfigFromeXML false!");
                        return DEBUG;
                    }
                    if (DEBUG) {
                        Slog.i(TAG, "getConfigFromeXML success!");
                    }
                    return true;
                }
            }
            xmlLoadedList = new ArrayList();
            xmlLoadedList.add(Boolean.valueOf(lightSensorRateMillsLoaded));
            xmlLoadedList.add(Boolean.valueOf(BrighenDebounceTimeLoaded));
            xmlLoadedList.add(Boolean.valueOf(DarkenDebounceTimeLoaded));
            xmlLoadedList.add(Boolean.valueOf(BrightenDebounceTimeParaBigLoaded));
            xmlLoadedList.add(Boolean.valueOf(DarkenDebounceTimeParaBigLoaded));
            xmlLoadedList.add(Boolean.valueOf(BrightenDeltaLuxParaLoaded));
            xmlLoadedList.add(Boolean.valueOf(DarkenDeltaLuxParaLoaded));
            xmlLoadedList.add(Boolean.valueOf(StabilityConstantLoaded));
            xmlLoadedList.add(Boolean.valueOf(StabilityTime1Loaded));
            xmlLoadedList.add(Boolean.valueOf(StabilityTime2Loaded));
            xmlLoadedList.add(Boolean.valueOf(DarkenlinePointsLoaded));
            xmlLoadedList.add(Boolean.valueOf(BrightenlinePointsLoaded));
            if (checkXmlLoadedList(xmlLoadedList)) {
                if (configGroupLoadStarted) {
                    Slog.e(TAG, "actualDeviceLevel = " + this.mDeviceActualBrightnessLevel + ", can't find matched level in XML, load failed!");
                    return DEBUG;
                }
                Slog.e(TAG, "getConfigFromeXML false!");
                return DEBUG;
            }
            if (DEBUG) {
                Slog.i(TAG, "getConfigFromeXML success!");
            }
            return true;
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        } catch (IOException e2) {
            e2.printStackTrace();
        } catch (NumberFormatException e3) {
            e3.printStackTrace();
        } catch (Exception e4) {
            e4.printStackTrace();
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean checkDeviceLevelString(String deviceLevelString) {
        return (deviceLevelString == null || deviceLevelString.length() == 0 || Integer.parseInt(deviceLevelString) == this.mDeviceActualBrightnessLevel) ? true : DEBUG;
    }

    private boolean checkXmlLoadedList(List<Boolean> list) {
        if (list == null) {
            return DEBUG;
        }
        for (Boolean booleanValue : list) {
            if (!booleanValue.booleanValue()) {
                return DEBUG;
            }
        }
        return true;
    }

    public void isFirstAmbientLux(boolean isFirst) {
        this.mFirstAmbientLux = isFirst;
    }

    public void handleLightSensorEvent(long time, float lux) {
        synchronized (this.mLock) {
            if (lux > 40000.0f) {
                if (DEBUG) {
                    Slog.i(TAG, "lux >= max, lux=" + lux);
                }
                lux = 40000.0f;
            }
            try {
                applyLightSensorMeasurement(time, lux);
                updateAmbientLux(time);
            } catch (ArrayIndexOutOfBoundsException e) {
                e.printStackTrace();
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
    }

    private void applyLightSensorMeasurement(long time, float lux) {
        this.mAmbientLightRingBuffer.prune(time - TableJankBd.recordMAXCOUNT);
        this.mAmbientLightRingBuffer.push(time, lux);
        this.mLastObservedLux = lux;
    }

    public float getCurrentAmbientLux() {
        return this.mAmbientLux;
    }

    private void setAmbientLux(float lux) {
        this.mAmbientLux = (float) Math.round(lux);
        if (this.mAmbientLux < 10.0f) {
            this.mStabilityBrightenConstantForSmallThr = WifiProCommonUtils.RECOVERY_PERCENTAGE;
            this.mStabilityDarkenConstantForSmallThr = WifiProCommonUtils.RECOVERY_PERCENTAGE;
        } else if (this.mAmbientLux < 10.0f || this.mAmbientLux >= 50.0f) {
            this.mStabilityBrightenConstantForSmallThr = 5.0f;
            this.mStabilityDarkenConstantForSmallThr = 5.0f;
        } else {
            this.mStabilityBrightenConstantForSmallThr = CustomGestureDetector.TOUCH_TOLERANCE;
            this.mStabilityDarkenConstantForSmallThr = CustomGestureDetector.TOUCH_TOLERANCE;
        }
        updatepara(this.mAmbientLightRingBuffer, this.mAmbientLux);
    }

    public void updateAmbientLux() {
        synchronized (this.mLock) {
            long time = SystemClock.uptimeMillis();
            try {
                this.mAmbientLightRingBuffer.push(time, this.mLastObservedLux);
                this.mAmbientLightRingBuffer.prune(time - TableJankBd.recordMAXCOUNT);
                if (DEBUG) {
                    Slog.d(TAG, "updateAmbientLux:time=" + time + ",mLastObservedLux=" + this.mLastObservedLux);
                }
                updateAmbientLux(time);
            } catch (ArrayIndexOutOfBoundsException e) {
                e.printStackTrace();
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        }
    }

    private void updateAmbientLux(long time) {
        float ambientLux = calculateAmbientLux(time);
        updateBuffer(time, ambientLux, AMBIENT_LIGHT_HORIZON);
        if (this.mFirstAmbientLux) {
            if (this.mCoverState) {
                this.mCoverState = DEBUG;
                if (!this.mPowerStatus) {
                    if (this.mLastCloseScreenEnable) {
                        ambientLux = this.mLastCloseScreenLux;
                    } else {
                        ambientLux = this.mCoverModeFirstLux;
                    }
                    this.mAmbientLightRingBuffer.putLux(PROXIMITY_NEGATIVE, ambientLux);
                    if (DEBUG) {
                        Slog.i(TAG, "LabcCoverMode ambientLux=" + ambientLux + ",mCoverState=" + this.mCoverState + ",mPowerStatus=" + this.mPowerStatus);
                    }
                }
            }
            setAmbientLux(ambientLux);
            this.mFirstAmbientLux = DEBUG;
            if (DEBUG) {
                Slog.d(TAG, "updateAmbientLux: Initializing: mAmbientLightRingBuffer=" + this.mAmbientLightRingBuffer + ", mAmbientLux=" + this.mAmbientLux + ",mLastCloseScreenLux=" + this.mLastCloseScreenLux);
            }
            this.mNeedToUpdateBrightness = true;
        }
        updatepara(this.mAmbientLightRingBuffer, this.mAmbientLux);
        long nextBrightenTransition = nextAmbientLightBrighteningTransition(time);
        long nextDarkenTransition = nextAmbientLightDarkeningTransition(time);
        boolean needToBrighten = decideToBrighten(ambientLux);
        boolean needToDarken = decideToDarken(ambientLux);
        long nextBrightenTransitionForSmallThr = nextAmbientLightBrighteningTransitionForSmallThr(time);
        long nextDarkenTransitionForSmallThr = nextAmbientLightDarkeningTransitionForSmallThr(time);
        boolean needToBrightenForSmallThr = decideToBrightenForSmallThr(ambientLux);
        boolean needToDarkenForSmallThr = decideToDarkenForSmallThr(ambientLux);
        needToBrightenForSmallThr = (!needToBrightenForSmallThr || nextBrightenTransitionForSmallThr > time) ? DEBUG : true;
        needToDarkenForSmallThr = (!needToDarkenForSmallThr || nextDarkenTransitionForSmallThr > time) ? DEBUG : true;
        needToBrighten = (!needToBrighten || nextBrightenTransition > time) ? needToBrightenForSmallThr : true;
        needToDarken = (!needToDarken || nextDarkenTransition > time) ? needToDarkenForSmallThr : true;
        float brightenLux = this.mAmbientLux + this.mBrightenDeltaLuxMax;
        float darkenLux = this.mAmbientLux - this.mDarkenDeltaLuxMax;
        float brightenLuxForSmallThr = this.mAmbientLux + this.mBrightenDeltaLuxMin;
        float darkenLuxForSmallThr = this.mAmbientLux - this.mDarkenDeltaLuxMin;
        if (DEBUG) {
            if (time - this.mPrintLogTime > TableJankEvent.recMAXCOUNT) {
                Slog.d(TAG, "t=" + time + ",BT=" + nextBrightenTransition + ",DT=" + nextDarkenTransition + ",BT1=" + nextBrightenTransitionForSmallThr + ",DT1=" + nextDarkenTransitionForSmallThr + ",Bn=" + needToBrighten + ",Dn=" + needToDarken + ",lx=" + this.mAmbientLightRingBuffer.toString(6) + ",mLx=" + this.mAmbientLux + ",s=" + this.mStability + ",ss=" + this.mStabilityForSmallThr + ",AuIntervened=" + this.mAutoBrightnessIntervened + ",mProximityState=" + this.mProximityPositiveStatus + ",bLux=" + brightenLux + ",dLux=" + darkenLux + ",bLux1=" + brightenLuxForSmallThr + ",dLux1=" + darkenLuxForSmallThr + ",mDt=" + this.mNormDarkenDebounceTime);
                this.mPrintLogTime = time;
            }
        }
        if ((needToBrighten | needToDarken) != 0) {
            setAmbientLux(ambientLux);
            if (DEBUG) {
                String str = TAG;
                StringBuilder append = new StringBuilder().append("updateAmbientLux: ");
                String str2 = needToBrighten ? "Brightened" : "Darkened";
                String hwRingBuffer = this.mAmbientLightRingBuffer.toString(6);
                hwRingBuffer = ",needBs=";
                hwRingBuffer = ",needDs=";
                hwRingBuffer = str;
                Slog.d(hwRingBuffer, append.append(str2).append(", mAmbientLightRingBuffer=").append(r23).append(", mAmbientLux=").append(this.mAmbientLux).append(",s=").append(this.mStability).append(",ss=").append(this.mStabilityForSmallThr).append(r23).append(needToBrightenForSmallThr).append(r23).append(needToDarkenForSmallThr).toString());
            }
            if (DEBUG && this.mIsCoverModeFastResponseFlag) {
                Slog.i(TAG, "CoverModeBResponseTime=" + this.mCoverModeBrightenResponseTime + ",CoverModeDResponseTime=" + this.mCoverModeDarkenResponseTime);
            }
            if (DEBUG && this.mPowerStatus) {
                Slog.i(TAG, "PowerOnBT=500,PowerOnDT=1000");
            }
            this.mNeedToUpdateBrightness = true;
            nextBrightenTransition = nextAmbientLightBrighteningTransition(time);
            nextDarkenTransition = nextAmbientLightDarkeningTransition(time);
        }
        this.mNextTransitionTime = Math.min(nextDarkenTransition, nextBrightenTransition);
        this.mNextTransitionTime = this.mNextTransitionTime > time ? (this.mNextTransitionTime + ((long) this.mLightSensorRate)) + 100 : (((long) this.mLightSensorRate) + time) + 100;
        if (DEBUG) {
            if (time - this.mPrintLogTime > TableJankEvent.recMAXCOUNT) {
                Slog.d(TAG, "updateAmbientLux: Scheduling ambient lux update for " + this.mNextTransitionTime + TimeUtils.formatUptime(this.mNextTransitionTime));
            }
        }
    }

    public boolean needToUpdateBrightness() {
        return this.mNeedToUpdateBrightness;
    }

    public boolean brightnessUpdated() {
        this.mNeedToUpdateBrightness = DEBUG;
        return DEBUG;
    }

    public boolean needToSendUpdateAmbientLuxMsg() {
        return this.mNextTransitionTime > 0 ? true : DEBUG;
    }

    public long getSendUpdateAmbientLuxMsgTime() {
        return this.mNextTransitionTime;
    }

    private long getNextAmbientLightBrighteningTime(long earliedtime) {
        if (this.mIsCoverModeFastResponseFlag) {
            return this.mCoverModeBrightenResponseTime + earliedtime;
        }
        if (this.mPowerStatus) {
            return POWER_ON_BRIGHTENING_LIGHT_DEBOUNCE + earliedtime;
        }
        return this.mNormBrighenDebounceTime + earliedtime;
    }

    private long getNextAmbientLightDarkeningTime(long earliedtime) {
        if (this.mIsCoverModeFastResponseFlag) {
            return this.mCoverModeDarkenResponseTime + earliedtime;
        }
        if (this.mPowerStatus) {
            return POWER_ON_DARKENING_LIGHT_DEBOUNCE + earliedtime;
        }
        return this.mNormDarkenDebounceTime + earliedtime;
    }

    public void setPowerStatus(boolean powerStatus) {
        this.mPowerStatus = powerStatus;
    }

    public void clear() {
        synchronized (this.mLock) {
            if (DEBUG) {
                Slog.d(TAG, "clear buffer data and algo flags");
            }
            this.mLastCloseScreenLux = this.mAmbientLux;
            if (DEBUG) {
                Slog.d(TAG, "LabcCoverMode clear: mLastCloseScreenLux=" + this.mLastCloseScreenLux);
            }
            this.mIsCoverModeFastResponseFlag = DEBUG;
            this.mAutoBrightnessIntervened = DEBUG;
            this.mAmbientLightRingBuffer.clear();
            this.mAmbientLightRingBufferFilter.clear();
        }
    }

    private void updateBuffer(long time, float ambientLux, int horizon) {
        this.mAmbientLightRingBufferFilter.push(time, ambientLux);
        this.mAmbientLightRingBufferFilter.prune(time - ((long) horizon));
    }

    private void updatepara(HwRingBuffer buffer, float lux) {
        float stability = calculateStability(buffer);
        float stabilityForSmallThr = calculateStabilityForSmallThr(buffer);
        if (stability > 100.0f) {
            this.mStability = 100.0f;
        } else if (stability < ((float) this.mStabilityConstant)) {
            this.mStability = (float) this.mStabilityConstant;
        } else {
            this.mStability = stability;
        }
        if (stabilityForSmallThr > 100.0f) {
            this.mStabilityForSmallThr = 100.0f;
        } else {
            this.mStabilityForSmallThr = stabilityForSmallThr;
        }
        float mLux = (float) Math.round(lux);
        this.mNormBrighenDebounceTime = (long) (((float) this.mBrighenDebounceTime) * (((this.mBrightenDebounceTimeParaBig * (this.mStability - ((float) this.mStabilityConstant))) / 100.0f) + HwCircleAnimation.SMALL_ALPHA));
        if (mLux >= this.mDarkTimeDelayLuxThreshold || !this.mDarkTimeDelayEnable) {
            this.mNormDarkenDebounceTime = (long) (((float) this.mDarkenDebounceTime) * (((this.mDarkenDebounceTimeParaBig * (this.mStability - ((float) this.mStabilityConstant))) / 100.0f) + HwCircleAnimation.SMALL_ALPHA));
        } else {
            this.mNormDarkenDebounceTime = (long) this.mDarkTimeDelay;
        }
        this.mNormBrighenDebounceTimeForSmallThr = (long) this.mBrighenDebounceTimeForSmallThr;
        this.mNormDarkenDebounceTimeForSmallThr = (long) this.mDarkenDebounceTimeForSmallThr;
        setDarkenThresholdNew();
        setBrightenThresholdNew();
    }

    private void setBrightenThresholdNew() {
        int count = PROXIMITY_NEGATIVE;
        Point temp1 = null;
        for (Point temp : this.mBrightenlinePoints) {
            if (count == 0) {
                temp1 = temp;
            }
            if (this.mAmbientLux < temp.x) {
                Point temp2 = temp;
                if (temp.x <= temp1.x) {
                    this.mBrightenDeltaLuxMax = HwCircleAnimation.SMALL_ALPHA;
                    if (DEBUG) {
                        Slog.i(TAG, "Brighten_temp1.x <= temp2.x,x" + temp.x + ", y = " + temp.y);
                    }
                } else {
                    this.mBrightenDeltaLuxMax = (((temp.y - temp1.y) / (temp.x - temp1.x)) * (this.mAmbientLux - temp1.x)) + temp1.y;
                }
                this.mBrightenDeltaLuxMin = this.mBrightenDeltaLuxMax * this.mRatioForBrightnenSmallThr;
                this.mBrightenDeltaLuxMax *= ((this.mBrightenDeltaLuxPara * (this.mStability - ((float) this.mStabilityConstant))) / 100.0f) + HwCircleAnimation.SMALL_ALPHA;
            }
            temp1 = temp;
            this.mBrightenDeltaLuxMax = temp.y;
            count += PROXIMITY_POSITIVE;
        }
        this.mBrightenDeltaLuxMin = this.mBrightenDeltaLuxMax * this.mRatioForBrightnenSmallThr;
        this.mBrightenDeltaLuxMax *= ((this.mBrightenDeltaLuxPara * (this.mStability - ((float) this.mStabilityConstant))) / 100.0f) + HwCircleAnimation.SMALL_ALPHA;
    }

    private void setDarkenThresholdNew() {
        int count = PROXIMITY_NEGATIVE;
        Point temp1 = null;
        for (Point temp : this.mDarkenlinePoints) {
            if (count == 0) {
                temp1 = temp;
            }
            if (this.mAmbientLux < temp.x) {
                Point temp2 = temp;
                if (temp.x <= temp1.x) {
                    this.mDarkenDeltaLuxMax = HwCircleAnimation.SMALL_ALPHA;
                    if (DEBUG) {
                        Slog.i(TAG, "Darken_temp1.x <= temp2.x,x" + temp.x + ", y = " + temp.y);
                    }
                } else {
                    this.mDarkenDeltaLuxMax = Math.max((((temp.y - temp1.y) / (temp.x - temp1.x)) * (this.mAmbientLux - temp1.x)) + temp1.y, HwCircleAnimation.SMALL_ALPHA);
                }
                if (this.mAmbientLux >= 10.0f) {
                    this.mDarkenDeltaLuxMin = this.mDarkenDeltaLuxMax;
                } else {
                    this.mDarkenDeltaLuxMin = this.mDarkenDeltaLuxMax * this.mRatioForDarkenSmallThr;
                }
                this.mDarkenDeltaLuxMax *= ((this.mDarkenDeltaLuxPara * (this.mStability - ((float) this.mStabilityConstant))) / 100.0f) + HwCircleAnimation.SMALL_ALPHA;
            }
            temp1 = temp;
            this.mDarkenDeltaLuxMax = temp.y;
            count += PROXIMITY_POSITIVE;
        }
        if (this.mAmbientLux >= 10.0f) {
            this.mDarkenDeltaLuxMin = this.mDarkenDeltaLuxMax * this.mRatioForDarkenSmallThr;
        } else {
            this.mDarkenDeltaLuxMin = this.mDarkenDeltaLuxMax;
        }
        this.mDarkenDeltaLuxMax *= ((this.mDarkenDeltaLuxPara * (this.mStability - ((float) this.mStabilityConstant))) / 100.0f) + HwCircleAnimation.SMALL_ALPHA;
    }

    private float calculateAmbientLux(long now) {
        int N = this.mAmbientLightRingBuffer.size();
        if (N == 0) {
            Slog.e(TAG, "calculateAmbientLux: No ambient light readings available");
            return -1.0f;
        } else if (this.mPostMMAFilterNum <= 0 || this.mPostMMAFilterNoFilterNum < this.mPostMMAFilterNum) {
            Slog.e(TAG, "error filterPara: PostMMVFilterNoFilterNum=" + this.mPostMMAFilterNoFilterNum + ",PostMMVFilterNum=" + this.mPostMMAFilterNum);
            return 0.0f;
        } else if (N <= this.mPostMMAFilterNoFilterNum) {
            return this.mAmbientLightRingBuffer.getLux(N + PROXIMITY_UNKNOWN);
        } else {
            float sum = this.mAmbientLightRingBuffer.getLux(N + PROXIMITY_UNKNOWN);
            float luxMin = this.mAmbientLightRingBuffer.getLux(N + PROXIMITY_UNKNOWN);
            float luxMax = this.mAmbientLightRingBuffer.getLux(N + PROXIMITY_UNKNOWN);
            for (int i = N - 2; i >= N - this.mPostMMAFilterNum; i += PROXIMITY_UNKNOWN) {
                if (luxMin > this.mAmbientLightRingBuffer.getLux(i)) {
                    luxMin = this.mAmbientLightRingBuffer.getLux(i);
                }
                if (luxMax < this.mAmbientLightRingBuffer.getLux(i)) {
                    luxMax = this.mAmbientLightRingBuffer.getLux(i);
                }
                sum += this.mAmbientLightRingBuffer.getLux(i);
            }
            return ((sum - luxMin) - luxMax) / CustomGestureDetector.TOUCH_TOLERANCE;
        }
    }

    private long nextAmbientLightBrighteningTransition(long time) {
        long earliestValidTime = time;
        for (int i = this.mAmbientLightRingBuffer.size() + PROXIMITY_UNKNOWN; i >= 0; i += PROXIMITY_UNKNOWN) {
            boolean BrightenChange;
            if (this.mAmbientLightRingBuffer.getLux(i) - this.mAmbientLux > this.mBrightenDeltaLuxMax) {
                BrightenChange = true;
            } else {
                BrightenChange = DEBUG;
            }
            if (!BrightenChange) {
                break;
            }
            earliestValidTime = this.mAmbientLightRingBuffer.getTime(i);
        }
        return getNextAmbientLightBrighteningTime(earliestValidTime);
    }

    private long nextAmbientLightBrighteningTransitionForSmallThr(long time) {
        long earliestValidTime = time;
        for (int i = this.mAmbientLightRingBuffer.size() + PROXIMITY_UNKNOWN; i >= 0; i += PROXIMITY_UNKNOWN) {
            boolean BrightenChange;
            if (this.mAmbientLightRingBuffer.getLux(i) - this.mAmbientLux > this.mBrightenDeltaLuxMin) {
                BrightenChange = true;
            } else {
                BrightenChange = DEBUG;
            }
            if (!BrightenChange) {
                break;
            }
            earliestValidTime = this.mAmbientLightRingBuffer.getTime(i);
        }
        return earliestValidTime + this.mNormBrighenDebounceTimeForSmallThr;
    }

    private long nextAmbientLightDarkeningTransition(long time) {
        long earliestValidTime = time;
        for (int i = this.mAmbientLightRingBuffer.size() + PROXIMITY_UNKNOWN; i >= 0; i += PROXIMITY_UNKNOWN) {
            boolean DarkenChange;
            if (this.mAmbientLux - this.mAmbientLightRingBuffer.getLux(i) >= this.mDarkenDeltaLuxMax) {
                DarkenChange = true;
            } else {
                DarkenChange = DEBUG;
            }
            if (!DarkenChange) {
                break;
            }
            earliestValidTime = this.mAmbientLightRingBuffer.getTime(i);
        }
        return getNextAmbientLightDarkeningTime(earliestValidTime);
    }

    private long nextAmbientLightDarkeningTransitionForSmallThr(long time) {
        long earliestValidTime = time;
        for (int i = this.mAmbientLightRingBuffer.size() + PROXIMITY_UNKNOWN; i >= 0; i += PROXIMITY_UNKNOWN) {
            boolean DarkenChange;
            if (this.mAmbientLux - this.mAmbientLightRingBuffer.getLux(i) >= this.mDarkenDeltaLuxMin) {
                DarkenChange = true;
            } else {
                DarkenChange = DEBUG;
            }
            if (!DarkenChange) {
                break;
            }
            earliestValidTime = this.mAmbientLightRingBuffer.getTime(i);
        }
        return earliestValidTime + this.mNormDarkenDebounceTimeForSmallThr;
    }

    private boolean decideToBrighten(float ambientLux) {
        boolean needToBrighten;
        if (ambientLux - this.mAmbientLux < this.mBrightenDeltaLuxMax || this.mStability >= this.mStabilityBrightenConstant) {
            needToBrighten = DEBUG;
        } else {
            needToBrighten = true;
        }
        if (!needToBrighten || this.mAutoBrightnessIntervened) {
            return DEBUG;
        }
        return true;
    }

    private boolean decideToBrightenForSmallThr(float ambientLux) {
        boolean needToBrighten;
        if (ambientLux - this.mAmbientLux < this.mBrightenDeltaLuxMin || this.mStabilityForSmallThr >= this.mStabilityBrightenConstantForSmallThr) {
            needToBrighten = DEBUG;
        } else {
            needToBrighten = true;
        }
        if (!needToBrighten || this.mAutoBrightnessIntervened) {
            return DEBUG;
        }
        return true;
    }

    private boolean decideToDarken(float ambientLux) {
        boolean needToDarken;
        if (this.mAmbientLux - ambientLux < this.mDarkenDeltaLuxMax || this.mStability > this.mStabilityDarkenConstant) {
            needToDarken = DEBUG;
        } else {
            needToDarken = true;
        }
        if (!needToDarken || this.mAutoBrightnessIntervened || this.mProximityPositiveStatus) {
            return DEBUG;
        }
        return true;
    }

    private boolean decideToDarkenForSmallThr(float ambientLux) {
        boolean needToDarken;
        if (this.mAmbientLux - ambientLux < this.mDarkenDeltaLuxMin || this.mStabilityForSmallThr > this.mStabilityDarkenConstantForSmallThr) {
            needToDarken = DEBUG;
        } else {
            needToDarken = true;
        }
        if (!needToDarken || this.mAutoBrightnessIntervened || this.mProximityPositiveStatus) {
            return DEBUG;
        }
        return true;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private float calculateStability(HwRingBuffer buffer) {
        int N = buffer.size();
        if (N <= PROXIMITY_POSITIVE) {
            return 0.0f;
        }
        int index1;
        int index2;
        float tmp;
        float Stability1;
        float Stability2;
        float Stability;
        float currentLux = buffer.getLux(N + PROXIMITY_UNKNOWN);
        calculateAvg(buffer);
        float luxT1 = currentLux;
        float luxT2 = currentLux;
        int T1 = PROXIMITY_NEGATIVE;
        int T2 = PROXIMITY_NEGATIVE;
        int index = PROXIMITY_NEGATIVE;
        float luxT1Min = currentLux;
        float luxT2Min = currentLux;
        int indexMin = PROXIMITY_NEGATIVE;
        float luxT1Max = currentLux;
        float luxT2Max = currentLux;
        int indexMax = PROXIMITY_NEGATIVE;
        for (int j = PROXIMITY_NEGATIVE; j < N + PROXIMITY_UNKNOWN; j += PROXIMITY_POSITIVE) {
            Object obj;
            float lux1 = buffer.getLux((N + PROXIMITY_UNKNOWN) - j);
            float lux2 = buffer.getLux(((N + PROXIMITY_UNKNOWN) - j) + PROXIMITY_UNKNOWN);
            if (this.mLuxBufferAvg <= lux1) {
            }
            if (this.mLuxBufferAvg >= lux1) {
                if (this.mLuxBufferAvg <= lux2) {
                    if (Math.abs(this.mLuxBufferAvg - lux1) < 1.0E-7f) {
                        if (Math.abs(this.mLuxBufferAvg - lux2) < 1.0E-7f) {
                            obj = PROXIMITY_POSITIVE;
                            if (obj == null) {
                                luxT1 = lux1;
                                luxT2 = lux2;
                                T1 = (N + PROXIMITY_UNKNOWN) - j;
                                T2 = ((N + PROXIMITY_UNKNOWN) - j) + PROXIMITY_UNKNOWN;
                                index = j;
                            }
                        }
                    }
                    obj = null;
                    if (obj == null) {
                        luxT1 = lux1;
                        luxT2 = lux2;
                        T1 = (N + PROXIMITY_UNKNOWN) - j;
                        T2 = ((N + PROXIMITY_UNKNOWN) - j) + PROXIMITY_UNKNOWN;
                        index = j;
                    }
                }
            }
            if (this.mLuxBufferAvgMin <= lux1) {
            }
            if (this.mLuxBufferAvgMin >= lux1) {
                if (this.mLuxBufferAvgMin <= lux2) {
                    if (Math.abs(this.mLuxBufferAvgMin - lux1) < 1.0E-7f) {
                        if (Math.abs(this.mLuxBufferAvgMin - lux2) < 1.0E-7f) {
                            obj = PROXIMITY_POSITIVE;
                            if (obj == null) {
                                luxT1Min = lux1;
                                luxT2Min = lux2;
                                indexMin = j;
                            }
                        }
                    }
                    obj = null;
                    if (obj == null) {
                        luxT1Min = lux1;
                        luxT2Min = lux2;
                        indexMin = j;
                    }
                }
            }
            if (this.mLuxBufferAvgMax <= lux1) {
            }
            if (this.mLuxBufferAvgMax >= lux1) {
                if (this.mLuxBufferAvgMax <= lux2) {
                    if (Math.abs(this.mLuxBufferAvgMax - lux1) < 1.0E-7f) {
                        if (Math.abs(this.mLuxBufferAvgMax - lux2) < 1.0E-7f) {
                            obj = PROXIMITY_POSITIVE;
                            if (obj == null) {
                                luxT1Max = lux1;
                                luxT2Max = lux2;
                                indexMax = j;
                            }
                        }
                    }
                    obj = null;
                    if (obj == null) {
                        luxT1Max = lux1;
                        luxT2Max = lux2;
                        indexMax = j;
                    }
                }
            }
            if (!(index == 0 || (indexMin == 0 && indexMax == 0))) {
                if (index > indexMin || index < indexMax) {
                    if (index >= indexMin && index <= indexMax) {
                        break;
                    }
                }
                break;
            }
        }
        if (indexMax <= indexMin) {
            index1 = indexMax;
            index2 = indexMin;
        } else {
            index1 = indexMin;
            index2 = indexMax;
        }
        int k1 = (N + PROXIMITY_UNKNOWN) - index1;
        while (k1 <= N + PROXIMITY_UNKNOWN && k1 != N + PROXIMITY_UNKNOWN) {
            float luxk1 = buffer.getLux(k1);
            float luxk2 = buffer.getLux(k1 + PROXIMITY_POSITIVE);
            if (indexMax > indexMin) {
                if (luxk1 <= luxk2) {
                    break;
                }
                T1 = k1 + PROXIMITY_POSITIVE;
            } else if (luxk1 >= luxk2) {
                break;
            } else {
                T1 = k1 + PROXIMITY_POSITIVE;
            }
            k1 += PROXIMITY_POSITIVE;
        }
        int k3 = (N + PROXIMITY_UNKNOWN) - index2;
        while (k3 >= 0 && k3 != 0) {
            float luxk3 = buffer.getLux(k3);
            float luxk4 = buffer.getLux(k3 + PROXIMITY_UNKNOWN);
            if (indexMax > indexMin) {
                if (luxk3 >= luxk4) {
                    break;
                }
                T2 = k3 + PROXIMITY_UNKNOWN;
            } else if (luxk3 <= luxk4) {
                break;
            } else {
                T2 = k3 + PROXIMITY_UNKNOWN;
            }
            k3 += PROXIMITY_UNKNOWN;
        }
        int t1 = (N + PROXIMITY_UNKNOWN) - T1;
        int t2 = T2;
        float s1 = calculateStabilityFactor(buffer, T1, N + PROXIMITY_UNKNOWN);
        float avg1 = calcluateAvg(buffer, T1, N + PROXIMITY_UNKNOWN);
        float s2 = calculateStabilityFactor(buffer, PROXIMITY_NEGATIVE, T2);
        float deltaAvg = Math.abs(avg1 - calcluateAvg(buffer, PROXIMITY_NEGATIVE, T2));
        float k = 0.0f;
        if (T1 != T2) {
            k = Math.abs((buffer.getLux(T1) - buffer.getLux(T2)) / ((float) (T1 - T2)));
        }
        if (k < 10.0f / (5.0f + k)) {
            tmp = k;
        } else {
            tmp = 10.0f / (5.0f + k);
        }
        if (tmp > 20.0f / (10.0f + deltaAvg)) {
            tmp = 20.0f / (10.0f + deltaAvg);
        }
        if (t1 > this.mStabilityTime1) {
            Stability1 = s1;
        } else {
            float a1 = (float) Math.exp((double) (t1 - this.mStabilityTime1));
            float b1 = (float) (this.mStabilityTime1 - t1);
            float s3 = tmp;
            Stability1 = ((a1 * s1) + (b1 * tmp)) / (a1 + b1);
        }
        if (t2 > this.mStabilityTime2) {
            Stability2 = s2;
        } else {
            float a2 = (float) Math.exp((double) (t2 - this.mStabilityTime2));
            float b2 = (float) (this.mStabilityTime2 - t2);
            float s4 = tmp;
            Stability2 = ((a2 * s2) + (b2 * tmp)) / (a2 + b2);
        }
        if (t1 > this.mStabilityTime1) {
            Stability = Stability1;
        } else {
            float a = (float) Math.exp((double) (t1 - this.mStabilityTime1));
            float b = (float) (this.mStabilityTime1 - t1);
            Stability = ((a * Stability1) + (b * Stability2)) / (a + b);
        }
        return Stability;
    }

    private void calculateAvg(HwRingBuffer buffer) {
        int N = buffer.size();
        if (N != 0) {
            float currentLux = buffer.getLux(N + PROXIMITY_UNKNOWN);
            float luxBufferSum = 0.0f;
            float luxBufferMin = currentLux;
            float luxBufferMax = currentLux;
            for (int i = N + PROXIMITY_UNKNOWN; i >= 0; i += PROXIMITY_UNKNOWN) {
                float lux = buffer.getLux(i);
                if (lux > luxBufferMax) {
                    luxBufferMax = lux;
                }
                if (lux < luxBufferMin) {
                    luxBufferMin = lux;
                }
                luxBufferSum += lux;
            }
            this.mLuxBufferAvg = luxBufferSum / ((float) N);
            this.mLuxBufferAvgMax = (this.mLuxBufferAvg + luxBufferMax) / 2.0f;
            this.mLuxBufferAvgMin = (this.mLuxBufferAvg + luxBufferMin) / 2.0f;
        }
    }

    private float calculateStabilityForSmallThr(HwRingBuffer buffer) {
        int N = buffer.size();
        if (N <= PROXIMITY_POSITIVE) {
            return 0.0f;
        }
        if (N <= 15) {
            return calculateStabilityFactor(buffer, PROXIMITY_NEGATIVE, N + PROXIMITY_UNKNOWN);
        }
        return calculateStabilityFactor(buffer, PROXIMITY_NEGATIVE, 14);
    }

    private float calcluateAvg(HwRingBuffer buffer, int start, int end) {
        float sum = 0.0f;
        for (int i = start; i <= end; i += PROXIMITY_POSITIVE) {
            sum += buffer.getLux(i);
        }
        if (end < start) {
            return 0.0f;
        }
        return sum / ((float) ((end - start) + PROXIMITY_POSITIVE));
    }

    private float calculateStabilityFactor(HwRingBuffer buffer, int start, int end) {
        int size = (end - start) + PROXIMITY_POSITIVE;
        float sum = 0.0f;
        float sigma = 0.0f;
        if (size <= PROXIMITY_POSITIVE) {
            return 0.0f;
        }
        int i;
        for (i = start; i <= end; i += PROXIMITY_POSITIVE) {
            sum += buffer.getLux(i);
        }
        float avg = sum / ((float) size);
        for (i = start; i <= end; i += PROXIMITY_POSITIVE) {
            sigma += (buffer.getLux(i) - avg) * (buffer.getLux(i) - avg);
        }
        float ss = sigma / ((float) (size + PROXIMITY_UNKNOWN));
        if (avg == 0.0f) {
            return 0.0f;
        }
        return ss / avg;
    }

    public boolean reportValueWhenSensorOnChange() {
        return this.mReportValueWhenSensorOnChange;
    }

    public int getProximityState() {
        return this.mProximity;
    }

    public boolean needToUseProximity() {
        return this.mAllowLabcUseProximity;
    }

    public boolean needToSendProximityDebounceMsg() {
        return this.mNeedToSendProximityDebounceMsg;
    }

    public long getPendingProximityDebounceTime() {
        return this.mPendingProximityDebounceTime;
    }

    public void setCoverModeStatus(boolean isclosed) {
        if (!isclosed && this.mIsclosed) {
            this.mCoverState = true;
        }
        this.mIsclosed = isclosed;
    }

    public void setCoverModeFastResponseFlag(boolean isFast) {
        this.mIsCoverModeFastResponseFlag = isFast;
        if (DEBUG) {
            Slog.i(TAG, "LabcCoverMode mIsCoverModeFastResponseFlag=" + this.mIsCoverModeFastResponseFlag);
        }
    }

    public boolean getLastCloseScreenEnable() {
        return this.mLastCloseScreenEnable ? DEBUG : true;
    }

    private void setProximityState(boolean proximityPositive) {
        this.mProximityPositiveStatus = proximityPositive;
        if (!this.mProximityPositiveStatus) {
            this.mNeedToUpdateBrightness = true;
            if (DEBUG) {
                Slog.i(TAG, "Proximity sets brightness");
            }
        }
    }

    private void clearPendingProximityDebounceTime() {
        if (this.mPendingProximityDebounceTime >= 0) {
            this.mPendingProximityDebounceTime = -1;
        }
    }

    public void handleProximitySensorEvent(long time, boolean positive) {
        if (this.mPendingProximity == 0 && !positive) {
            return;
        }
        if (this.mPendingProximity != PROXIMITY_POSITIVE || !positive) {
            if (positive) {
                this.mPendingProximity = PROXIMITY_POSITIVE;
                this.mPendingProximityDebounceTime = ((long) this.mProximityPositiveDebounceTime) + time;
            } else {
                this.mPendingProximity = PROXIMITY_NEGATIVE;
                this.mPendingProximityDebounceTime = ((long) this.mProximityNegativeDebounceTime) + time;
            }
            debounceProximitySensor();
        }
    }

    public void debounceProximitySensor() {
        this.mNeedToSendProximityDebounceMsg = DEBUG;
        if (this.mPendingProximity != PROXIMITY_UNKNOWN && this.mPendingProximityDebounceTime >= 0) {
            if (this.mPendingProximityDebounceTime <= SystemClock.uptimeMillis()) {
                this.mProximity = this.mPendingProximity;
                if (this.mProximity == PROXIMITY_POSITIVE) {
                    setProximityState(true);
                } else if (this.mProximity == 0) {
                    setProximityState(DEBUG);
                }
                if (DEBUG) {
                    Slog.d(TAG, "debounceProximitySensor:mProximity=" + this.mProximity);
                }
                clearPendingProximityDebounceTime();
                return;
            }
            this.mNeedToSendProximityDebounceMsg = true;
        }
    }

    public int getpowerOnFastResponseLuxNum() {
        return this.mPowerOnFastResponseLuxNum;
    }
}
