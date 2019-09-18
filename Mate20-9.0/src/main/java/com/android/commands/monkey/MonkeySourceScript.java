package com.android.commands.monkey;

import android.content.ComponentName;
import android.os.SystemClock;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.NoSuchElementException;
import java.util.Random;

public class MonkeySourceScript implements MonkeyEventSource {
    private static final String EVENT_KEYWORD_ACTIVITY = "LaunchActivity";
    private static final String EVENT_KEYWORD_DEVICE_WAKEUP = "DeviceWakeUp";
    private static final String EVENT_KEYWORD_DRAG = "Drag";
    private static final String EVENT_KEYWORD_END_APP_FRAMERATE_CAPTURE = "EndCaptureAppFramerate";
    private static final String EVENT_KEYWORD_END_FRAMERATE_CAPTURE = "EndCaptureFramerate";
    private static final String EVENT_KEYWORD_FLIP = "DispatchFlip";
    private static final String EVENT_KEYWORD_INPUT_STRING = "DispatchString";
    private static final String EVENT_KEYWORD_INSTRUMENTATION = "LaunchInstrumentation";
    private static final String EVENT_KEYWORD_KEY = "DispatchKey";
    private static final String EVENT_KEYWORD_KEYPRESS = "DispatchPress";
    private static final String EVENT_KEYWORD_LONGPRESS = "LongPress";
    private static final String EVENT_KEYWORD_PINCH_ZOOM = "PinchZoom";
    private static final String EVENT_KEYWORD_POINTER = "DispatchPointer";
    private static final String EVENT_KEYWORD_POWERLOG = "PowerLog";
    private static final String EVENT_KEYWORD_PRESSANDHOLD = "PressAndHold";
    private static final String EVENT_KEYWORD_PROFILE_WAIT = "ProfileWait";
    private static final String EVENT_KEYWORD_ROTATION = "RotateScreen";
    private static final String EVENT_KEYWORD_RUNCMD = "RunCmd";
    private static final String EVENT_KEYWORD_START_APP_FRAMERATE_CAPTURE = "StartCaptureAppFramerate";
    private static final String EVENT_KEYWORD_START_FRAMERATE_CAPTURE = "StartCaptureFramerate";
    private static final String EVENT_KEYWORD_TAP = "Tap";
    private static final String EVENT_KEYWORD_TRACKBALL = "DispatchTrackball";
    private static final String EVENT_KEYWORD_WAIT = "UserWait";
    private static final String EVENT_KEYWORD_WRITEPOWERLOG = "WriteLog";
    private static final String HEADER_COUNT = "count=";
    private static final String HEADER_LINE_BY_LINE = "linebyline";
    private static final String HEADER_SPEED = "speed=";
    private static int LONGPRESS_WAIT_TIME = 2000;
    private static final int MAX_ONE_TIME_READS = 100;
    private static final long SLEEP_COMPENSATE_DIFF = 16;
    private static final String STARTING_DATA_LINE = "start data >>";
    private static final boolean THIS_DEBUG = false;
    BufferedReader mBufferedReader;
    private long mDeviceSleepTime = 30000;
    private int mEventCountInScript = 0;
    FileInputStream mFStream;
    private boolean mFileOpened = THIS_DEBUG;
    DataInputStream mInputStream;
    private long mLastExportDownTimeKey = 0;
    private long mLastExportDownTimeMotion = 0;
    private long mLastExportEventTime = -1;
    private long mLastRecordedDownTimeKey = 0;
    private long mLastRecordedDownTimeMotion = 0;
    private long mLastRecordedEventTime = -1;
    private float[] mLastX = new float[2];
    private float[] mLastY = new float[2];
    private long mMonkeyStartTime = -1;
    private long mProfileWaitTime = 5000;
    private MonkeyEventQueue mQ;
    private boolean mReadScriptLineByLine = THIS_DEBUG;
    private String mScriptFileName;
    private long mScriptStartTime = -1;
    private double mSpeed = 1.0d;
    private int mVerbose = 0;

