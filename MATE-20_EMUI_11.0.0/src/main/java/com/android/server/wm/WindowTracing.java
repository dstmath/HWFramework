package com.android.server.wm;

import android.os.Build;
import android.os.ShellCommand;
import android.os.SystemClock;
import android.os.Trace;
import android.util.Log;
import android.util.proto.ProtoOutputStream;
import android.view.Choreographer;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

/* access modifiers changed from: package-private */
public class WindowTracing {
    private static final int BUFFER_CAPACITY_ALL = 4194304;
    private static final int BUFFER_CAPACITY_CRITICAL = 524288;
    private static final int BUFFER_CAPACITY_TRIM = 2097152;
    private static final String TAG = "WindowTracing";
    private static final String TRACE_FILENAME = "/data/misc/wmtrace/wm_trace.pb";
    private final WindowTraceBuffer mBuffer;
    private final Choreographer mChoreographer;
    private boolean mEnabled;
    private final Object mEnabledLock;
    private volatile boolean mEnabledLockFree;
    private final Choreographer.FrameCallback mFrameCallback;
    private final WindowManagerGlobalLock mGlobalLock;
    private int mLogLevel;
    private boolean mLogOnFrame;
    private boolean mScheduled;
    private final WindowManagerService mService;
    private final File mTraceFile;

    public /* synthetic */ void lambda$new$0$WindowTracing(long frameTimeNanos) {
        log("onFrame");
    }

    static WindowTracing createDefaultAndStartLooper(WindowManagerService service, Choreographer choreographer) {
        return new WindowTracing(new File(TRACE_FILENAME), service, choreographer, BUFFER_CAPACITY_TRIM);
    }

    private WindowTracing(File file, WindowManagerService service, Choreographer choreographer, int bufferCapacity) {
        this(file, service, choreographer, service.mGlobalLock, bufferCapacity);
    }

    WindowTracing(File file, WindowManagerService service, Choreographer choreographer, WindowManagerGlobalLock globalLock, int bufferCapacity) {
        this.mEnabledLock = new Object();
        this.mFrameCallback = new Choreographer.FrameCallback() {
            /* class com.android.server.wm.$$Lambda$WindowTracing$lz89IHzR4nKO_ZtXtwyNGkRleMY */

            @Override // android.view.Choreographer.FrameCallback
            public final void doFrame(long j) {
                WindowTracing.this.lambda$new$0$WindowTracing(j);
            }
        };
        this.mLogLevel = 1;
        this.mLogOnFrame = false;
        this.mChoreographer = choreographer;
        this.mService = service;
        this.mGlobalLock = globalLock;
        this.mTraceFile = file;
        this.mBuffer = new WindowTraceBuffer(bufferCapacity);
        setLogLevel(1, null);
    }

    /* access modifiers changed from: package-private */
    public void startTrace(PrintWriter pw) {
        if (Build.IS_USER) {
            logAndPrintln(pw, "Error: Tracing is not supported on user builds.");
            return;
        }
        synchronized (this.mEnabledLock) {
            logAndPrintln(pw, "Start tracing to " + this.mTraceFile + ".");
            this.mBuffer.resetBuffer();
            this.mEnabledLockFree = true;
            this.mEnabled = true;
        }
        log("trace.enable");
    }

    /* access modifiers changed from: package-private */
    public void stopTrace(PrintWriter pw) {
        stopTrace(pw, true);
    }

    /* access modifiers changed from: package-private */
    public void stopTrace(PrintWriter pw, boolean writeToFile) {
        if (Build.IS_USER) {
            logAndPrintln(pw, "Error: Tracing is not supported on user builds.");
            return;
        }
        synchronized (this.mEnabledLock) {
            logAndPrintln(pw, "Stop tracing to " + this.mTraceFile + ". Waiting for traces to flush.");
            this.mEnabledLockFree = false;
            this.mEnabled = false;
            if (this.mEnabled) {
                logAndPrintln(pw, "ERROR: tracing was re-enabled while waiting for flush.");
                throw new IllegalStateException("tracing enabled while waiting for flush.");
            } else if (writeToFile) {
                writeTraceToFileLocked();
                logAndPrintln(pw, "Trace written to " + this.mTraceFile + ".");
            }
        }
    }

