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
    private static final int CALLER_LOCATION = 6;
    private static final int METHOD_TO_TRACE_LOCATION = 5;
    private static final int MIN_STACK_TRACE_LENGTH = 7;
    private static final String UIAUTOMATOR_PACKAGE = "com.android.uiautomator.core";
    private static final String UNKNOWN_METHOD_STRING = "(unknown method)";
    private static Tracer mInstance = null;
    private Mode mCurrentMode = Mode.NONE;
    private File mOutputFile;
    private List<TracerSink> mSinks = new ArrayList();

    public enum Mode {
        NONE,
        FILE,
        LOGCAT,
        ALL
    }

    /* access modifiers changed from: private */
    public interface TracerSink {
        void close();

        void log(String str);
    }

    /* access modifiers changed from: private */
    public class FileSink implements TracerSink {
        private SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        private PrintWriter mOut;

        public FileSink(File file) throws FileNotFoundException {
            this.mOut = new PrintWriter(file);
        }

        @Override // com.android.uiautomator.core.Tracer.TracerSink
        public void log(String message) {
            this.mOut.printf("%s %s\n", this.mDateFormat.format(new Date()), message);
        }

        @Override // com.android.uiautomator.core.Tracer.TracerSink
        public void close() {
            this.mOut.close();
        }
    }

    /* access modifiers changed from: private */
    public class LogcatSink implements TracerSink {
        private static final String LOGCAT_TAG = "UiAutomatorTrace";

        private LogcatSink() {
        }

        /* synthetic */ LogcatSink(Tracer x0, AnonymousClass1 x1) {
            this();
        }

        @Override // com.android.uiautomator.core.Tracer.TracerSink
        public void log(String message) {
            Log.i(LOGCAT_TAG, message);
        }

        @Override // com.android.uiautomator.core.Tracer.TracerSink
        public void close() {
        }
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
            int i = AnonymousClass1.$SwitchMap$com$android$uiautomator$core$Tracer$Mode[mode.ordinal()];
            if (i != 1) {
                if (i == 2) {
                    this.mSinks.add(new LogcatSink(this, null));
                } else if (i == 3) {
                    this.mSinks.add(new LogcatSink(this, null));
                    if (this.mOutputFile != null) {
                        this.mSinks.add(new FileSink(this.mOutputFile));
                        return;
                    }
                    throw new IllegalArgumentException("Please provide a filename before attempting write trace to a file");
                }
            } else if (this.mOutputFile != null) {
                this.mSinks.add(new FileSink(this.mOutputFile));
            } else {
                throw new IllegalArgumentException("Please provide a filename before attempting write trace to a file");
            }
        } catch (FileNotFoundException e) {
            Log.w("Tracer", "Could not open log file: " + e.getMessage());
        }
    }

    /* access modifiers changed from: package-private */
    /* renamed from: com.android.uiautomator.core.Tracer$1  reason: invalid class name */
    public static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] $SwitchMap$com$android$uiautomator$core$Tracer$Mode = new int[Mode.values().length];

        static {
            try {
                $SwitchMap$com$android$uiautomator$core$Tracer$Mode[Mode.FILE.ordinal()] = 1;
            } catch (NoSuchFieldError e) {
            }
            try {
                $SwitchMap$com$android$uiautomator$core$Tracer$Mode[Mode.LOGCAT.ordinal()] = 2;
            } catch (NoSuchFieldError e2) {
            }
            try {
                $SwitchMap$com$android$uiautomator$core$Tracer$Mode[Mode.ALL.ordinal()] = 3;
            } catch (NoSuchFieldError e3) {
            }
        }
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
        String caller;
        if (this.mCurrentMode != Mode.NONE && (caller = getCaller()) != null) {
            log(String.format("%s (%s)", caller, join(", ", arguments)));
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
        return String.format("%s.%s from %s() at %s:%d", caller.getClassName().substring(indexOfDot + 1), caller.getMethodName(), previousCaller.getMethodName(), previousCaller.getFileName(), Integer.valueOf(previousCaller.getLineNumber()));
    }
}
