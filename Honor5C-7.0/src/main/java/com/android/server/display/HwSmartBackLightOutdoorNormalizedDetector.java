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

public class HwSmartBackLightOutdoorNormalizedDetector {
    protected static final int AMBIENT_LIGHT_HORIZON = 10000;
    private static final int AMBIENT_MAX_LUX = 40000;
    private static final int APICAL_INDOOR_UI = 1;
    private static final int APICAL_OUTDOOR_UI = 2;
    private static boolean DEBUG;
    private static String TAG;
    protected boolean mAmChangeFlagSBL;
    protected AmbientLightRingBufferForSbl mAmbientLightRingBuffer;
    protected AmbientLightRingBufferForSbl mAmbientLightRingBufferFilter;
    protected float mAmbientLux;
    private int mBrighenDebounceTime;
    private float mBrightenDeltaLuxMax;
    List<Point> mBrightenLinePointsList;
    List<Point> mDarkLinePointsList;
    private int mDarkenDebounceTime;
    private float mDarkenDeltaLuxMax;
    protected int mInDoorThreshold;
    protected boolean mInoutFlag;
    private int mLastApicalFlag;
    protected int mLightSensorRateMillis;
    protected int mOutDoorThreshold;

    protected final class AmbientLightRingBufferForSbl {
        private static final float BUFFER_SLACK = 1.5f;
        private static final int DEFAULT_CAPACITY = 50;
        private int mCapacity;
        private int mCount;
        private int mEnd;
        private float[] mRingLux;
        private long[] mRingTime;
        private int mStart;

        public AmbientLightRingBufferForSbl(HwSmartBackLightOutdoorNormalizedDetector this$0) {
            this(DEFAULT_CAPACITY);
        }

        public AmbientLightRingBufferForSbl(int initialCapacity) {
            this.mCapacity = initialCapacity;
            this.mRingLux = new float[this.mCapacity];
            this.mRingTime = new long[this.mCapacity];
        }

        public float getLux(int index) {
            return this.mRingLux[offsetOf(index)];
        }

        public long getTime(int index) {
            return this.mRingTime[offsetOf(index)];
        }

        public void push(long time, float lux) {
            int next = this.mEnd;
            if (this.mCount == this.mCapacity) {
                int newSize = this.mCapacity * HwSmartBackLightOutdoorNormalizedDetector.APICAL_OUTDOOR_UI;
                float[] newRingLux = new float[newSize];
                long[] newRingTime = new long[newSize];
                int length = this.mCapacity - this.mStart;
                System.arraycopy(this.mRingLux, this.mStart, newRingLux, 0, length);
                System.arraycopy(this.mRingTime, this.mStart, newRingTime, 0, length);
                if (this.mStart != 0) {
                    System.arraycopy(this.mRingLux, 0, newRingLux, length, this.mStart);
                    System.arraycopy(this.mRingTime, 0, newRingTime, length, this.mStart);
                }
                this.mRingLux = newRingLux;
                this.mRingTime = newRingTime;
                next = this.mCapacity;
                this.mCapacity = newSize;
                this.mStart = 0;
            }
            this.mRingTime[next] = time;
            this.mRingLux[next] = lux;
            this.mEnd = next + HwSmartBackLightOutdoorNormalizedDetector.APICAL_INDOOR_UI;
            if (this.mEnd == this.mCapacity) {
                this.mEnd = 0;
            }
            this.mCount += HwSmartBackLightOutdoorNormalizedDetector.APICAL_INDOOR_UI;
        }

        public void prune(long horizon) {
            if (this.mCount != 0) {
                while (this.mCount > HwSmartBackLightOutdoorNormalizedDetector.APICAL_INDOOR_UI) {
                    int next = this.mStart + HwSmartBackLightOutdoorNormalizedDetector.APICAL_INDOOR_UI;
                    if (next >= this.mCapacity) {
                        next -= this.mCapacity;
                    }
                    if (this.mRingTime[next] > horizon) {
                        break;
                    }
                    this.mStart = next;
                    this.mCount--;
                }
                if (this.mRingTime[this.mStart] < horizon) {
                    this.mRingTime[this.mStart] = horizon;
                }
            }
        }

