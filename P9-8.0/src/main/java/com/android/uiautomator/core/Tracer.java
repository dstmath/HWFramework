package com.android.uiautomator.core;

import android.util.Log;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Tracer {
    private static final /* synthetic */ int[] -com-android-uiautomator-core-Tracer$ModeSwitchesValues = null;
    private static final int CALLER_LOCATION = 6;
    private static final int METHOD_TO_TRACE_LOCATION = 5;
    private static final int MIN_STACK_TRACE_LENGTH = 7;
    private static final String UIAUTOMATOR_PACKAGE = "com.android.uiautomator.core";
    private static final String UNKNOWN_METHOD_STRING = "(unknown method)";
    private static Tracer mInstance = null;
    private Mode mCurrentMode = Mode.NONE;
    private File mOutputFile;
    private List<TracerSink> mSinks = new ArrayList();

    private interface TracerSink {
        void close();

        void log(String str);
    }

    private class FileSink implements TracerSink {
        private SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        private PrintWriter mOut;

        public FileSink(File file) throws FileNotFoundException {
            this.mOut = new PrintWriter(file);
        }

        public void log(String message) {
            this.mOut.printf("%s %s\n", new Object[]{this.mDateFormat.format(new Date()), message});
        }

        public void close() {
            this.mOut.close();
        }
    }

    private class LogcatSink implements TracerSink {
        private static final String LOGCAT_TAG = "UiAutomatorTrace";

        /* synthetic */ LogcatSink(Tracer this$0, LogcatSink -this1) {
            this();
        }

        private LogcatSink() {
        }

        public void log(String message) {
            Log.i(LOGCAT_TAG, message);
        }

        public void close() {
        }
    }

    public enum Mode {
        NONE,
        FILE,
        LOGCAT,
        ALL
    }

    private static /* synthetic */ int[] -getcom-android-uiautomator-core-Tracer$ModeSwitchesValues() {
        if (-com-android-uiautomator-core-Tracer$ModeSwitchesValues != null) {
            return -com-android-uiautomator-core-Tracer$ModeSwitchesValues;
        }
        int[] iArr = new int[Mode.values().length];
        try {
            iArr[Mode.ALL.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[Mode.FILE.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[Mode.LOGCAT.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[Mode.NONE.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        -com-android-uiautomator-core-Tracer$ModeSwitchesValues = iArr;
        return iArr;
    }

    public static Tracer getInstance() {
        if (mInstance == null) {
            mInstance = new Tracer();
        }
        return mInstance;
    }

    public void setOutputMode(Mode mode) {
        closeSinks();
        this.mCurrentMode = mode;
        try {
            switch (-getcom-android-uiautomator-core-Tracer$ModeSwitchesValues()[mode.ordinal()]) {
                case 1:
                    this.mSinks.add(new LogcatSink(this, null));
                    if (this.mOutputFile == null) {
                        throw new IllegalArgumentException("Please provide a filename before attempting write trace to a file");
                    }
                    this.mSinks.add(new FileSink(this.mOutputFile));
                    return;
                case 2:
                    if (this.mOutputFile == null) {
                        throw new IllegalArgumentException("Please provide a filename before attempting write trace to a file");
                    }
                    this.mSinks.add(new FileSink(this.mOutputFile));
                    return;
                case 3:
                    this.mSinks.add(new LogcatSink(this, null));
                    return;
                default:
                    return;
            }
        } catch (FileNotFoundException e) {
            Log.w("Tracer", "Could not open log file: " + e.getMessage());
        }
        Log.w("Tracer", "Could not open log file: " + e.getMessage());
    }

    private void closeSinks() {
        for (TracerSink sink : this.mSinks) {
            sink.close();
        }
        this.mSinks.clear();
    }

    public void setOutputFilename(String filename) {
        this.mOutputFile = new File(filename);
    }

    private void doTrace(Object[] arguments) {
        if (this.mCurrentMode != Mode.NONE && getCaller() != null) {
            log(String.format("%s (%s)", new Object[]{getCaller(), join(", ", arguments)}));
        }
    }

    private void log(String message) {
        for (TracerSink sink : this.mSinks) {
            sink.log(message);
        }
    }

    public boolean isTracingEnabled() {
        return this.mCurrentMode != Mode.NONE;
    }

    public static void trace(Object... arguments) {
        getInstance().doTrace(arguments);
    }

    private static String join(String separator, Object[] strings) {
        if (strings.length == 0) {
            return "";
        }
        StringBuilder builder = new StringBuilder(objectToString(strings[0]));
        for (int i = 1; i < strings.length; i++) {
            builder.append(separator);
            builder.append(objectToString(strings[i]));
        }
        return builder.toString();
    }

    private static String objectToString(Object obj) {
        if (!obj.getClass().isArray()) {
            return obj.toString();
        }
        if (obj instanceof Object[]) {
            return Arrays.deepToString((Object[]) obj);
        }
        return "[...]";
    }

    private static String getCaller() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        if (stackTrace.length < MIN_STACK_TRACE_LENGTH) {
            return UNKNOWN_METHOD_STRING;
        }
        StackTraceElement caller = stackTrace[METHOD_TO_TRACE_LOCATION];
        StackTraceElement previousCaller = stackTrace[CALLER_LOCATION];
        if (previousCaller.getClassName().startsWith(UIAUTOMATOR_PACKAGE)) {
            return null;
        }
        int indexOfDot = caller.getClassName().lastIndexOf(46);
        if (indexOfDot < 0) {
            indexOfDot = 0;
        }
        if (indexOfDot + 1 >= caller.getClassName().length()) {
            return UNKNOWN_METHOD_STRING;
        }
        Object[] objArr = new Object[METHOD_TO_TRACE_LOCATION];
        objArr[0] = caller.getClassName().substring(indexOfDot + 1);
        objArr[1] = caller.getMethodName();
        objArr[2] = previousCaller.getMethodName();
        objArr[3] = previousCaller.getFileName();
        objArr[4] = Integer.valueOf(previousCaller.getLineNumber());
        return String.format("%s.%s from %s() at %s:%d", objArr);
    }
}