    public MonkeySourceScript(Random random, String filename, long throttle, boolean randomizeThrottle, long profileWaitTime, long deviceSleepTime) {
        this.mScriptFileName = filename;
        this.mQ = new MonkeyEventQueue(random, throttle, randomizeThrottle);
        this.mProfileWaitTime = profileWaitTime;
        this.mDeviceSleepTime = deviceSleepTime;
    }

    private void resetValue() {
        this.mLastRecordedDownTimeKey = 0;
        this.mLastRecordedDownTimeMotion = 0;
        this.mLastRecordedEventTime = -1;
        this.mLastExportDownTimeKey = 0;
        this.mLastExportDownTimeMotion = 0;
        this.mLastExportEventTime = -1;
    }

    private boolean readHeader() throws IOException {
        this.mFileOpened = true;
        this.mFStream = new FileInputStream(this.mScriptFileName);
        this.mInputStream = new DataInputStream(this.mFStream);
        this.mBufferedReader = new BufferedReader(new InputStreamReader(this.mInputStream));
        while (true) {
            String readLine = this.mBufferedReader.readLine();
            String line = readLine;
            if (readLine == null) {
                return THIS_DEBUG;
            }
            String line2 = line.trim();
            if (line2.indexOf(HEADER_COUNT) >= 0) {
                try {
                    this.mEventCountInScript = Integer.parseInt(line2.substring(HEADER_COUNT.length() + 1).trim());
                } catch (NumberFormatException e) {
                    Logger logger = Logger.err;
                    logger.println("" + e);
                    return THIS_DEBUG;
                }
            } else if (line2.indexOf(HEADER_SPEED) >= 0) {
                try {
                    this.mSpeed = Double.parseDouble(line2.substring(HEADER_COUNT.length() + 1).trim());
                } catch (NumberFormatException e2) {
                    Logger logger2 = Logger.err;
                    logger2.println("" + e2);
                    return THIS_DEBUG;
                }
            } else if (line2.indexOf(HEADER_LINE_BY_LINE) >= 0) {
                this.mReadScriptLineByLine = true;
            } else if (line2.indexOf(STARTING_DATA_LINE) >= 0) {
                return true;
            }
        }
    }

    private int readLines() throws IOException {
        for (int i = 0; i < MAX_ONE_TIME_READS; i++) {
            String line = this.mBufferedReader.readLine();
            if (line == null) {
                return i;
            }
            line.trim();
            processLine(line);
        }
        return MAX_ONE_TIME_READS;
    }

    private int readOneLine() throws IOException {
        String line = this.mBufferedReader.readLine();
        if (line == null) {
            return 0;
        }
        line.trim();
        processLine(line);
        return 1;
    }

