package com.android.server.display;

import android.util.Slog;
import android.util.Xml;
import com.android.server.input.HwCircleAnimation;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import com.android.server.wifipro.WifiProCommonDefs;
import huawei.com.android.server.policy.HwGlobalActionsView;
import huawei.com.android.server.policy.fingersense.CustomGestureDetector;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class HwNormalizedManualBrightnessThresholdDetector {
    private static final int AMBIENT_LIGHT_HORIZON = 10000;
    private static final int AMBIENT_MAX_LUX = 40000;
    private static boolean DEBUG = false;
    private static final int INDOOR_UI = 1;
    private static final int OUTDOOR_UI = 2;
    private static String TAG;
    protected boolean mAmChangeFlagForHBM;
    protected HwRingBuffer mAmbientLightRingBuffer;
    protected HwRingBuffer mAmbientLightRingBufferFilter;
    protected float mAmbientLux;
    private int mBrighenDebounceTime;
    private float mBrightenDeltaLuxMax;
    List<Point> mBrightenLinePointsList;
    List<Point> mDarkLinePointsList;
    private int mDarkenDebounceTime;
    private float mDarkenDeltaLuxMax;
    private int mInDoorThreshold;
    private int mLastInOutState;
    private int mLightSensorRateMillis;
    private int mOutDoorThreshold;

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
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.display.HwNormalizedManualBrightnessThresholdDetector.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.display.HwNormalizedManualBrightnessThresholdDetector.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.display.HwNormalizedManualBrightnessThresholdDetector.<clinit>():void");
    }

    public HwNormalizedManualBrightnessThresholdDetector(String configFilePath) {
        this.mDarkenDeltaLuxMax = 0.0f;
        this.mBrightenDeltaLuxMax = 0.0f;
        this.mAmChangeFlagForHBM = false;
        this.mLastInOutState = INDOOR_UI;
        this.mBrightenLinePointsList = null;
        this.mDarkLinePointsList = null;
        this.mAmbientLightRingBuffer = new HwRingBuffer(50);
        this.mAmbientLightRingBufferFilter = new HwRingBuffer(50);
        try {
            if (!getConfig(configFilePath)) {
                Slog.e(TAG, "getConfig failed! loadDefaultConfig");
                loadDefaultConfig();
            }
        } catch (Exception e) {
            e.printStackTrace();
            loadDefaultConfig();
        }
    }

    private boolean getConfig(String configFilePath) throws IOException {
        Exception e;
        Throwable th;
        if (configFilePath == null || configFilePath.length() == 0) {
            Slog.e(TAG, "getConfig configFilePath is null! use default config");
            return false;
        }
        FileInputStream fileInputStream = null;
        try {
            FileInputStream inputStream = new FileInputStream(new File(configFilePath));
            try {
                if (getConfigFromXML(inputStream)) {
                    checkConfigLoadedFromXML();
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

    private void checkConfigLoadedFromXML() {
        if (this.mInDoorThreshold > this.mOutDoorThreshold) {
            loadDefaultConfig();
            Slog.e(TAG, "checkConfig failed for mInDoorThreshold > mOutDoorThreshold , LoadDefaultConfig!");
        } else if (this.mBrighenDebounceTime < 0 || this.mDarkenDebounceTime < 0) {
            loadDefaultConfig();
            Slog.e(TAG, "checkConfig failed for mBrighenDebounceTime or mDarkenDebounceTime is wrong, LoadDefaultConfig!");
        } else if (!checkPointsListIsOK(this.mBrightenLinePointsList)) {
            loadDefaultConfig();
            Slog.e(TAG, "checkConfig failed for mBrightenLinePointsList is wrong, LoadDefaultConfig!");
        } else if (checkPointsListIsOK(this.mDarkLinePointsList)) {
            Slog.i(TAG, "checkConfig LoadedFromXML success!");
        } else {
            loadDefaultConfig();
            Slog.e(TAG, "checkConfig failed for mDarkLinePointsList is wrong, LoadDefaultConfig!");
        }
    }

    private boolean checkPointsListIsOK(List<Point> LinePointsList) {
        List<Point> mLinePointsList = LinePointsList;
        if (LinePointsList == null) {
            Slog.e(TAG, "checkConfig false for mLinePointsList == null");
            return false;
        }
        int mDrkenNum = 0;
        Point lastPoint = null;
        for (Point tmpPoint : LinePointsList) {
            if (mDrkenNum == 0) {
                lastPoint = tmpPoint;
            } else if (lastPoint == null || lastPoint.x < tmpPoint.x) {
                lastPoint = tmpPoint;
            } else {
                loadDefaultConfig();
                Slog.e(TAG, "checkConfig false for mLinePointsList is wrong");
                return false;
            }
            mDrkenNum += INDOOR_UI;
        }
        return true;
    }

    protected void loadDefaultConfig() {
        Slog.i(TAG, "loadDefaultConfig");
        this.mLightSensorRateMillis = HwGlobalActionsView.VIBRATE_DELAY;
        this.mInDoorThreshold = 4000;
        this.mOutDoorThreshold = 8000;
        this.mBrighenDebounceTime = 3000;
        this.mDarkenDebounceTime = 3000;
        if (this.mBrightenLinePointsList != null) {
            this.mBrightenLinePointsList.clear();
        } else {
            this.mBrightenLinePointsList = new ArrayList();
        }
        this.mBrightenLinePointsList.add(new Point(0.0f, 1000.0f));
        this.mBrightenLinePointsList.add(new Point(1000.0f, 5000.0f));
        this.mBrightenLinePointsList.add(new Point(40000.0f, 10000.0f));
        if (this.mDarkLinePointsList != null) {
            this.mDarkLinePointsList.clear();
        } else {
            this.mDarkLinePointsList = new ArrayList();
        }
        this.mDarkLinePointsList.add(new Point(0.0f, HwCircleAnimation.SMALL_ALPHA));
        this.mDarkLinePointsList.add(new Point(500.0f, 10.0f));
        this.mDarkLinePointsList.add(new Point(1000.0f, 500.0f));
        this.mDarkLinePointsList.add(new Point(2000.0f, 1000.0f));
        this.mDarkLinePointsList.add(new Point(40000.0f, 30000.0f));
        Slog.i(TAG, "LoadDefaultConfig_LightSensorRateMillis=" + this.mLightSensorRateMillis);
        Slog.i(TAG, "LoadDefaultConfig_InDoorThreshold=" + this.mInDoorThreshold);
        Slog.i(TAG, "LoadDefaultConfig_OutDoorThreshold=" + this.mOutDoorThreshold);
        Slog.i(TAG, "LoadDefaultConfig_BrighenDebounceTime=" + this.mBrighenDebounceTime);
        Slog.i(TAG, "LoadDefaultConfig_DarkenDebounceTime=" + this.mDarkenDebounceTime);
        for (Point temp : this.mBrightenLinePointsList) {
            Slog.i(TAG, "LoadDefaultConfig_BrightenLinePointsList x = " + temp.x + ", y = " + temp.y);
        }
        for (Point temp2 : this.mDarkLinePointsList) {
            Slog.i(TAG, "LoadDefaultConfig_DarkLinePointsList x = " + temp2.x + ", y = " + temp2.y);
        }
    }

    private boolean getConfigFromXML(InputStream inStream) {
        Slog.i(TAG, "getConfigFromeXML");
        boolean inDoorThresholdLoaded = false;
        boolean outDoorThresholdLoaded = false;
        boolean brighenDebounceTimeLoaded = false;
        boolean darkenDebounceTimeLoaded = false;
        boolean brightenLinePointsLoadStarted = false;
        boolean brightenLinePointsLoaded = false;
        boolean darkenLinePointsLoadStarted = false;
        boolean darkenLinePointsLoaded = false;
        XmlPullParser parser = Xml.newPullParser();
        try {
            parser.setInput(inStream, "UTF-8");
            for (int eventType = parser.getEventType(); eventType != INDOOR_UI; eventType = parser.next()) {
                switch (eventType) {
                    case OUTDOOR_UI /*2*/:
                        String name = parser.getName();
                        if (name != null && name.length() != 0) {
                            if (!name.equals("LightSensorRateMills")) {
                                if (!name.equals("OutDoorThreshold")) {
                                    if (!name.equals("InDoorThreshold")) {
                                        if (!name.equals("ManualBrighenDebounceTime")) {
                                            if (!name.equals("ManualDarkenDebounceTime")) {
                                                if (!name.equals("ManualBrightenLinePoints")) {
                                                    Point currentPoint;
                                                    String s;
                                                    if (!name.equals("Point") || !brightenLinePointsLoadStarted) {
                                                        if (!name.equals("ManualDarkenLinePoints")) {
                                                            if (name.equals("Point") && darkenLinePointsLoadStarted) {
                                                                currentPoint = new Point();
                                                                s = parser.nextText();
                                                                currentPoint.x = Float.parseFloat(s.split(",")[0]);
                                                                currentPoint.y = Float.parseFloat(s.split(",")[INDOOR_UI]);
                                                                if (this.mDarkLinePointsList == null) {
                                                                    this.mDarkLinePointsList = new ArrayList();
                                                                }
                                                                this.mDarkLinePointsList.add(currentPoint);
                                                                break;
                                                            }
                                                        }
                                                        darkenLinePointsLoadStarted = true;
                                                        break;
                                                    }
                                                    currentPoint = new Point();
                                                    s = parser.nextText();
                                                    currentPoint.x = Float.parseFloat(s.split(",")[0]);
                                                    currentPoint.y = Float.parseFloat(s.split(",")[INDOOR_UI]);
                                                    if (this.mBrightenLinePointsList == null) {
                                                        this.mBrightenLinePointsList = new ArrayList();
                                                    }
                                                    this.mBrightenLinePointsList.add(currentPoint);
                                                    break;
                                                }
                                                brightenLinePointsLoadStarted = true;
                                                break;
                                            }
                                            this.mDarkenDebounceTime = Integer.parseInt(parser.nextText());
                                            darkenDebounceTimeLoaded = true;
                                            Slog.i(TAG, "ManualDarkenDebounceTime = " + this.mDarkenDebounceTime);
                                            break;
                                        }
                                        this.mBrighenDebounceTime = Integer.parseInt(parser.nextText());
                                        brighenDebounceTimeLoaded = true;
                                        Slog.i(TAG, "ManualBrighenDebounceTime = " + this.mBrighenDebounceTime);
                                        break;
                                    }
                                    this.mInDoorThreshold = Integer.parseInt(parser.nextText());
                                    inDoorThresholdLoaded = true;
                                    Slog.i(TAG, "InDoorThreshold = " + this.mInDoorThreshold);
                                    break;
                                }
                                this.mOutDoorThreshold = Integer.parseInt(parser.nextText());
                                outDoorThresholdLoaded = true;
                                Slog.i(TAG, "OutDoorThreshold = " + this.mOutDoorThreshold);
                                break;
                            }
                            this.mLightSensorRateMillis = Integer.parseInt(parser.nextText());
                            Slog.i(TAG, "LightSensorRateMills = " + this.mLightSensorRateMillis + "ms");
                            break;
                        }
                        return false;
                        break;
                    case WifiProCommonDefs.WIFI_SECURITY_PHISHING_FAILED /*3*/:
                        if (parser.getName().equals("ManualBrightenLinePoints")) {
                            brightenLinePointsLoadStarted = false;
                            if (this.mBrightenLinePointsList != null) {
                                brightenLinePointsLoaded = true;
                                break;
                            }
                            Slog.e(TAG, "no ManualBrightenLinePoints loaded!");
                            return false;
                        } else if (parser.getName().equals("ManualDarkenLinePoints")) {
                            darkenLinePointsLoadStarted = false;
                            if (this.mDarkLinePointsList != null) {
                                darkenLinePointsLoaded = true;
                                break;
                            }
                            Slog.e(TAG, "no ManualDarkenLinePoints loaded!");
                            return false;
                        } else {
                            continue;
                        }
                    default:
                        break;
                }
            }
            if (inDoorThresholdLoaded && outDoorThresholdLoaded && brighenDebounceTimeLoaded && darkenDebounceTimeLoaded && brightenLinePointsLoaded && darkenLinePointsLoaded) {
                float f;
                Slog.i(TAG, "getConfigFromeXML success!");
                for (Point temp : this.mBrightenLinePointsList) {
                    f = temp.x;
                    Slog.i(TAG, "mBrightenLinePointsList x = " + r0 + ", y = " + temp.y);
                }
                for (Point temp2 : this.mDarkLinePointsList) {
                    f = temp2.x;
                    Slog.i(TAG, "mDarkLinePointsList x = " + r0 + ", y = " + temp2.y);
                }
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
        Slog.e(TAG, "getConfig failed!");
        return false;
    }

    public void clearAmbientLightRingBuffer() {
        this.mAmbientLightRingBuffer.clear();
        this.mAmbientLightRingBufferFilter.clear();
    }

    public void handleLightSensorEvent(long time, float lux) {
        if (lux > 40000.0f) {
            lux = 40000.0f;
        }
        applyLightSensorMeasurement(time, lux);
        updateAmbientLux(time);
    }

    private void applyLightSensorMeasurement(long time, float lux) {
        this.mAmbientLightRingBuffer.prune(time - MemoryConstant.MIN_INTERVAL_OP_TIMEOUT);
        this.mAmbientLightRingBuffer.push(time, lux);
    }

    private float calculateAmbientLuxForNewPolicy(long now) {
        int N = this.mAmbientLightRingBuffer.size();
        if (N == 0) {
            if (DEBUG) {
                Slog.v(TAG, "calculateAmbientLux: No ambient light readings available");
            }
            return -1.0f;
        } else if (N < 5) {
            return this.mAmbientLightRingBuffer.getLux(N - 1);
        } else {
            float sum = this.mAmbientLightRingBuffer.getLux(N - 1);
            float luxMin = this.mAmbientLightRingBuffer.getLux(N - 1);
            float luxMax = this.mAmbientLightRingBuffer.getLux(N - 1);
            for (int i = N - 2; i >= (N - 1) - 4; i--) {
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

    private long nextAmbientLightBrighteningTransitionForNewPolicy(long time) {
        int N = this.mAmbientLightRingBufferFilter.size();
        long debounceTime = (long) this.mBrighenDebounceTime;
        if (INDOOR_UI == N) {
            debounceTime = 0;
        }
        long earliestValidTime = time;
        for (int i = N - 1; i >= 0; i--) {
            boolean BrightenChange;
            if (this.mAmbientLightRingBufferFilter.getLux(i) - this.mAmbientLux > this.mBrightenDeltaLuxMax) {
                BrightenChange = true;
            } else {
                BrightenChange = false;
            }
            if (!BrightenChange) {
                break;
            }
            earliestValidTime = this.mAmbientLightRingBufferFilter.getTime(i);
        }
        return earliestValidTime + debounceTime;
    }

    private long nextAmbientLightDarkeningTransitionForNewPolicy(long time) {
        int N = this.mAmbientLightRingBufferFilter.size();
        long debounceTime = (long) this.mDarkenDebounceTime;
        if (INDOOR_UI == N) {
            debounceTime = 0;
        }
        long earliestValidTime = time;
        for (int i = N - 1; i >= 0; i--) {
            boolean DarkenChange;
            if (this.mAmbientLux - this.mAmbientLightRingBufferFilter.getLux(i) >= this.mDarkenDeltaLuxMax) {
                DarkenChange = true;
            } else {
                DarkenChange = false;
            }
            if (!DarkenChange) {
                break;
            }
            earliestValidTime = this.mAmbientLightRingBufferFilter.getTime(i);
        }
        return earliestValidTime + debounceTime;
    }

    private long nextAmbientLightDarkeningTransitionForInOut(long time) {
        int N = this.mAmbientLightRingBufferFilter.size();
        long debounceTime = (long) this.mBrighenDebounceTime;
        if (INDOOR_UI == N) {
            debounceTime = 0;
        }
        long earliestValidTime = time;
        int i = N - 1;
        while (i >= 0) {
            boolean DarkenChange;
            if (this.mAmbientLightRingBufferFilter.getLux(i) <= ((float) this.mInDoorThreshold) || this.mAmbientLightRingBufferFilter.getLux(i) >= ((float) this.mOutDoorThreshold)) {
                DarkenChange = false;
            } else {
                DarkenChange = true;
            }
            if (!DarkenChange) {
                break;
            }
            earliestValidTime = this.mAmbientLightRingBufferFilter.getTime(i);
            i--;
        }
        return earliestValidTime + debounceTime;
    }

    private void setBrightenThresholdNew() {
        int count = 0;
        Point temp1 = null;
        for (Point temp : this.mBrightenLinePointsList) {
            if (count == 0) {
                temp1 = temp;
            }
            if (this.mAmbientLux < temp.x) {
                Point temp2 = temp;
                if (temp.x <= temp1.x) {
                    this.mBrightenDeltaLuxMax = HwCircleAnimation.SMALL_ALPHA;
                    if (DEBUG) {
                        Slog.i(TAG, "Brighten_temp1.x <= temp2.x,x" + temp.x + ", y = " + temp.y);
                        return;
                    }
                    return;
                }
                this.mBrightenDeltaLuxMax = (((temp.y - temp1.y) / (temp.x - temp1.x)) * (this.mAmbientLux - temp1.x)) + temp1.y;
                return;
            }
            temp1 = temp;
            this.mBrightenDeltaLuxMax = temp.y;
            count += INDOOR_UI;
        }
    }

    private void setDarkenThresholdNew() {
        int count = 0;
        Point temp1 = null;
        for (Point temp : this.mDarkLinePointsList) {
            if (count == 0) {
                temp1 = temp;
            }
            if (this.mAmbientLux < temp.x) {
                Point temp2 = temp;
                if (temp.x <= temp1.x) {
                    this.mDarkenDeltaLuxMax = HwCircleAnimation.SMALL_ALPHA;
                    if (DEBUG) {
                        Slog.i(TAG, "Darken_temp1.x <= temp2.x,x" + temp.x + ", y = " + temp.y);
                        return;
                    }
                    return;
                }
                this.mDarkenDeltaLuxMax = Math.max((((temp.y - temp1.y) / (temp.x - temp1.x)) * (this.mAmbientLux - temp1.x)) + temp1.y, HwCircleAnimation.SMALL_ALPHA);
                return;
            }
            temp1 = temp;
            this.mDarkenDeltaLuxMax = temp.y;
            count += INDOOR_UI;
        }
    }

    private void updateAmbientLux(long time) {
        float value = calculateAmbientLuxForNewPolicy(time);
        if (this.mAmbientLightRingBuffer.size() == INDOOR_UI) {
            Slog.i(TAG, "fist sensor lux and filteredlux=" + value + ",time=" + time);
        }
        this.mAmbientLightRingBufferFilter.push(time, value);
        this.mAmbientLightRingBufferFilter.prune(time - MemoryConstant.MIN_INTERVAL_OP_TIMEOUT);
        long nextBrightenTransition = nextAmbientLightBrighteningTransitionForNewPolicy(time);
        long nextDarkenTransition = nextAmbientLightDarkeningTransitionForNewPolicy(time);
        long nextInOutTransition = nextAmbientLightDarkeningTransitionForInOut(time);
        this.mAmChangeFlagForHBM = false;
        boolean updateFlag = nextBrightenTransition <= time || nextDarkenTransition <= time || ((nextBrightenTransition <= time && nextInOutTransition <= time) || (nextDarkenTransition <= time && nextInOutTransition <= time));
        if (nextBrightenTransition > time && nextDarkenTransition > time) {
            if (!updateFlag) {
                return;
            }
        }
        if (DEBUG) {
            Slog.i(TAG, "update_Flag=" + updateFlag + ",filteredlux=" + value + ",time=" + time + ",nextBTime=" + nextBrightenTransition + ",nextDTime=" + nextDarkenTransition + ",nextInOutTime=" + nextInOutTransition);
        }
        updateParaForHBM(value);
    }

    private void updateParaForHBM(float lux) {
        float mAmbientLuxTmp = lux;
        if (lux >= ((float) this.mOutDoorThreshold)) {
            this.mAmbientLux = lux;
            this.mLastInOutState = OUTDOOR_UI;
            this.mAmChangeFlagForHBM = true;
        }
        if (lux < ((float) this.mInDoorThreshold)) {
            this.mAmbientLux = lux;
            this.mLastInOutState = INDOOR_UI;
            this.mAmChangeFlagForHBM = true;
        }
        if (lux < ((float) this.mOutDoorThreshold) && lux >= ((float) this.mInDoorThreshold)) {
            this.mAmbientLux = lux;
            if (this.mAmbientLightRingBufferFilter.size() == INDOOR_UI) {
                this.mLastInOutState = INDOOR_UI;
            }
            this.mAmChangeFlagForHBM = true;
        }
        setBrightenThresholdNew();
        setDarkenThresholdNew();
        if (DEBUG) {
            Slog.i(TAG, "update_lux =" + this.mAmbientLux + ",IN_OUT_DoorFlag =" + this.mLastInOutState + ",mBrightenDeltaLuxMax=" + this.mBrightenDeltaLuxMax + ",mDarkenDeltaLuxMax=" + this.mDarkenDeltaLuxMax);
        }
    }

    public float getAmbientLuxForHBM() {
        return this.mAmbientLux;
    }

    public boolean getLuxChangedFlagForHBM() {
        return this.mAmChangeFlagForHBM;
    }

    public void setLuxChangedFlagForHBM() {
        this.mAmChangeFlagForHBM = false;
    }

    public int getIndoorOutdoorFlagForHBM() {
        return this.mLastInOutState;
    }
}