    private void setLogLevel(int logLevel, PrintWriter pw) {
        logAndPrintln(pw, "Setting window tracing log level to " + logLevel);
        this.mLogLevel = logLevel;
        if (logLevel == 0) {
            setBufferCapacity(BUFFER_CAPACITY_ALL, pw);
        } else if (logLevel == 1) {
            setBufferCapacity(BUFFER_CAPACITY_TRIM, pw);
        } else if (logLevel == 2) {
            setBufferCapacity(BUFFER_CAPACITY_CRITICAL, pw);
        }
    }

    private void setLogFrequency(boolean onFrame, PrintWriter pw) {
        StringBuilder sb = new StringBuilder();
        sb.append("Setting window tracing log frequency to ");
        sb.append(onFrame ? "frame" : "transaction");
        logAndPrintln(pw, sb.toString());
        this.mLogOnFrame = onFrame;
    }

    private void setBufferCapacity(int capacity, PrintWriter pw) {
        logAndPrintln(pw, "Setting window tracing buffer capacity to " + capacity + "bytes");
        this.mBuffer.setCapacity(capacity);
    }

    /* access modifiers changed from: package-private */
    public boolean isEnabled() {
        return this.mEnabledLockFree;
    }

    /* JADX INFO: Can't fix incorrect switch cases order, some code will duplicate */
    /* access modifiers changed from: package-private */
    public int onShellCommand(ShellCommand shell) {
        char c;
        PrintWriter pw = shell.getOutPrintWriter();
        String cmd = shell.getNextArgRequired();
        char c2 = 65535;
        switch (cmd.hashCode()) {
            case -892481550:
                if (cmd.equals("status")) {
                    c = 2;
                    break;
                }
                c = 65535;
                break;
            case 3530753:
                if (cmd.equals("size")) {
                    c = 6;
                    break;
                }
                c = 65535;
                break;
            case 3540994:
                if (cmd.equals("stop")) {
                    c = 1;
                    break;
                }
                c = 65535;
                break;
            case 97692013:
                if (cmd.equals("frame")) {
                    c = 3;
                    break;
                }
                c = 65535;
                break;
            case 102865796:
                if (cmd.equals("level")) {
                    c = 5;
                    break;
                }
                c = 65535;
                break;
            case 109757538:
                if (cmd.equals("start")) {
                    c = 0;
                    break;
                }
                c = 65535;
                break;
            case 2141246174:
                if (cmd.equals("transaction")) {
                    c = 4;
                    break;
                }
                c = 65535;
                break;
            default:
                c = 65535;
                break;
        }
        switch (c) {
            case 0:
                startTrace(pw);
                return 0;
            case 1:
                stopTrace(pw);
                return 0;
            case 2:
                logAndPrintln(pw, getStatus());
                return 0;
            case 3:
                setLogFrequency(true, pw);
                this.mBuffer.resetBuffer();
                return 0;
            case 4:
                setLogFrequency(false, pw);
                this.mBuffer.resetBuffer();
                return 0;
            case 5:
                String logLevelStr = shell.getNextArgRequired().toLowerCase();
                int hashCode = logLevelStr.hashCode();
                if (hashCode != 96673) {
                    if (hashCode != 3568674) {
                        if (hashCode == 1952151455 && logLevelStr.equals("critical")) {
                            c2 = 2;
                        }
                    } else if (logLevelStr.equals("trim")) {
                        c2 = 1;
                    }
                } else if (logLevelStr.equals("all")) {
                    c2 = 0;
                }
                if (c2 == 0) {
                    setLogLevel(0, pw);
                } else if (c2 == 1) {
                    setLogLevel(1, pw);
                } else if (c2 != 2) {
                    setLogLevel(1, pw);
                } else {
                    setLogLevel(2, pw);
                }
                this.mBuffer.resetBuffer();
                return 0;
            case 6:
                setBufferCapacity(Integer.parseInt(shell.getNextArgRequired()) * 1024, pw);
                this.mBuffer.resetBuffer();
                return 0;
            default:
                pw.println("Unknown command: " + cmd);
                pw.println("Window manager trace options:");
                pw.println("  start: Start logging");
                pw.println("  stop: Stop logging");
                pw.println("  frame: Log trace once per frame");
                pw.println("  transaction: Log each transaction");
                pw.println("  size: Set the maximum log size (in KB)");
                pw.println("  status: Print trace status");
                pw.println("  level [lvl]: Set the log level between");
                pw.println("    lvl may be one of:");
                pw.println("      critical: Only visible windows with reduced information");
                pw.println("      trim: All windows with reduced");
                pw.println("      all: All window and information");
                return -1;
        }
    }

