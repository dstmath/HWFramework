package com.android.server.wifi;

import android.os.FileUtils;
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
    private long mPendingConnectionId;

    public LastMileLogger(WifiInjector injector) {
        this(injector, WIFI_EVENT_BUFFER_PATH, WIFI_EVENT_ENABLE_PATH, WIFI_EVENT_RELEASE_PATH);
    }

    public LastMileLogger(WifiInjector injector, String bufferPath, String enablePath, String releasePath) {
        this.mPendingConnectionId = -1;
        this.mLog = injector.makeLog(TAG);
        this.mEventBufferPath = bufferPath;
        this.mEventEnablePath = enablePath;
        this.mEventReleasePath = releasePath;
    }

    public void reportConnectionEvent(long connectionId, byte event) {
        if (connectionId < 0) {
            this.mLog.warn("Ignoring negative connection id: %").c(connectionId);
            return;
        }
        switch (event) {
            case (byte) 0:
                this.mPendingConnectionId = connectionId;
                enableTracing();
                return;
            case (byte) 1:
                this.mPendingConnectionId = -1;
                disableTracing();
                return;
            case (byte) 2:
                if (connectionId >= this.mPendingConnectionId) {
                    this.mPendingConnectionId = -1;
                    disableTracing();
                    this.mLastMileLogForLastFailure = readTrace();
                    return;
                }
                return;
            default:
                return;
        }
    }

    public void dump(PrintWriter pw) {
        dumpInternal(pw, "Last failed last-mile log", this.mLastMileLogForLastFailure);
        dumpInternal(pw, "Latest last-mile log", readTrace());
        this.mLastMileLogForLastFailure = null;
    }

    private void enableTracing() {
        if (ensureFailSafeIsArmed()) {
            try {
                FileUtils.stringToFile(this.mEventEnablePath, "1");
            } catch (IOException e) {
                this.mLog.warn("Failed to start event tracing: %").r(e.getMessage()).flush();
            }
            return;
        }
        this.mLog.wC("Failed to arm fail-safe.");
    }

    private void disableTracing() {
        try {
            FileUtils.stringToFile(this.mEventEnablePath, HwWifiCHRStateManager.TYPE_AP_VENDOR);
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
            pw.format("No last mile log for \"%s\"\n", new Object[]{description});
            return;
        }
        pw.format("-------------------------- %s ---------------------------\n", new Object[]{description});
        pw.print(new String(lastMileLog));
        pw.println("--------------------------------------------------------------------");
    }
}
