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
            String line = this.mBufferedReader.readLine();
            if (line == null) {
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

    /* JADX INFO: Multiple debug info for r10v12 float: [D('pt2yEnd' float), D('y2' float)] */
    /* JADX INFO: Multiple debug info for r7v25 long: [D('y' float), D('waitDuration' long)] */
    private void handleEvent(String s, String[] args) {
        String[] strArr;
        String str;
        String[] strArr2;
        long eventTime;
        MonkeyMotionEvent e;
        int device;
        MonkeyMotionEvent e2;
        if (s.indexOf(EVENT_KEYWORD_KEY) >= 0 && args.length == 8) {
            try {
                Logger.out.println(" old key\n");
                long downTime = Long.parseLong(args[0]);
                long eventTime2 = Long.parseLong(args[1]);
                int action = Integer.parseInt(args[2]);
                int code = Integer.parseInt(args[3]);
                MonkeyKeyEvent e3 = new MonkeyKeyEvent(downTime, eventTime2, action, code, Integer.parseInt(args[4]), Integer.parseInt(args[5]), Integer.parseInt(args[6]), Integer.parseInt(args[7]));
                Logger logger = Logger.out;
                logger.println(" Key code " + code + "\n");
                this.mQ.addLast((MonkeyEvent) e3);
                Logger.out.println("Added key up \n");
            } catch (NumberFormatException e4) {
            }
        } else if ((s.indexOf(EVENT_KEYWORD_POINTER) >= 0 || s.indexOf(EVENT_KEYWORD_TRACKBALL) >= 0) && args.length == 12) {
            try {
                long downTime2 = Long.parseLong(args[0]);
                long eventTime3 = Long.parseLong(args[1]);
                int action2 = Integer.parseInt(args[2]);
                float x = Float.parseFloat(args[3]);
                float y = Float.parseFloat(args[4]);
                float pressure = Float.parseFloat(args[5]);
                float size = Float.parseFloat(args[6]);
                int metaState = Integer.parseInt(args[7]);
                float xPrecision = Float.parseFloat(args[8]);
                float yPrecision = Float.parseFloat(args[9]);
                int device2 = Integer.parseInt(args[10]);
                int edgeFlags = Integer.parseInt(args[11]);
                if (s.indexOf("Pointer") > 0) {
                    e2 = new MonkeyTouchEvent(action2);
                } else {
                    e2 = new MonkeyTrackballEvent(action2);
                }
                e2.setDownTime(downTime2).setEventTime(eventTime3).setMetaState(metaState).setPrecision(xPrecision, yPrecision).setDeviceId(device2).setEdgeFlags(edgeFlags).addPointer(0, x, y, pressure, size);
                this.mQ.addLast((MonkeyEvent) e2);
            } catch (NumberFormatException e5) {
            }
        } else if ((s.indexOf(EVENT_KEYWORD_POINTER) >= 0 || s.indexOf(EVENT_KEYWORD_TRACKBALL) >= 0) && args.length == 13) {
            try {
                long downTime3 = Long.parseLong(args[0]);
                long eventTime4 = Long.parseLong(args[1]);
                int action3 = Integer.parseInt(args[2]);
                float x2 = Float.parseFloat(args[3]);
                float y2 = Float.parseFloat(args[4]);
                float pressure2 = Float.parseFloat(args[5]);
                float size2 = Float.parseFloat(args[6]);
                int metaState2 = Integer.parseInt(args[7]);
                float xPrecision2 = Float.parseFloat(args[8]);
                float yPrecision2 = Float.parseFloat(args[9]);
                int device3 = Integer.parseInt(args[10]);
                int edgeFlags2 = Integer.parseInt(args[11]);
                int pointerId = Integer.parseInt(args[12]);
                if (s.indexOf("Pointer") > 0) {
                    if (action3 == 5) {
                        e = new MonkeyTouchEvent((pointerId << 8) | 5).setIntermediateNote(true);
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
                    device = device3;
                    e.setDownTime(downTime3).setEventTime(eventTime).setMetaState(metaState2).setPrecision(xPrecision2, yPrecision2).setDeviceId(device).setEdgeFlags(edgeFlags2).addPointer(0, this.mLastX[0], this.mLastY[0], pressure2, size2).addPointer(1, x2, y2, pressure2, size2);
                    this.mLastX[1] = x2;
                    this.mLastY[1] = y2;
                } else {
                    device = device3;
                    if (pointerId == 0) {
                        e.setDownTime(downTime3).setEventTime(eventTime).setMetaState(metaState2).setPrecision(xPrecision2, yPrecision2).setDeviceId(device).setEdgeFlags(edgeFlags2).addPointer(0, x2, y2, pressure2, size2);
                        if (action3 == 6) {
                            e.addPointer(1, this.mLastX[1], this.mLastY[1]);
                        }
                        this.mLastX[0] = x2;
                        this.mLastY[0] = y2;
                    }
                }
                if (this.mReadScriptLineByLine) {
                    long realElapsedTime = SystemClock.uptimeMillis() - this.mMonkeyStartTime;
                    long scriptElapsedTime = eventTime - this.mScriptStartTime;
                    if (realElapsedTime < scriptElapsedTime) {
                        this.mQ.addLast((MonkeyEvent) new MonkeyWaitEvent(scriptElapsedTime - realElapsedTime));
                    }
                }
                this.mQ.addLast((MonkeyEvent) e);
            } catch (NumberFormatException e6) {
            }
        } else {
            if (s.indexOf(EVENT_KEYWORD_ROTATION) >= 0) {
                strArr = args;
                if (strArr.length == 2) {
                    try {
                        int rotationDegree = Integer.parseInt(strArr[0]);
                        int persist = Integer.parseInt(strArr[1]);
                        if (rotationDegree == 0 || rotationDegree == 1 || rotationDegree == 2 || rotationDegree == 3) {
                            this.mQ.addLast((MonkeyEvent) new MonkeyRotationEvent(rotationDegree, persist != 0 ? true : THIS_DEBUG));
                            return;
                        }
                        return;
                    } catch (NumberFormatException e7) {
                        return;
                    }
                }
            } else {
                strArr = args;
            }
            if (s.indexOf(EVENT_KEYWORD_TAP) >= 0 && strArr.length >= 2) {
                try {
                    float x3 = Float.parseFloat(strArr[0]);
                    float y3 = Float.parseFloat(strArr[1]);
                    long tapDuration = 0;
                    if (strArr.length == 3) {
                        tapDuration = Long.parseLong(strArr[2]);
                    }
                    long downTime4 = SystemClock.uptimeMillis();
                    this.mQ.addLast((MonkeyEvent) new MonkeyTouchEvent(0).setDownTime(downTime4).setEventTime(downTime4).addPointer(0, x3, y3, 1.0f, 5.0f));
                    if (tapDuration > 0) {
                        this.mQ.addLast((MonkeyEvent) new MonkeyWaitEvent(tapDuration));
                    }
                    this.mQ.addLast((MonkeyEvent) new MonkeyTouchEvent(1).setDownTime(downTime4).setEventTime(downTime4).addPointer(0, x3, y3, 1.0f, 5.0f));
                } catch (NumberFormatException e8) {
                    Logger logger2 = Logger.err;
                    logger2.println("// " + e8.toString());
                }
            } else if (s.indexOf(EVENT_KEYWORD_PRESSANDHOLD) < 0 || strArr.length != 3) {
                if (s.indexOf(EVENT_KEYWORD_DRAG) < 0 || strArr.length != 5) {
                    str = "// ";
                } else {
                    float xStart = Float.parseFloat(strArr[0]);
                    float yStart = Float.parseFloat(strArr[1]);
                    float xEnd = Float.parseFloat(strArr[2]);
                    float yEnd = Float.parseFloat(strArr[3]);
                    int stepCount = Integer.parseInt(strArr[4]);
                    str = "// ";
                    long downTime5 = SystemClock.uptimeMillis();
                    long eventTime5 = SystemClock.uptimeMillis();
                    if (stepCount > 0) {
                        float xStep = (xEnd - xStart) / ((float) stepCount);
                        float yStep = (yEnd - yStart) / ((float) stepCount);
                        this.mQ.addLast((MonkeyEvent) new MonkeyTouchEvent(0).setDownTime(downTime5).setEventTime(eventTime5).addPointer(0, xStart, yStart, 1.0f, 5.0f));
                        int i = 0;
                        float x4 = xStart;
                        float y4 = yStart;
                        while (i < stepCount) {
                            x4 += xStep;
                            y4 += yStep;
                            this.mQ.addLast((MonkeyEvent) new MonkeyTouchEvent(2).setDownTime(downTime5).setEventTime(SystemClock.uptimeMillis()).addPointer(0, x4, y4, 1.0f, 5.0f));
                            i++;
                            xStart = xStart;
                            yStep = yStep;
                        }
                        this.mQ.addLast((MonkeyEvent) new MonkeyTouchEvent(1).setDownTime(downTime5).setEventTime(SystemClock.uptimeMillis()).addPointer(0, x4, y4, 1.0f, 5.0f));
                    }
                }
                if (s.indexOf(EVENT_KEYWORD_PINCH_ZOOM) >= 0 && strArr.length == 9) {
                    float pt1xStart = Float.parseFloat(strArr[0]);
                    float pt1yStart = Float.parseFloat(strArr[1]);
                    float pt1xEnd = Float.parseFloat(strArr[2]);
                    float pt1yEnd = Float.parseFloat(strArr[3]);
                    float pt2xStart = Float.parseFloat(strArr[4]);
                    float pt2yStart = Float.parseFloat(strArr[5]);
                    float pt2xEnd = Float.parseFloat(strArr[6]);
                    float pt2yEnd = Float.parseFloat(strArr[7]);
                    int stepCount2 = Integer.parseInt(strArr[8]);
                    float y1 = pt1yStart;
                    long downTime6 = SystemClock.uptimeMillis();
                    long eventTime6 = SystemClock.uptimeMillis();
                    if (stepCount2 > 0) {
                        float pt1xStep = (pt1xEnd - pt1xStart) / ((float) stepCount2);
                        float pt1yStep = (pt1yEnd - pt1yStart) / ((float) stepCount2);
                        float pt2xStep = (pt2xEnd - pt2xStart) / ((float) stepCount2);
                        float pt2yStep = (pt2yEnd - pt2yStart) / ((float) stepCount2);
                        this.mQ.addLast((MonkeyEvent) new MonkeyTouchEvent(0).setDownTime(downTime6).setEventTime(eventTime6).addPointer(0, pt1xStart, y1, 1.0f, 5.0f));
                        float y22 = pt2yStart;
                        float x22 = pt2xStart;
                        this.mQ.addLast((MonkeyEvent) new MonkeyTouchEvent(261).setDownTime(downTime6).addPointer(0, pt1xStart, y1).addPointer(1, x22, y22).setIntermediateNote(true));
                        int i2 = 0;
                        float x1 = pt1xStart;
                        while (i2 < stepCount2) {
                            x1 += pt1xStep;
                            float y12 = y1 + pt1yStep;
                            x22 += pt2xStep;
                            y22 += pt2yStep;
                            this.mQ.addLast((MonkeyEvent) new MonkeyTouchEvent(2).setDownTime(downTime6).setEventTime(SystemClock.uptimeMillis()).addPointer(0, x1, y12, 1.0f, 5.0f).addPointer(1, x22, y22, 1.0f, 5.0f));
                            i2++;
                            y1 = y12;
                            pt1xStep = pt1xStep;
                            pt1yStep = pt1yStep;
                        }
                        this.mQ.addLast((MonkeyEvent) new MonkeyTouchEvent(6).setDownTime(downTime6).setEventTime(SystemClock.uptimeMillis()).addPointer(0, x1, y1).addPointer(1, x22, y22));
                    }
                }
                if (s.indexOf(EVENT_KEYWORD_FLIP) >= 0) {
                    strArr2 = args;
                    if (strArr2.length == 1) {
                        this.mQ.addLast((MonkeyEvent) new MonkeyFlipEvent(Boolean.parseBoolean(strArr2[0])));
                    }
                } else {
                    strArr2 = args;
                }
                if (s.indexOf(EVENT_KEYWORD_ACTIVITY) >= 0 && strArr2.length >= 2) {
                    long alarmTime = 0;
                    ComponentName mApp = new ComponentName(strArr2[0], strArr2[1]);
                    if (strArr2.length > 2) {
                        try {
                            alarmTime = Long.parseLong(strArr2[2]);
                        } catch (NumberFormatException e9) {
                            Logger logger3 = Logger.err;
                            logger3.println(str + e9.toString());
                            return;
                        }
                    }
                    if (strArr2.length == 2) {
                        this.mQ.addLast((MonkeyEvent) new MonkeyActivityEvent(mApp));
                    } else {
                        this.mQ.addLast((MonkeyEvent) new MonkeyActivityEvent(mApp, alarmTime));
                    }
                } else if (s.indexOf(EVENT_KEYWORD_DEVICE_WAKEUP) >= 0) {
                    long deviceSleepTime = this.mDeviceSleepTime;
                    this.mQ.addLast((MonkeyEvent) new MonkeyActivityEvent(new ComponentName("com.google.android.powerutil", "com.google.android.powerutil.WakeUpScreen"), deviceSleepTime));
                    this.mQ.addLast((MonkeyEvent) new MonkeyKeyEvent(0, 7));
                    this.mQ.addLast((MonkeyEvent) new MonkeyKeyEvent(1, 7));
                    this.mQ.addLast((MonkeyEvent) new MonkeyWaitEvent(3000 + deviceSleepTime));
                    this.mQ.addLast((MonkeyEvent) new MonkeyKeyEvent(0, 82));
                    this.mQ.addLast((MonkeyEvent) new MonkeyKeyEvent(1, 82));
                    this.mQ.addLast((MonkeyEvent) new MonkeyKeyEvent(0, 4));
                    this.mQ.addLast((MonkeyEvent) new MonkeyKeyEvent(1, 4));
                } else if (s.indexOf(EVENT_KEYWORD_INSTRUMENTATION) >= 0 && strArr2.length == 2) {
                    this.mQ.addLast((MonkeyEvent) new MonkeyInstrumentationEvent(strArr2[0], strArr2[1]));
                } else if (s.indexOf(EVENT_KEYWORD_WAIT) >= 0 && strArr2.length == 1) {
                    try {
                        this.mQ.addLast((MonkeyEvent) new MonkeyWaitEvent((long) Integer.parseInt(strArr2[0])));
                    } catch (NumberFormatException e10) {
                    }
                } else if (s.indexOf(EVENT_KEYWORD_PROFILE_WAIT) >= 0) {
                    this.mQ.addLast((MonkeyEvent) new MonkeyWaitEvent(this.mProfileWaitTime));
                } else if (s.indexOf(EVENT_KEYWORD_KEYPRESS) < 0 || strArr2.length != 1) {
                    if (s.indexOf(EVENT_KEYWORD_LONGPRESS) >= 0) {
                        this.mQ.addLast((MonkeyEvent) new MonkeyKeyEvent(0, 23));
                        this.mQ.addLast((MonkeyEvent) new MonkeyWaitEvent((long) LONGPRESS_WAIT_TIME));
                        this.mQ.addLast((MonkeyEvent) new MonkeyKeyEvent(1, 23));
                    }
                    if (s.indexOf(EVENT_KEYWORD_POWERLOG) >= 0 && strArr2.length > 0) {
                        String power_log_type = strArr2[0];
                        if (strArr2.length == 1) {
                            this.mQ.addLast((MonkeyEvent) new MonkeyPowerEvent(power_log_type));
                        } else if (strArr2.length == 2) {
                            this.mQ.addLast((MonkeyEvent) new MonkeyPowerEvent(power_log_type, strArr2[1]));
                        }
                    }
                    if (s.indexOf(EVENT_KEYWORD_WRITEPOWERLOG) >= 0) {
                        this.mQ.addLast((MonkeyEvent) new MonkeyPowerEvent());
                    }
                    if (s.indexOf(EVENT_KEYWORD_RUNCMD) >= 0 && strArr2.length == 1) {
                        this.mQ.addLast((MonkeyEvent) new MonkeyCommandEvent(strArr2[0]));
                    }
                    if (s.indexOf(EVENT_KEYWORD_INPUT_STRING) >= 0 && strArr2.length == 1) {
                        String input = strArr2[0];
                        this.mQ.addLast((MonkeyEvent) new MonkeyCommandEvent("input text " + input));
                    } else if (s.indexOf(EVENT_KEYWORD_START_FRAMERATE_CAPTURE) >= 0) {
                        this.mQ.addLast((MonkeyEvent) new MonkeyGetFrameRateEvent("start"));
                    } else if (s.indexOf(EVENT_KEYWORD_END_FRAMERATE_CAPTURE) >= 0 && strArr2.length == 1) {
                        this.mQ.addLast((MonkeyEvent) new MonkeyGetFrameRateEvent("end", strArr2[0]));
                    } else if (s.indexOf(EVENT_KEYWORD_START_APP_FRAMERATE_CAPTURE) >= 0 && strArr2.length == 1) {
                        this.mQ.addLast((MonkeyEvent) new MonkeyGetAppFrameRateEvent("start", strArr2[0]));
                    } else if (s.indexOf(EVENT_KEYWORD_END_APP_FRAMERATE_CAPTURE) >= 0 && strArr2.length == 2) {
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
                    float x5 = Float.parseFloat(strArr[0]);
                    float y5 = Float.parseFloat(strArr[1]);
                    long pressDuration = Long.parseLong(strArr[2]);
                    long downTime7 = SystemClock.uptimeMillis();
                    MonkeyMotionEvent e1 = new MonkeyTouchEvent(0).setDownTime(downTime7).setEventTime(downTime7).addPointer(0, x5, y5, 1.0f, 5.0f);
                    MonkeyWaitEvent e22 = new MonkeyWaitEvent(pressDuration);
                    new MonkeyTouchEvent(1).setDownTime(downTime7 + pressDuration).setEventTime(downTime7 + pressDuration).addPointer(0, x5, y5, 1.0f, 5.0f);
                    this.mQ.addLast((MonkeyEvent) e1);
                    this.mQ.addLast((MonkeyEvent) e22);
                    this.mQ.addLast((MonkeyEvent) e22);
                } catch (NumberFormatException e11) {
                    Logger logger4 = Logger.err;
                    logger4.println("// " + e11.toString());
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

    @Override // com.android.commands.monkey.MonkeyEventSource
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

    @Override // com.android.commands.monkey.MonkeyEventSource
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

    @Override // com.android.commands.monkey.MonkeyEventSource
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