    private void handleEvent(String s, String[] args) {
        String[] strArr;
        String[] strArr2;
        long eventTime;
        MonkeyMotionEvent e;
        MonkeyMotionEvent e2;
        String str = s;
        String[] strArr3 = args;
        if (str.indexOf(EVENT_KEYWORD_KEY) >= 0 && strArr3.length == 8) {
            try {
                Logger.out.println(" old key\n");
                long downTime = Long.parseLong(strArr3[0]);
                long eventTime2 = Long.parseLong(strArr3[1]);
                int action = Integer.parseInt(strArr3[2]);
                int code = Integer.parseInt(strArr3[3]);
                MonkeyKeyEvent monkeyKeyEvent = new MonkeyKeyEvent(downTime, eventTime2, action, code, Integer.parseInt(strArr3[4]), Integer.parseInt(strArr3[5]), Integer.parseInt(strArr3[6]), Integer.parseInt(strArr3[7]));
                Logger logger = Logger.out;
                logger.println(" Key code " + code + "\n");
                this.mQ.addLast((MonkeyEvent) monkeyKeyEvent);
                Logger.out.println("Added key up \n");
            } catch (NumberFormatException e3) {
            }
        } else if ((str.indexOf(EVENT_KEYWORD_POINTER) >= 0 || str.indexOf(EVENT_KEYWORD_TRACKBALL) >= 0) && strArr3.length == 12) {
            try {
                long downTime2 = Long.parseLong(strArr3[0]);
                long eventTime3 = Long.parseLong(strArr3[1]);
                int action2 = Integer.parseInt(strArr3[2]);
                float x = Float.parseFloat(strArr3[3]);
                float y = Float.parseFloat(strArr3[4]);
                float pressure = Float.parseFloat(strArr3[5]);
                float size = Float.parseFloat(strArr3[6]);
                int metaState = Integer.parseInt(strArr3[7]);
                float xPrecision = Float.parseFloat(strArr3[8]);
                float yPrecision = Float.parseFloat(strArr3[9]);
                int device = Integer.parseInt(strArr3[10]);
                int edgeFlags = Integer.parseInt(strArr3[11]);
                if (str.indexOf("Pointer") > 0) {
                    e2 = new MonkeyTouchEvent(action2);
                } else {
                    e2 = new MonkeyTrackballEvent(action2);
                }
                e2.setDownTime(downTime2).setEventTime(eventTime3).setMetaState(metaState).setPrecision(xPrecision, yPrecision).setDeviceId(device).setEdgeFlags(edgeFlags).addPointer(0, x, y, pressure, size);
                this.mQ.addLast((MonkeyEvent) e2);
            } catch (NumberFormatException e4) {
            }
        } else if ((str.indexOf(EVENT_KEYWORD_POINTER) >= 0 || str.indexOf(EVENT_KEYWORD_TRACKBALL) >= 0) && strArr3.length == 13) {
            try {
                long downTime3 = Long.parseLong(strArr3[0]);
                long eventTime4 = Long.parseLong(strArr3[1]);
                int action3 = Integer.parseInt(strArr3[2]);
                float x2 = Float.parseFloat(strArr3[3]);
                float y2 = Float.parseFloat(strArr3[4]);
                float pressure2 = Float.parseFloat(strArr3[5]);
                float size2 = Float.parseFloat(strArr3[6]);
                int metaState2 = Integer.parseInt(strArr3[7]);
                float xPrecision2 = Float.parseFloat(strArr3[8]);
                float yPrecision2 = Float.parseFloat(strArr3[9]);
                int device2 = Integer.parseInt(strArr3[10]);
                int edgeFlags2 = Integer.parseInt(strArr3[11]);
                int pointerId = Integer.parseInt(strArr3[12]);
                if (str.indexOf("Pointer") > 0) {
                    if (action3 == 5) {
                        e = new MonkeyTouchEvent(5 | (pointerId << 8)).setIntermediateNote(true);
                    } else {
                        e = new MonkeyTouchEvent(action3);
                    }
                    if (this.mScriptStartTime < 0) {
                        this.mMonkeyStartTime = SystemClock.uptimeMillis();
                        eventTime = eventTime4;
                        this.mScriptStartTime = eventTime;
                    } else {
                        eventTime = eventTime4;
                    }
                } else {
                    eventTime = eventTime4;
                    e = new MonkeyTrackballEvent(action3);
                }
                if (pointerId == 1) {
                    long downTime4 = downTime3;
                    e.setDownTime(downTime4).setEventTime(eventTime).setMetaState(metaState2).setPrecision(xPrecision2, yPrecision2).setDeviceId(device2).setEdgeFlags(edgeFlags2).addPointer(0, this.mLastX[0], this.mLastY[0], pressure2, size2).addPointer(1, x2, y2, pressure2, size2);
                    this.mLastX[1] = x2;
                    this.mLastY[1] = y2;
                    int i = action3;
                    long j = downTime4;
                } else {
                    long downTime5 = downTime3;
                    if (pointerId == 0) {
                        e.setDownTime(downTime5).setEventTime(eventTime).setMetaState(metaState2).setPrecision(xPrecision2, yPrecision2).setDeviceId(device2).setEdgeFlags(edgeFlags2).addPointer(0, x2, y2, pressure2, size2);
                        if (action3 == 6) {
                            int i2 = action3;
                            long j2 = downTime5;
                            e.addPointer(1, this.mLastX[1], this.mLastY[1]);
                        } else {
                            long j3 = downTime5;
                        }
                        this.mLastX[0] = x2;
                        this.mLastY[0] = y2;
                    } else {
                        long j4 = downTime5;
                    }
                }
                if (this.mReadScriptLineByLine != 0) {
                    long curUpTime = SystemClock.uptimeMillis();
                    float f = xPrecision2;
                    int i3 = metaState2;
                    long realElapsedTime = curUpTime - this.mMonkeyStartTime;
                    long j5 = curUpTime;
                    long scriptElapsedTime = eventTime - this.mScriptStartTime;
                    if (realElapsedTime < scriptElapsedTime) {
                        this.mQ.addLast((MonkeyEvent) new MonkeyWaitEvent(scriptElapsedTime - realElapsedTime));
                    }
                } else {
                    int i4 = metaState2;
                }
                this.mQ.addLast((MonkeyEvent) e);
            } catch (NumberFormatException e5) {
            }
        } else {
            String str2 = s;
            if (str2.indexOf(EVENT_KEYWORD_ROTATION) >= 0) {
                strArr = args;
                if (strArr.length == 2) {
                    try {
                        int rotationDegree = Integer.parseInt(strArr[0]);
                        int persist = Integer.parseInt(strArr[1]);
                        if (rotationDegree == 0 || rotationDegree == 1 || rotationDegree == 2 || rotationDegree == 3) {
                            this.mQ.addLast((MonkeyEvent) new MonkeyRotationEvent(rotationDegree, persist != 0 ? true : THIS_DEBUG));
                        }
                    } catch (NumberFormatException e6) {
                    }
                    return;
                }
            } else {
                strArr = args;
            }
            if (str2.indexOf(EVENT_KEYWORD_TAP) >= 0 && strArr.length >= 2) {
                try {
                    float x3 = Float.parseFloat(strArr[0]);
                    float y3 = Float.parseFloat(strArr[1]);
                    long tapDuration = 0;
                    if (strArr.length == 3) {
                        tapDuration = Long.parseLong(strArr[2]);
                    }
                    long downTime6 = SystemClock.uptimeMillis();
                    this.mQ.addLast((MonkeyEvent) new MonkeyTouchEvent(0).setDownTime(downTime6).setEventTime(downTime6).addPointer(0, x3, y3, 1.0f, 5.0f));
                    if (tapDuration > 0) {
                        this.mQ.addLast((MonkeyEvent) new MonkeyWaitEvent(tapDuration));
                    }
                    this.mQ.addLast((MonkeyEvent) new MonkeyTouchEvent(1).setDownTime(downTime6).setEventTime(downTime6).addPointer(0, x3, y3, 1.0f, 5.0f));
                } catch (NumberFormatException e7) {
                    Logger logger2 = Logger.err;
                    logger2.println("// " + e7.toString());
                }
            } else if (str2.indexOf(EVENT_KEYWORD_PRESSANDHOLD) < 0 || strArr.length != 3) {
                if (str2.indexOf(EVENT_KEYWORD_DRAG) >= 0 && strArr.length == 5) {
                    float xStart = Float.parseFloat(strArr[0]);
                    float yStart = Float.parseFloat(strArr[1]);
                    float xEnd = Float.parseFloat(strArr[2]);
                    float yEnd = Float.parseFloat(strArr[3]);
                    int stepCount = Integer.parseInt(strArr[4]);
                    float x4 = xStart;
                    float y4 = yStart;
                    long downTime7 = SystemClock.uptimeMillis();
                    long eventTime5 = SystemClock.uptimeMillis();
                    if (stepCount > 0) {
                        float xStep = (xEnd - xStart) / ((float) stepCount);
                        float yStep = (yEnd - yStart) / ((float) stepCount);
                        float f2 = xStart;
                        int stepCount2 = stepCount;
                        MonkeyMotionEvent e8 = new MonkeyTouchEvent(0).setDownTime(downTime7).setEventTime(eventTime5).addPointer(0, x4, y4, 1.0f, 5.0f);
                        this.mQ.addLast((MonkeyEvent) e8);
                        long j6 = eventTime5;
                        MonkeyMotionEvent monkeyMotionEvent = e8;
                        float x5 = x4;
                        float y5 = y4;
                        int i5 = 0;
                        while (i5 < stepCount2) {
                            x5 += xStep;
                            y5 += yStep;
                            int stepCount3 = stepCount2;
                            MonkeyMotionEvent e9 = new MonkeyTouchEvent(2).setDownTime(downTime7).setEventTime(SystemClock.uptimeMillis()).addPointer(0, x5, y5, 1.0f, 5.0f);
                            this.mQ.addLast((MonkeyEvent) e9);
                            i5++;
                            MonkeyMotionEvent monkeyMotionEvent2 = e9;
                            stepCount2 = stepCount3;
                            yStart = yStart;
                        }
                        float f3 = yStart;
                        this.mQ.addLast((MonkeyEvent) new MonkeyTouchEvent(1).setDownTime(downTime7).setEventTime(SystemClock.uptimeMillis()).addPointer(0, x5, y5, 1.0f, 5.0f));
                    }
                }
                if (str2.indexOf(EVENT_KEYWORD_PINCH_ZOOM) >= 0 && strArr.length == 9) {
                    float pt1xStart = Float.parseFloat(strArr[0]);
                    float pt1yStart = Float.parseFloat(strArr[1]);
                    float pt1xEnd = Float.parseFloat(strArr[2]);
                    float pt1yEnd = Float.parseFloat(strArr[3]);
                    float pt2xStart = Float.parseFloat(strArr[4]);
                    float pt2yStart = Float.parseFloat(strArr[5]);
                    float pt2xEnd = Float.parseFloat(strArr[6]);
                    float pt2yEnd = Float.parseFloat(strArr[7]);
                    int stepCount4 = Integer.parseInt(strArr[8]);
                    float x1 = pt1xStart;
                    float y22 = pt2yStart;
                    float x22 = pt2xStart;
                    float y1 = pt1yStart;
                    long downTime8 = SystemClock.uptimeMillis();
                    long eventTime6 = SystemClock.uptimeMillis();
                    if (stepCount4 > 0) {
                        float f4 = pt1xStart;
                        float pt1xStep = (pt1xEnd - pt1xStart) / ((float) stepCount4);
                        float f5 = pt1yStart;
                        float pt1yStep = (pt1yEnd - pt1yStart) / ((float) stepCount4);
                        float f6 = pt1xEnd;
                        float pt2xStep = (pt2xEnd - pt2xStart) / ((float) stepCount4);
                        float f7 = pt1yEnd;
                        float f8 = pt2xStart;
                        MonkeyEventQueue monkeyEventQueue = this.mQ;
                        float f9 = pt2yStart;
                        float f10 = pt2xEnd;
                        MonkeyMotionEvent eventTime7 = new MonkeyTouchEvent(0).setDownTime(downTime8).setEventTime(eventTime6);
                        long j7 = eventTime6;
                        float y23 = y22;
                        MonkeyMotionEvent monkeyMotionEvent3 = eventTime7;
                        float pt2xStep2 = pt2xStep;
                        float pt2yStep = (pt2yEnd - pt2yStart) / ((float) stepCount4);
                        long downTime9 = downTime8;
                        float x23 = x22;
                        float y12 = y1;
                        float x12 = x1;
                        monkeyEventQueue.addLast((MonkeyEvent) monkeyMotionEvent3.addPointer(0, x1, y12, 1.0f, 5.0f));
                        this.mQ.addLast((MonkeyEvent) new MonkeyTouchEvent(261).setDownTime(downTime9).addPointer(0, x12, y12).addPointer(1, x23, y23).setIntermediateNote(true));
                        float y24 = y23;
                        int i6 = 0;
                        while (i6 < stepCount4) {
                            x12 += pt1xStep;
                            y12 += pt1yStep;
                            x23 += pt2xStep2;
                            y24 += pt2yStep;
                            long eventTime8 = SystemClock.uptimeMillis();
                            this.mQ.addLast((MonkeyEvent) new MonkeyTouchEvent(2).setDownTime(downTime9).setEventTime(eventTime8).addPointer(0, x12, y12, 1.0f, 5.0f).addPointer(1, x23, y24, 1.0f, 5.0f));
                            i6++;
                            long j8 = eventTime8;
                            pt1xStep = pt1xStep;
                            pt1yStep = pt1yStep;
                        }
                        float f11 = pt1yStep;
                        this.mQ.addLast((MonkeyEvent) new MonkeyTouchEvent(6).setDownTime(downTime9).setEventTime(SystemClock.uptimeMillis()).addPointer(0, x12, y12).addPointer(1, x23, y24));
                    }
                }
                String str3 = s;
                if (str3.indexOf(EVENT_KEYWORD_FLIP) >= 0) {
                    strArr2 = args;
                    if (strArr2.length == 1) {
                        this.mQ.addLast((MonkeyEvent) new MonkeyFlipEvent(Boolean.parseBoolean(strArr2[0])));
                    }
                } else {
                    strArr2 = args;
                }
                if (str3.indexOf(EVENT_KEYWORD_ACTIVITY) >= 0 && strArr2.length >= 2) {
                    long alarmTime = 0;
                    ComponentName mApp = new ComponentName(strArr2[0], strArr2[1]);
                    if (strArr2.length > 2) {
                        try {
                            alarmTime = Long.parseLong(strArr2[2]);
                        } catch (NumberFormatException e10) {
                            Logger logger3 = Logger.err;
                            logger3.println("// " + e10.toString());
                            return;
                        }
                    }
                    if (strArr2.length == 2) {
                        this.mQ.addLast((MonkeyEvent) new MonkeyActivityEvent(mApp));
                    } else {
                        this.mQ.addLast((MonkeyEvent) new MonkeyActivityEvent(mApp, alarmTime));
                    }
                } else if (str3.indexOf(EVENT_KEYWORD_DEVICE_WAKEUP) >= 0) {
                    long deviceSleepTime = this.mDeviceSleepTime;
                    this.mQ.addLast((MonkeyEvent) new MonkeyActivityEvent(new ComponentName("com.google.android.powerutil", "com.google.android.powerutil.WakeUpScreen"), deviceSleepTime));
                    this.mQ.addLast((MonkeyEvent) new MonkeyKeyEvent(0, 7));
                    this.mQ.addLast((MonkeyEvent) new MonkeyKeyEvent(1, 7));
                    this.mQ.addLast((MonkeyEvent) new MonkeyWaitEvent(3000 + deviceSleepTime));
                    this.mQ.addLast((MonkeyEvent) new MonkeyKeyEvent(0, 82));
                    this.mQ.addLast((MonkeyEvent) new MonkeyKeyEvent(1, 82));
                    this.mQ.addLast((MonkeyEvent) new MonkeyKeyEvent(0, 4));
                    this.mQ.addLast((MonkeyEvent) new MonkeyKeyEvent(1, 4));
                } else if (str3.indexOf(EVENT_KEYWORD_INSTRUMENTATION) >= 0 && strArr2.length == 2) {
                    this.mQ.addLast((MonkeyEvent) new MonkeyInstrumentationEvent(strArr2[0], strArr2[1]));
                } else if (str3.indexOf(EVENT_KEYWORD_WAIT) >= 0 && strArr2.length == 1) {
                    try {
                        this.mQ.addLast((MonkeyEvent) new MonkeyWaitEvent((long) Integer.parseInt(strArr2[0])));
                    } catch (NumberFormatException e11) {
                    }
                } else if (str3.indexOf(EVENT_KEYWORD_PROFILE_WAIT) >= 0) {
                    this.mQ.addLast((MonkeyEvent) new MonkeyWaitEvent(this.mProfileWaitTime));
                } else if (str3.indexOf(EVENT_KEYWORD_KEYPRESS) < 0 || strArr2.length != 1) {
                    if (str3.indexOf(EVENT_KEYWORD_LONGPRESS) >= 0) {
                        this.mQ.addLast((MonkeyEvent) new MonkeyKeyEvent(0, 23));
                        this.mQ.addLast((MonkeyEvent) new MonkeyWaitEvent((long) LONGPRESS_WAIT_TIME));
                        this.mQ.addLast((MonkeyEvent) new MonkeyKeyEvent(1, 23));
                    }
                    if (str3.indexOf(EVENT_KEYWORD_POWERLOG) >= 0 && strArr2.length > 0) {
                        String power_log_type = strArr2[0];
                        if (strArr2.length == 1) {
                            this.mQ.addLast((MonkeyEvent) new MonkeyPowerEvent(power_log_type));
                        } else if (strArr2.length == 2) {
                            this.mQ.addLast((MonkeyEvent) new MonkeyPowerEvent(power_log_type, strArr2[1]));
                        }
                    }
                    if (str3.indexOf(EVENT_KEYWORD_WRITEPOWERLOG) >= 0) {
                        this.mQ.addLast((MonkeyEvent) new MonkeyPowerEvent());
                    }
                    if (str3.indexOf(EVENT_KEYWORD_RUNCMD) >= 0 && strArr2.length == 1) {
                        this.mQ.addLast((MonkeyEvent) new MonkeyCommandEvent(strArr2[0]));
                    }
                    if (str3.indexOf(EVENT_KEYWORD_INPUT_STRING) >= 0 && strArr2.length == 1) {
                        String input = strArr2[0];
                        this.mQ.addLast((MonkeyEvent) new MonkeyCommandEvent("input text " + input));
                    } else if (str3.indexOf(EVENT_KEYWORD_START_FRAMERATE_CAPTURE) >= 0) {
                        this.mQ.addLast((MonkeyEvent) new MonkeyGetFrameRateEvent("start"));
                    } else if (str3.indexOf(EVENT_KEYWORD_END_FRAMERATE_CAPTURE) >= 0 && strArr2.length == 1) {
                        this.mQ.addLast((MonkeyEvent) new MonkeyGetFrameRateEvent("end", strArr2[0]));
                    } else if (str3.indexOf(EVENT_KEYWORD_START_APP_FRAMERATE_CAPTURE) >= 0 && strArr2.length == 1) {
                        this.mQ.addLast((MonkeyEvent) new MonkeyGetAppFrameRateEvent("start", strArr2[0]));
                    } else if (str3.indexOf(EVENT_KEYWORD_END_APP_FRAMERATE_CAPTURE) >= 0 && strArr2.length == 2) {
                        this.mQ.addLast((MonkeyEvent) new MonkeyGetAppFrameRateEvent("end", strArr2[0], strArr2[1]));
                    }
                } else {
                    int keyCode = MonkeySourceRandom.getKeyCode(strArr2[0]);
                    if (keyCode != 0) {
                        this.mQ.addLast((MonkeyEvent) new MonkeyKeyEvent(0, keyCode));
                        this.mQ.addLast((MonkeyEvent) new MonkeyKeyEvent(1, keyCode));
                    }
                }
            } else {
                try {
                    float x6 = Float.parseFloat(strArr[0]);
                    float y6 = Float.parseFloat(strArr[1]);
                    long pressDuration = Long.parseLong(strArr[2]);
                    long downTime10 = SystemClock.uptimeMillis();
                    MonkeyMotionEvent e1 = new MonkeyTouchEvent(0).setDownTime(downTime10).setEventTime(downTime10).addPointer(0, x6, y6, 1.0f, 5.0f);
                    MonkeyWaitEvent e22 = new MonkeyWaitEvent(pressDuration);
                    MonkeyMotionEvent addPointer = new MonkeyTouchEvent(1).setDownTime(downTime10 + pressDuration).setEventTime(downTime10 + pressDuration).addPointer(0, x6, y6, 1.0f, 5.0f);
                    this.mQ.addLast((MonkeyEvent) e1);
                    this.mQ.addLast((MonkeyEvent) e22);
                    this.mQ.addLast((MonkeyEvent) e22);
                } catch (NumberFormatException e12) {
                    Logger logger4 = Logger.err;
                    logger4.println("// " + e12.toString());
                }
            }
        }
    }

