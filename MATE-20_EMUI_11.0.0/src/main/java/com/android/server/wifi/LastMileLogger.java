package com.android.server.wifi;

import android.os.FileUtils;
import com.android.internal.annotations.VisibleForTesting;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import libcore.io.IoUtils;

public class LastMileLogger {
    private static final String TAG = "LastMileLogger";
    private static final String WIFI_EVENT_BUFFER_PATH = "/sys/kernel/debug/tracing/instances/wifi/trace";
    private static final String WIFI_EVENT_ENABLE_PATH = "/sys/kernel/debug/tracing/instances/wifi/tracing_on";
    private static final String WIFI_EVENT_RELEASE_PATH = "/sys/kernel/debug/tracing/instances/wifi/free_buffer";
    private final String mEventBufferPath;
    private final String mEventEnablePath;
    private final String mEventReleasePath;
    private byte[] mLastMileLogForLastFailure;
    private FileInputStream mLastMileTraceHandle;
    private WifiLog mLog;

    public LastMileLogger(WifiInjector injector) {
        this(injector, WIFI_EVENT_BUFFER_PATH, WIFI_EVENT_ENABLE_PATH, WIFI_EVENT_RELEASE_PATH);
    }

    @VisibleForTesting
    public LastMileLogger(WifiInjector injector, String bufferPath, String enablePath, String releasePath) {
        this.mLog = injector.makeLog(TAG);
        this.mEventBufferPath = bufferPath;
        this.mEventEnablePath = enablePath;
        this.mEventReleasePath = releasePath;
    }

    public void reportConnectionEvent(byte event) {
        if (event == 0) {
            enableTracing();
        } else if (event == 1) {
            disableTracing();
        } else if (event == 2) {
            disableTracing();
            this.mLastMileLogForLastFailure = readTrace();
        } else if (event == 3) {
            disableTracing();
            this.mLastMileLogForLastFailure = readTrace();
        }
    }

    public void dump(PrintWriter pw) {
        dumpInternal(pw, "Last failed last-mile log", this.mLastMileLogForLastFailure);
        dumpInternal(pw, "Latest last-mile log", readTrace());
    }

    private void enableTracing() {
        if (!ensureFailSafeIsArmed()) {
            this.mLog.wC("Failed to arm fail-safe.");
            return;
        }
        try {
            FileUtils.stringToFile(this.mEventEnablePath, "1");
        } catch (IOException e) {
            this.mLog.warn("Failed to start event tracing: %").r(e.getMessage()).flush();
        }
    }

    private void disableTracing() {
        try {
            FileUtils.stringToFile(this.mEventEnablePath, "0");
        } catch (IOException e) {
            this.mLog.warn("Failed to stop event tracing: %").r(e.getMessage()).flush();
        }
    }

    private byte[] readTrace() {
        try {
            return IoUtils.readFileAsByteArray(this.mEventBufferPath);
        } catch (IOException e) {
            this.mLog.warn("Failed to read event trace: %").r(e.getMessage()).flush();
            return new byte[0];
        }
    }

    private boolean ensureFailSafeIsArmed() {
        if (this.mLastMileTraceHandle != null) {
            return true;
        }
        try {
            this.mLastMileTraceHandle = new FileInputStream(this.mEventReleasePath);
            return true;
        } catch (IOException e) {
            this.mLog.warn("Failed to open free_buffer pseudo-file: %").r(e.getMessage()).flush();
            return false;
        }
    }

    private static void dumpInternal(PrintWriter pw, String description, byte[] lastMileLog) {
        if (lastMileLog == null || lastMileLog.length < 1) {
            pw.format("No last mile log for \"%s\"\n", description);
            return;
        }
        pw.format("-------------------------- %s ---------------------------\n", description);
        pw.print(new String(lastMileLog));
        pw.println("--------------------------------------------------------------------");
    }
}