        public int size() {
            return this.mCount;
        }

        public boolean isEmpty() {
            return this.mCount == 0;
        }

        public void clear() {
            this.mStart = 0;
            this.mEnd = 0;
            this.mCount = 0;
        }

        private int offsetOf(int index) {
            if (index >= this.mCount || index < 0) {
                throw new ArrayIndexOutOfBoundsException(index);
            }
            index += this.mStart;
            if (index >= this.mCapacity) {
                return index - this.mCapacity;
            }
            return index;
        }
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
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.display.HwSmartBackLightOutdoorNormalizedDetector.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.display.HwSmartBackLightOutdoorNormalizedDetector.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.display.HwSmartBackLightOutdoorNormalizedDetector.<clinit>():void");
    }

    public HwSmartBackLightOutdoorNormalizedDetector(String configFilePath) {
        this.mDarkenDeltaLuxMax = 0.0f;
        this.mBrightenDeltaLuxMax = 0.0f;
        this.mAmChangeFlagSBL = false;
        this.mInoutFlag = false;
        this.mLastApicalFlag = APICAL_INDOOR_UI;
        this.mBrightenLinePointsList = null;
        this.mDarkLinePointsList = null;
        this.mAmbientLightRingBuffer = new AmbientLightRingBufferForSbl(this);
        this.mAmbientLightRingBufferFilter = new AmbientLightRingBufferForSbl(this);
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
        } else if (this.mBrighenDebounceTime >= AMBIENT_LIGHT_HORIZON || this.mDarkenDebounceTime >= AMBIENT_LIGHT_HORIZON) {
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
            mDrkenNum += APICAL_INDOOR_UI;
        }
        return true;
    }

    protected void loadDefaultConfig() {
        Slog.i(TAG, "loadDefaultConfig");
        this.mLightSensorRateMillis = HwGlobalActionsView.VIBRATE_DELAY;
        this.mInDoorThreshold = 5000;
        this.mOutDoorThreshold = 8000;
        this.mBrighenDebounceTime = 3000;
        this.mDarkenDebounceTime = 3000;
        if (this.mBrightenLinePointsList != null) {
            this.mBrightenLinePointsList.clear();
        } else {
            this.mBrightenLinePointsList = new ArrayList();
        }
        this.mBrightenLinePointsList.add(new Point(0.0f, 15.0f));
        this.mBrightenLinePointsList.add(new Point(2.0f, 15.0f));
        this.mBrightenLinePointsList.add(new Point(10.0f, 19.0f));
        this.mBrightenLinePointsList.add(new Point(100.0f, 239.0f));
        this.mBrightenLinePointsList.add(new Point(500.0f, 439.0f));
        this.mBrightenLinePointsList.add(new Point(1000.0f, 989.0f));
        this.mBrightenLinePointsList.add(new Point(40000.0f, 989.0f));
        if (this.mDarkLinePointsList != null) {
            this.mDarkLinePointsList.clear();
        } else {
            this.mDarkLinePointsList = new ArrayList();
        }
        this.mDarkLinePointsList.add(new Point(0.0f, HwCircleAnimation.SMALL_ALPHA));
        this.mDarkLinePointsList.add(new Point(HwCircleAnimation.SMALL_ALPHA, HwCircleAnimation.SMALL_ALPHA));
        this.mDarkLinePointsList.add(new Point(15.0f, 15.0f));
        this.mDarkLinePointsList.add(new Point(20.0f, 15.0f));
        this.mDarkLinePointsList.add(new Point(85.0f, 80.0f));
        this.mDarkLinePointsList.add(new Point(100.0f, 80.0f));
        this.mDarkLinePointsList.add(new Point(420.0f, 400.0f));
        this.mDarkLinePointsList.add(new Point(500.0f, 400.0f));
        this.mDarkLinePointsList.add(new Point(600.0f, 500.0f));
        this.mDarkLinePointsList.add(new Point(1000.0f, 500.0f));
        this.mDarkLinePointsList.add(new Point(2000.0f, 1000.0f));
        this.mDarkLinePointsList.add(new Point(40000.0f, 1000.0f));
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
        boolean lightSensorRateMillsLoaded = false;
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
            for (int eventType = parser.getEventType(); eventType != APICAL_INDOOR_UI; eventType = parser.next()) {
                switch (eventType) {
                    case APICAL_OUTDOOR_UI /*2*/:
                        String name = parser.getName();
                        if (name == null || name.length() == 0) {
                            return false;
                        }
                        if (name.equals("LightSensorRateMills")) {
                            this.mLightSensorRateMillis = Integer.parseInt(parser.nextText());
                            lightSensorRateMillsLoaded = true;
                            Slog.i(TAG, "LightSensorRateMills = " + this.mLightSensorRateMillis + "ms");
                            break;
                        }
                        if (name.equals("OutDoorThreshold")) {
                            this.mOutDoorThreshold = Integer.parseInt(parser.nextText());
                            outDoorThresholdLoaded = true;
                            Slog.i(TAG, "OutDoorThreshold = " + this.mOutDoorThreshold);
                            break;
                        }
                        if (name.equals("InDoorThreshold")) {
                            this.mInDoorThreshold = Integer.parseInt(parser.nextText());
                            inDoorThresholdLoaded = true;
                            Slog.i(TAG, "InDoorThreshold = " + this.mInDoorThreshold);
                            break;
                        }
                        if (name.equals("BrighenDebounceTime")) {
                            this.mBrighenDebounceTime = Integer.parseInt(parser.nextText());
                            brighenDebounceTimeLoaded = true;
                            Slog.i(TAG, "BrighenDebounceTime = " + this.mBrighenDebounceTime);
                            break;
                        }
                        if (name.equals("DarkenDebounceTime")) {
                            this.mDarkenDebounceTime = Integer.parseInt(parser.nextText());
                            darkenDebounceTimeLoaded = true;
                            Slog.i(TAG, "DarkenDebounceTime = " + this.mDarkenDebounceTime);
                            break;
                        }
                        if (name.equals("BrightenLinePoints")) {
                            brightenLinePointsLoadStarted = true;
                            break;
                        }
                        Point currentPoint;
                        String s;
                        String[] pointSplited;
                        int length;
                        if (name.equals("Point") && brightenLinePointsLoadStarted) {
                            currentPoint = new Point();
                            s = parser.nextText();
                            pointSplited = s.split(",");
                            if (pointSplited != null) {
                                length = pointSplited.length;
                                if (r0 >= APICAL_OUTDOOR_UI) {
                                    currentPoint.x = Float.parseFloat(pointSplited[0]);
                                    currentPoint.y = Float.parseFloat(pointSplited[APICAL_INDOOR_UI]);
                                    if (this.mBrightenLinePointsList == null) {
                                        this.mBrightenLinePointsList = new ArrayList();
                                    }
                                    this.mBrightenLinePointsList.add(currentPoint);
                                    break;
                                }
                            }
                            Slog.e(TAG, "split failed! s = " + s);
                            return false;
                        }
                        if (name.equals("DarkenLinePoints")) {
                            darkenLinePointsLoadStarted = true;
                            break;
                        }
                        if (name.equals("Point") && darkenLinePointsLoadStarted) {
                            currentPoint = new Point();
                            s = parser.nextText();
                            pointSplited = s.split(",");
                            if (pointSplited != null) {
                                length = pointSplited.length;
                                if (r0 >= APICAL_OUTDOOR_UI) {
                                    currentPoint.x = Float.parseFloat(pointSplited[0]);
                                    currentPoint.y = Float.parseFloat(pointSplited[APICAL_INDOOR_UI]);
                                    if (this.mDarkLinePointsList == null) {
                                        this.mDarkLinePointsList = new ArrayList();
                                    }
                                    this.mDarkLinePointsList.add(currentPoint);
                                    break;
                                }
                            }
                            Slog.e(TAG, "split failed! s = " + s);
                            return false;
                        }
                        break;
                    case WifiProCommonDefs.WIFI_SECURITY_PHISHING_FAILED /*3*/:
                        if (parser.getName().equals("BrightenLinePoints")) {
                            brightenLinePointsLoadStarted = false;
                            if (this.mBrightenLinePointsList != null) {
                                brightenLinePointsLoaded = true;
                                break;
                            }
                            Slog.e(TAG, "no BrightenLinePoints loaded!");
                            return false;
                        } else if (parser.getName().equals("DarkenLinePoints")) {
                            darkenLinePointsLoadStarted = false;
                            if (this.mDarkLinePointsList != null) {
                                darkenLinePointsLoaded = true;
                                break;
                            }
                            Slog.e(TAG, "no DarkenLinePoints loaded!");
                            return false;
                        } else {
                            continue;
                        }
                    default:
                        break;
                }
            }
            if (lightSensorRateMillsLoaded && inDoorThresholdLoaded && outDoorThresholdLoaded && brighenDebounceTimeLoaded && darkenDebounceTimeLoaded && brightenLinePointsLoaded && darkenLinePointsLoaded) {
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
            if (DEBUG) {
                Slog.i(TAG, "lux >= max, lux=" + lux);
            }
            lux = 40000.0f;
        }
        applyLightSensorMeasurement(time, lux);
        updateAmbientLux(time);
    }

    private void applyLightSensorMeasurement(long time, float lux) {
        this.mAmbientLightRingBuffer.prune(time - MemoryConstant.MIN_INTERVAL_OP_TIMEOUT);
        this.mAmbientLightRingBuffer.push(time, lux);
    }

    protected float calculateAmbientLuxForNewPolicy(long now) {
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

    protected long nextAmbientLightBrighteningTransitionForNewPolicy(long time) {
        int N = this.mAmbientLightRingBufferFilter.size();
        long debounceTime = (long) this.mBrighenDebounceTime;
        if (APICAL_INDOOR_UI == N) {
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

    protected long nextAmbientLightDarkeningTransitionForNewPolicy(long time) {
        int N = this.mAmbientLightRingBufferFilter.size();
        long debounceTime = (long) this.mDarkenDebounceTime;
        if (APICAL_INDOOR_UI == N) {
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

    private long nextAmbientLightBrighteningTransitionForOldPolicy(long time) {
        int N = this.mAmbientLightRingBufferFilter.size();
        long debounceTime = (long) this.mBrighenDebounceTime;
        if (APICAL_INDOOR_UI == N) {
            debounceTime = 0;
        }
        long earliestValidTime = time;
        for (int i = N - 1; i >= 0; i--) {
            boolean BrightenChange;
            if (this.mAmbientLightRingBufferFilter.getLux(i) > ((float) this.mOutDoorThreshold)) {
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

    private long nextAmbientLightDarkeningTransitionForOldPolicy(long time) {
        int N = this.mAmbientLightRingBufferFilter.size();
        long debounceTime = (long) this.mDarkenDebounceTime;
        if (APICAL_INDOOR_UI == N) {
            debounceTime = 0;
        }
        long earliestValidTime = time;
        for (int i = N - 1; i >= 0; i--) {
            boolean DarkenChange;
            if (this.mAmbientLightRingBufferFilter.getLux(i) < ((float) this.mInDoorThreshold)) {
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
        if (APICAL_INDOOR_UI == N) {
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

    public void setBrightenThresholdNew() {
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
            count += APICAL_INDOOR_UI;
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
            count += APICAL_INDOOR_UI;
        }
    }

    protected void updateAmbientLux(long time) {
        float value = calculateAmbientLuxForNewPolicy(time);
        if (this.mAmbientLightRingBuffer.size() == APICAL_INDOOR_UI) {
            Slog.i(TAG, "fist sensor lux and filteredlux=" + value + ",time=" + time);
        }
        this.mAmbientLightRingBufferFilter.push(time, value);
        this.mAmbientLightRingBufferFilter.prune(time - MemoryConstant.MIN_INTERVAL_OP_TIMEOUT);
        long nextBrightenTransition = nextAmbientLightBrighteningTransitionForNewPolicy(time);
        long nextDarkenTransition = nextAmbientLightDarkeningTransitionForNewPolicy(time);
        long nextBrightenTransitionOld = nextAmbientLightBrighteningTransitionForOldPolicy(time);
        long nextDarkenTransitionOld = nextAmbientLightDarkeningTransitionForOldPolicy(time);
        long nextInOutTransition = nextAmbientLightDarkeningTransitionForInOut(time);
        this.mAmChangeFlagSBL = false;
        boolean updateFlag = ((nextBrightenTransition > time || nextDarkenTransitionOld > time) && ((nextDarkenTransition > time || nextBrightenTransitionOld > time) && ((nextBrightenTransition > time || nextInOutTransition > time) && ((nextDarkenTransition > time || nextInOutTransition > time) && (nextBrightenTransitionOld > time || !this.mInoutFlag))))) ? nextDarkenTransitionOld <= time ? this.mInoutFlag : false : true;
        if ((nextBrightenTransition > time || nextBrightenTransitionOld > time) && (nextDarkenTransition > time || nextDarkenTransitionOld > time)) {
            if (!updateFlag) {
                return;
            }
        }
        if (DEBUG) {
            Slog.i(TAG, "updateSBL_Flag=" + updateFlag + ",filteredlux=" + value + ",time=" + time + ",nextBTime=" + nextBrightenTransition + ",nextDTime=" + nextDarkenTransition + ",nextBTimeOld=" + nextBrightenTransitionOld + ",nextDTimeOld=" + nextDarkenTransitionOld + ",nextInOutTime=" + nextInOutTransition);
        }
        updateParaForSBL(value);
    }

    protected void updateParaForSBL(float lux) {
        float mAmbientLuxTmp = lux;
        if (lux >= ((float) this.mOutDoorThreshold)) {
            this.mAmbientLux = lux;
            this.mLastApicalFlag = APICAL_OUTDOOR_UI;
            this.mAmChangeFlagSBL = true;
            this.mInoutFlag = false;
        }
        if (lux < ((float) this.mInDoorThreshold)) {
            this.mAmbientLux = lux;
            this.mLastApicalFlag = APICAL_INDOOR_UI;
            this.mAmChangeFlagSBL = true;
            this.mInoutFlag = false;
        }
        if (lux < ((float) this.mOutDoorThreshold) && lux >= ((float) this.mInDoorThreshold)) {
            this.mAmbientLux = lux;
            if (this.mAmbientLightRingBufferFilter.size() == APICAL_INDOOR_UI) {
                this.mLastApicalFlag = APICAL_INDOOR_UI;
            }
            this.mAmChangeFlagSBL = true;
            this.mInoutFlag = true;
        }
        setBrightenThresholdNew();
        setDarkenThresholdNew();
        if (DEBUG) {
            Slog.i(TAG, "updateSBL_lux =" + this.mAmbientLux + ",IN_OUT_DoorFlag =" + this.mLastApicalFlag + ",mBrightenDeltaLuxMax=" + this.mBrightenDeltaLuxMax + ",mDarkenDeltaLuxMax=" + this.mDarkenDeltaLuxMax);
        }
    }

    public float getAmbientLuxForSBL() {
        return this.mAmbientLux;
    }

    public boolean getLuxChangedFlagForSBL() {
        return this.mAmChangeFlagSBL;
    }

    public int getIndoorOutdoorFlagForSBL() {
        return this.mLastApicalFlag;
    }
}