    /* access modifiers changed from: package-private */
    public String getStatus() {
        StringBuilder sb = new StringBuilder();
        sb.append("Status: ");
        sb.append(isEnabled() ? "Enabled" : "Disabled");
        sb.append("\nLog level: ");
        sb.append(this.mLogLevel);
        sb.append("\n");
        sb.append(this.mBuffer.getStatus());
        return sb.toString();
    }

    /* access modifiers changed from: package-private */
    public void logState(String where) {
        if (isEnabled()) {
            if (this.mLogOnFrame) {
                schedule();
            } else {
                log(where);
            }
        }
    }

    private void schedule() {
        if (!this.mScheduled) {
            this.mScheduled = true;
            this.mChoreographer.postFrameCallback(this.mFrameCallback);
        }
    }

    /* JADX INFO: finally extract failed */
    private void log(String where) {
        Trace.traceBegin(32, "traceStateLocked");
        try {
            ProtoOutputStream os = new ProtoOutputStream();
            long tokenOuter = os.start(2246267895810L);
            os.write(1125281431553L, SystemClock.elapsedRealtimeNanos());
            os.write(1138166333442L, where);
            long tokenInner = os.start(1146756268035L);
            synchronized (this.mGlobalLock) {
                try {
                    WindowManagerService.boostPriorityForLockedSection();
                    Trace.traceBegin(32, "writeToProtoLocked");
                    try {
                        this.mService.writeToProtoLocked(os, this.mLogLevel);
                    } finally {
                        Trace.traceEnd(32);
                    }
                } catch (Throwable th) {
                    WindowManagerService.resetPriorityAfterLockedSection();
                    throw th;
                }
            }
            WindowManagerService.resetPriorityAfterLockedSection();
            os.end(tokenInner);
            os.end(tokenOuter);
            this.mBuffer.add(os);
            this.mScheduled = false;
        } catch (Exception e) {
            Log.wtf(TAG, "Exception while tracing state", e);
        } catch (Throwable th2) {
            Trace.traceEnd(32);
            throw th2;
        }
        Trace.traceEnd(32);
    }

    /* access modifiers changed from: package-private */
    public void writeTraceToFile() {
        synchronized (this.mEnabledLock) {
            writeTraceToFileLocked();
        }
    }

    private void logAndPrintln(PrintWriter pw, String msg) {
        Log.i(TAG, msg);
        if (pw != null) {
            pw.println(msg);
            pw.flush();
        }
    }

    private void writeTraceToFileLocked() {
        try {
            Trace.traceBegin(32, "writeTraceToFileLocked");
            this.mBuffer.writeTraceToFile(this.mTraceFile);
        } catch (IOException e) {
            Log.e(TAG, "Unable to write buffer to file", e);
        } catch (Throwable th) {
            Trace.traceEnd(32);
            throw th;
        }
        Trace.traceEnd(32);
    }
}