    private void processLine(String line) {
        int index1 = line.indexOf(40);
        int index2 = line.indexOf(41);
        if (index1 >= 0 && index2 >= 0) {
            String[] args = line.substring(index1 + 1, index2).split(",");
            for (int i = 0; i < args.length; i++) {
                args[i] = args[i].trim();
            }
            handleEvent(line, args);
        }
    }

    private void closeFile() throws IOException {
        this.mFileOpened = THIS_DEBUG;
        try {
            this.mFStream.close();
            this.mInputStream.close();
        } catch (NullPointerException e) {
        }
    }

    private void readNextBatch() throws IOException {
        int linesRead;
        if (!this.mFileOpened) {
            resetValue();
            readHeader();
        }
        if (this.mReadScriptLineByLine) {
            linesRead = readOneLine();
        } else {
            linesRead = readLines();
        }
        if (linesRead == 0) {
            closeFile();
        }
    }

    private void needSleep(long time) {
        if (time >= 1) {
            try {
                Thread.sleep(time);
            } catch (InterruptedException e) {
            }
        }
    }

    public boolean validate() {
        try {
            boolean validHeader = readHeader();
            closeFile();
            if (this.mVerbose > 0) {
                Logger logger = Logger.out;
                logger.println("Replaying " + this.mEventCountInScript + " events with speed " + this.mSpeed);
            }
            return validHeader;
        } catch (IOException e) {
            return THIS_DEBUG;
        }
    }

