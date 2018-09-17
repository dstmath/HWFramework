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
    private boolean mFileOpened = false;
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
    private boolean mReadScriptLineByLine = false;
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
                return false;
            }
            line = line.trim();
            if (line.indexOf(HEADER_COUNT) >= 0) {
                try {
                    this.mEventCountInScript = Integer.parseInt(line.substring(HEADER_COUNT.length() + 1).trim());
                } catch (NumberFormatException e) {
                    Logger.err.println("" + e);
                    return false;
                }
            } else if (line.indexOf(HEADER_SPEED) >= 0) {
                try {
                    this.mSpeed = Double.parseDouble(line.substring(HEADER_COUNT.length() + 1).trim());
                } catch (NumberFormatException e2) {
                    Logger.err.println("" + e2);
                    return false;
                }
            } else if (line.indexOf(HEADER_LINE_BY_LINE) >= 0) {
                this.mReadScriptLineByLine = true;
            } else if (line.indexOf(STARTING_DATA_LINE) >= 0) {
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
        long downTime;
        long eventTime;
        int action;
        float x;
        float y;
        float pressure;
        float size;
        int metaState;
        float xPrecision;
        float yPrecision;
        int device;
        int edgeFlags;
        MonkeyEvent monkeyTouchEvent;
        MonkeyEvent e;
        if (s.indexOf(EVENT_KEYWORD_KEY) >= 0 && args.length == 8) {
            try {
                Logger.out.println(" old key\n");
                downTime = Long.parseLong(args[0]);
                eventTime = Long.parseLong(args[1]);
                action = Integer.parseInt(args[2]);
                int code = Integer.parseInt(args[3]);
                MonkeyEvent e2 = new MonkeyKeyEvent(downTime, eventTime, action, code, Integer.parseInt(args[4]), Integer.parseInt(args[5]), Integer.parseInt(args[6]), Integer.parseInt(args[7]));
                Logger.out.println(" Key code " + code + "\n");
                this.mQ.addLast(e2);
                Logger.out.println("Added key up \n");
            } catch (NumberFormatException e3) {
            }
        } else if ((s.indexOf(EVENT_KEYWORD_POINTER) >= 0 || s.indexOf(EVENT_KEYWORD_TRACKBALL) >= 0) && args.length == 12) {
            try {
                downTime = Long.parseLong(args[0]);
                eventTime = Long.parseLong(args[1]);
                action = Integer.parseInt(args[2]);
                x = Float.parseFloat(args[3]);
                y = Float.parseFloat(args[4]);
                pressure = Float.parseFloat(args[5]);
                size = Float.parseFloat(args[6]);
                metaState = Integer.parseInt(args[7]);
                xPrecision = Float.parseFloat(args[8]);
                yPrecision = Float.parseFloat(args[9]);
                device = Integer.parseInt(args[10]);
                edgeFlags = Integer.parseInt(args[11]);
                if (s.indexOf("Pointer") > 0) {
                    monkeyTouchEvent = new MonkeyTouchEvent(action);
                } else {
                    monkeyTouchEvent = new MonkeyTrackballEvent(action);
                }
                e.setDownTime(downTime).setEventTime(eventTime).setMetaState(metaState).setPrecision(xPrecision, yPrecision).setDeviceId(device).setEdgeFlags(edgeFlags).addPointer(0, x, y, pressure, size);
                this.mQ.addLast(e);
            } catch (NumberFormatException e4) {
            }
        } else if ((s.indexOf(EVENT_KEYWORD_POINTER) >= 0 || s.indexOf(EVENT_KEYWORD_TRACKBALL) >= 0) && args.length == 13) {
            try {
                downTime = Long.parseLong(args[0]);
                eventTime = Long.parseLong(args[1]);
                action = Integer.parseInt(args[2]);
                x = Float.parseFloat(args[3]);
                y = Float.parseFloat(args[4]);
                pressure = Float.parseFloat(args[5]);
                size = Float.parseFloat(args[6]);
                metaState = Integer.parseInt(args[7]);
                xPrecision = Float.parseFloat(args[8]);
                yPrecision = Float.parseFloat(args[9]);
                device = Integer.parseInt(args[10]);
                edgeFlags = Integer.parseInt(args[11]);
                int pointerId = Integer.parseInt(args[12]);
                if (s.indexOf("Pointer") > 0) {
                    if (action == 5) {
                        e = new MonkeyTouchEvent((pointerId << 8) | 5).setIntermediateNote(true);
                    } else {
                        monkeyTouchEvent = new MonkeyTouchEvent(action);
                    }
                    if (this.mScriptStartTime < 0) {
                        this.mMonkeyStartTime = SystemClock.uptimeMillis();
                        this.mScriptStartTime = eventTime;
                    }
                } else {
                    monkeyTouchEvent = new MonkeyTrackballEvent(action);
                }
                if (pointerId == 1) {
                    e.setDownTime(downTime).setEventTime(eventTime).setMetaState(metaState).setPrecision(xPrecision, yPrecision).setDeviceId(device).setEdgeFlags(edgeFlags).addPointer(0, this.mLastX[0], this.mLastY[0], pressure, size).addPointer(1, x, y, pressure, size);
                    this.mLastX[1] = x;
                    this.mLastY[1] = y;
                } else if (pointerId == 0) {
                    e.setDownTime(downTime).setEventTime(eventTime).setMetaState(metaState).setPrecision(xPrecision, yPrecision).setDeviceId(device).setEdgeFlags(edgeFlags).addPointer(0, x, y, pressure, size);
                    if (action == 6) {
                        e.addPointer(1, this.mLastX[1], this.mLastY[1]);
                    }
                    this.mLastX[0] = x;
                    this.mLastY[0] = y;
                }
                if (this.mReadScriptLineByLine) {
                    long realElapsedTime = SystemClock.uptimeMillis() - this.mMonkeyStartTime;
                    long scriptElapsedTime = eventTime - this.mScriptStartTime;
                    if (realElapsedTime < scriptElapsedTime) {
                        this.mQ.addLast(new MonkeyWaitEvent(scriptElapsedTime - realElapsedTime));
                    }
                }
                this.mQ.addLast(e);
            } catch (NumberFormatException e5) {
            }
        } else if (s.indexOf(EVENT_KEYWORD_ROTATION) >= 0 && args.length == 2) {
            try {
                int rotationDegree = Integer.parseInt(args[0]);
                int persist = Integer.parseInt(args[1]);
                if (rotationDegree == 0 || rotationDegree == 1 || rotationDegree == 2 || rotationDegree == 3) {
                    this.mQ.addLast(new MonkeyRotationEvent(rotationDegree, persist != 0));
                }
            } catch (NumberFormatException e6) {
            }
        } else if (s.indexOf(EVENT_KEYWORD_TAP) >= 0 && args.length >= 2) {
            try {
                x = Float.parseFloat(args[0]);
                y = Float.parseFloat(args[1]);
                long tapDuration = 0;
                if (args.length == 3) {
                    tapDuration = Long.parseLong(args[2]);
                }
                downTime = SystemClock.uptimeMillis();
                this.mQ.addLast(new MonkeyTouchEvent(0).setDownTime(downTime).setEventTime(downTime).addPointer(0, x, y, 1.0f, 5.0f));
                if (tapDuration > 0) {
                    this.mQ.addLast(new MonkeyWaitEvent(tapDuration));
                }
                this.mQ.addLast(new MonkeyTouchEvent(1).setDownTime(downTime).setEventTime(downTime).addPointer(0, x, y, 1.0f, 5.0f));
            } catch (NumberFormatException e7) {
                Logger.err.println("// " + e7.toString());
            }
        } else if (s.indexOf(EVENT_KEYWORD_PRESSANDHOLD) < 0 || args.length != 3) {
            int stepCount;
            int i;
            if (s.indexOf(EVENT_KEYWORD_DRAG) >= 0 && args.length == 5) {
                float xStart = Float.parseFloat(args[0]);
                float yStart = Float.parseFloat(args[1]);
                float xEnd = Float.parseFloat(args[2]);
                float yEnd = Float.parseFloat(args[3]);
                stepCount = Integer.parseInt(args[4]);
                x = xStart;
                y = yStart;
                downTime = SystemClock.uptimeMillis();
                eventTime = SystemClock.uptimeMillis();
                if (stepCount > 0) {
                    float xStep = (xEnd - xStart) / ((float) stepCount);
                    float yStep = (yEnd - yStart) / ((float) stepCount);
                    this.mQ.addLast(new MonkeyTouchEvent(0).setDownTime(downTime).setEventTime(eventTime).addPointer(0, xStart, yStart, 1.0f, 5.0f));
                    for (i = 0; i < stepCount; i++) {
                        x += xStep;
                        y += yStep;
                        this.mQ.addLast(new MonkeyTouchEvent(2).setDownTime(downTime).setEventTime(SystemClock.uptimeMillis()).addPointer(0, x, y, 1.0f, 5.0f));
                    }
                    this.mQ.addLast(new MonkeyTouchEvent(1).setDownTime(downTime).setEventTime(SystemClock.uptimeMillis()).addPointer(0, x, y, 1.0f, 5.0f));
                }
            }
            if (s.indexOf(EVENT_KEYWORD_PINCH_ZOOM) >= 0 && args.length == 9) {
                float pt1xStart = Float.parseFloat(args[0]);
                float pt1yStart = Float.parseFloat(args[1]);
                float pt1xEnd = Float.parseFloat(args[2]);
                float pt1yEnd = Float.parseFloat(args[3]);
                float pt2xStart = Float.parseFloat(args[4]);
                float pt2yStart = Float.parseFloat(args[5]);
                float pt2xEnd = Float.parseFloat(args[6]);
                float pt2yEnd = Float.parseFloat(args[7]);
                stepCount = Integer.parseInt(args[8]);
                float x1 = pt1xStart;
                float y1 = pt1yStart;
                float x2 = pt2xStart;
                float y2 = pt2yStart;
                downTime = SystemClock.uptimeMillis();
                eventTime = SystemClock.uptimeMillis();
                if (stepCount > 0) {
                    float pt1xStep = (pt1xEnd - pt1xStart) / ((float) stepCount);
                    float pt1yStep = (pt1yEnd - pt1yStart) / ((float) stepCount);
                    float pt2xStep = (pt2xEnd - pt2xStart) / ((float) stepCount);
                    float pt2yStep = (pt2yEnd - pt2yStart) / ((float) stepCount);
                    this.mQ.addLast(new MonkeyTouchEvent(0).setDownTime(downTime).setEventTime(eventTime).addPointer(0, pt1xStart, pt1yStart, 1.0f, 5.0f));
                    this.mQ.addLast(new MonkeyTouchEvent(261).setDownTime(downTime).addPointer(0, pt1xStart, pt1yStart).addPointer(1, pt2xStart, pt2yStart).setIntermediateNote(true));
                    for (i = 0; i < stepCount; i++) {
                        x1 += pt1xStep;
                        y1 += pt1yStep;
                        x2 += pt2xStep;
                        y2 += pt2yStep;
                        this.mQ.addLast(new MonkeyTouchEvent(2).setDownTime(downTime).setEventTime(SystemClock.uptimeMillis()).addPointer(0, x1, y1, 1.0f, 5.0f).addPointer(1, x2, y2, 1.0f, 5.0f));
                    }
                    this.mQ.addLast(new MonkeyTouchEvent(6).setDownTime(downTime).setEventTime(SystemClock.uptimeMillis()).addPointer(0, x1, y1).addPointer(1, x2, y2));
                }
            }
            if (s.indexOf(EVENT_KEYWORD_FLIP) >= 0 && args.length == 1) {
                this.mQ.addLast(new MonkeyFlipEvent(Boolean.parseBoolean(args[0])));
            }
            if (s.indexOf(EVENT_KEYWORD_ACTIVITY) >= 0 && args.length >= 2) {
                long alarmTime = 0;
                ComponentName componentName = new ComponentName(args[0], args[1]);
                if (args.length > 2) {
                    try {
                        alarmTime = Long.parseLong(args[2]);
                    } catch (NumberFormatException e72) {
                        Logger.err.println("// " + e72.toString());
                        return;
                    }
                }
                if (args.length == 2) {
                    this.mQ.addLast(new MonkeyActivityEvent(componentName));
                } else {
                    this.mQ.addLast(new MonkeyActivityEvent(componentName, alarmTime));
                }
            } else if (s.indexOf(EVENT_KEYWORD_DEVICE_WAKEUP) >= 0) {
                long deviceSleepTime = this.mDeviceSleepTime;
                this.mQ.addLast(new MonkeyActivityEvent(new ComponentName("com.google.android.powerutil", "com.google.android.powerutil.WakeUpScreen"), deviceSleepTime));
                this.mQ.addLast(new MonkeyKeyEvent(0, 7));
                this.mQ.addLast(new MonkeyKeyEvent(1, 7));
                this.mQ.addLast(new MonkeyWaitEvent(3000 + deviceSleepTime));
                this.mQ.addLast(new MonkeyKeyEvent(0, 82));
                this.mQ.addLast(new MonkeyKeyEvent(1, 82));
                this.mQ.addLast(new MonkeyKeyEvent(0, 4));
                this.mQ.addLast(new MonkeyKeyEvent(1, 4));
            } else if (s.indexOf(EVENT_KEYWORD_INSTRUMENTATION) >= 0 && args.length == 2) {
                this.mQ.addLast(new MonkeyInstrumentationEvent(args[0], args[1]));
            } else if (s.indexOf(EVENT_KEYWORD_WAIT) >= 0 && args.length == 1) {
                try {
                    this.mQ.addLast(new MonkeyWaitEvent((long) Integer.parseInt(args[0])));
                } catch (NumberFormatException e8) {
                }
            } else if (s.indexOf(EVENT_KEYWORD_PROFILE_WAIT) >= 0) {
                this.mQ.addLast(new MonkeyWaitEvent(this.mProfileWaitTime));
            } else if (s.indexOf(EVENT_KEYWORD_KEYPRESS) < 0 || args.length != 1) {
                if (s.indexOf(EVENT_KEYWORD_LONGPRESS) >= 0) {
                    this.mQ.addLast(new MonkeyKeyEvent(0, 23));
                    this.mQ.addLast(new MonkeyWaitEvent((long) LONGPRESS_WAIT_TIME));
                    this.mQ.addLast(new MonkeyKeyEvent(1, 23));
                }
                if (s.indexOf(EVENT_KEYWORD_POWERLOG) >= 0 && args.length > 0) {
                    String power_log_type = args[0];
                    if (args.length == 1) {
                        this.mQ.addLast(new MonkeyPowerEvent(power_log_type));
                    } else if (args.length == 2) {
                        this.mQ.addLast(new MonkeyPowerEvent(power_log_type, args[1]));
                    }
                }
                if (s.indexOf(EVENT_KEYWORD_WRITEPOWERLOG) >= 0) {
                    this.mQ.addLast(new MonkeyPowerEvent());
                }
                if (s.indexOf(EVENT_KEYWORD_RUNCMD) >= 0 && args.length == 1) {
                    this.mQ.addLast(new MonkeyCommandEvent(args[0]));
                }
                if (s.indexOf(EVENT_KEYWORD_INPUT_STRING) >= 0 && args.length == 1) {
                    this.mQ.addLast(new MonkeyCommandEvent("input text " + args[0]));
                } else if (s.indexOf(EVENT_KEYWORD_START_FRAMERATE_CAPTURE) >= 0) {
                    this.mQ.addLast(new MonkeyGetFrameRateEvent("start"));
                } else if (s.indexOf(EVENT_KEYWORD_END_FRAMERATE_CAPTURE) >= 0 && args.length == 1) {
                    this.mQ.addLast(new MonkeyGetFrameRateEvent("end", args[0]));
                } else if (s.indexOf(EVENT_KEYWORD_START_APP_FRAMERATE_CAPTURE) >= 0 && args.length == 1) {
                    this.mQ.addLast(new MonkeyGetAppFrameRateEvent("start", args[0]));
                } else if (s.indexOf(EVENT_KEYWORD_END_APP_FRAMERATE_CAPTURE) >= 0 && args.length == 2) {
                    this.mQ.addLast(new MonkeyGetAppFrameRateEvent("end", args[0], args[1]));
                }
            } else {
                int keyCode = MonkeySourceRandom.getKeyCode(args[0]);
                if (keyCode != 0) {
                    this.mQ.addLast(new MonkeyKeyEvent(0, keyCode));
                    this.mQ.addLast(new MonkeyKeyEvent(1, keyCode));
                }
            }
        } else {
            try {
                x = Float.parseFloat(args[0]);
                y = Float.parseFloat(args[1]);
                long pressDuration = Long.parseLong(args[2]);
                downTime = SystemClock.uptimeMillis();
                MonkeyEvent e1 = new MonkeyTouchEvent(0).setDownTime(downTime).setEventTime(downTime).addPointer(0, x, y, 1.0f, 5.0f);
                monkeyTouchEvent = new MonkeyWaitEvent(pressDuration);
                MonkeyMotionEvent e32 = new MonkeyTouchEvent(1).setDownTime(downTime + pressDuration).setEventTime(downTime + pressDuration).addPointer(0, x, y, 1.0f, 5.0f);
                this.mQ.addLast(e1);
                this.mQ.addLast(monkeyTouchEvent);
                this.mQ.addLast(monkeyTouchEvent);
            } catch (NumberFormatException e722) {
                Logger.err.println("// " + e722.toString());
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
        this.mFileOpened = false;
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
                Logger.out.println("Replaying " + this.mEventCountInScript + " events with speed " + this.mSpeed);
            }
            return validHeader;
        } catch (IOException e) {
            return false;
        }
    }

    public void setVerbose(int verbose) {
        this.mVerbose = verbose;
    }

    private void adjustKeyEventTime(MonkeyKeyEvent e) {
        if (e.getEventTime() >= 0) {
            long thisDownTime;
            long thisEventTime;
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