    public void setVerbose(int verbose) {
        this.mVerbose = verbose;
    }

    private void adjustKeyEventTime(MonkeyKeyEvent e) {
        long thisEventTime;
        long thisDownTime;
        if (e.getEventTime() >= 0) {
            if (this.mLastRecordedEventTime <= 0) {
                thisDownTime = SystemClock.uptimeMillis();
                thisEventTime = thisDownTime;
            } else {
                if (e.getDownTime() != this.mLastRecordedDownTimeKey) {
                    thisDownTime = e.getDownTime();
                } else {
                    thisDownTime = this.mLastExportDownTimeKey;
                }
                long expectedDelay = (long) (((double) (e.getEventTime() - this.mLastRecordedEventTime)) * this.mSpeed);
                thisEventTime = this.mLastExportEventTime + expectedDelay;
                needSleep(expectedDelay - SLEEP_COMPENSATE_DIFF);
            }
            this.mLastRecordedDownTimeKey = e.getDownTime();
            this.mLastRecordedEventTime = e.getEventTime();
            e.setDownTime(thisDownTime);
            e.setEventTime(thisEventTime);
            this.mLastExportDownTimeKey = thisDownTime;
            this.mLastExportEventTime = thisEventTime;
        }
    }

    private void adjustMotionEventTime(MonkeyMotionEvent e) {
        long thisEventTime = SystemClock.uptimeMillis();
        long thisDownTime = e.getDownTime();
        if (thisDownTime == this.mLastRecordedDownTimeMotion) {
            e.setDownTime(this.mLastExportDownTimeMotion);
        } else {
            this.mLastRecordedDownTimeMotion = thisDownTime;
            e.setDownTime(thisEventTime);
            this.mLastExportDownTimeMotion = thisEventTime;
        }
        e.setEventTime(thisEventTime);
    }

    public MonkeyEvent getNextEvent() {
        if (this.mQ.isEmpty()) {
            try {
                readNextBatch();
            } catch (IOException e) {
                return null;
            }
        }
        try {
            MonkeyEvent ev = (MonkeyEvent) this.mQ.getFirst();
            this.mQ.removeFirst();
            if (ev.getEventType() == 0) {
                adjustKeyEventTime((MonkeyKeyEvent) ev);
            } else if (ev.getEventType() == 1 || ev.getEventType() == 2) {
                adjustMotionEventTime((MonkeyMotionEvent) ev);
            }
            return ev;
        } catch (NoSuchElementException e2) {
            return null;
        }
    }
}
