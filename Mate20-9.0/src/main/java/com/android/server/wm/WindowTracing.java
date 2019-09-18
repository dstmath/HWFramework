package com.android.server.wm;

import android.content.Context;
import android.os.Build;
import android.os.ShellCommand;
import android.os.SystemClock;
import android.os.Trace;
import android.util.Log;
import android.util.proto.ProtoOutputStream;
import com.android.internal.annotations.VisibleForTesting;
import com.android.server.display.DisplayTransformManager;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Objects;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

class WindowTracing {
    private static final long MAGIC_NUMBER_VALUE = 4990904633914181975L;
    private static final String TAG = "WindowTracing";
    private boolean mEnabled;
    private volatile boolean mEnabledLockFree;
    private final Object mLock = new Object();
    private final File mTraceFile;
    private final BlockingQueue<ProtoOutputStream> mWriteQueue = new ArrayBlockingQueue(DisplayTransformManager.LEVEL_COLOR_MATRIX_GRAYSCALE);

    WindowTracing(File file) {
        this.mTraceFile = file;
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x005f, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:?, code lost:
        $closeResource(r2, r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0066, code lost:
        throw r3;
     */
    public void startTrace(PrintWriter pw) throws IOException {
        if (Build.IS_USER) {
            logAndPrintln(pw, "Error: Tracing is not supported on user builds.");
            return;
        }
        synchronized (this.mLock) {
            logAndPrintln(pw, "Start tracing to " + this.mTraceFile + ".");
            this.mWriteQueue.clear();
            this.mTraceFile.delete();
            OutputStream os = new FileOutputStream(this.mTraceFile);
            this.mTraceFile.setReadable(true, false);
            ProtoOutputStream proto = new ProtoOutputStream(os);
            proto.write(1125281431553L, MAGIC_NUMBER_VALUE);
            proto.flush();
            $closeResource(null, os);
            this.mEnabledLockFree = true;
            this.mEnabled = true;
        }
    }

    private static /* synthetic */ void $closeResource(Throwable x0, AutoCloseable x1) {
        if (x0 != null) {
            try {
                x1.close();
            } catch (Throwable th) {
                x0.addSuppressed(th);
            }
        } else {
            x1.close();
        }
    }

    private void logAndPrintln(PrintWriter pw, String msg) {
        Log.i(TAG, msg);
        if (pw != null) {
            pw.println(msg);
            pw.flush();
        }
    }

    /* access modifiers changed from: package-private */
    public void stopTrace(PrintWriter pw) {
        if (Build.IS_USER) {
            logAndPrintln(pw, "Error: Tracing is not supported on user builds.");
            return;
        }
        synchronized (this.mLock) {
            logAndPrintln(pw, "Stop tracing to " + this.mTraceFile + ". Waiting for traces to flush.");
            this.mEnabledLockFree = false;
            this.mEnabled = false;
            while (!this.mWriteQueue.isEmpty()) {
                if (!this.mEnabled) {
                    try {
                        this.mLock.wait();
                        this.mLock.notify();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                } else {
                    logAndPrintln(pw, "ERROR: tracing was re-enabled while waiting for flush.");
                    throw new IllegalStateException("tracing enabled while waiting for flush.");
                }
            }
            logAndPrintln(pw, "Trace written to " + this.mTraceFile + ".");
        }
    }

    /* access modifiers changed from: package-private */
    public void appendTraceEntry(ProtoOutputStream proto) {
        if (this.mEnabledLockFree && !this.mWriteQueue.offer(proto)) {
            Log.e(TAG, "Dropping window trace entry, queue full");
        }
    }

    /* access modifiers changed from: package-private */
    public void loop() {
        while (true) {
            loopOnce();
        }
    }

    /* access modifiers changed from: package-private */
    @VisibleForTesting
    public void loopOnce() {
        OutputStream os;
        try {
            ProtoOutputStream proto = this.mWriteQueue.take();
            synchronized (this.mLock) {
                try {
                    Trace.traceBegin(32, "writeToFile");
                    os = new FileOutputStream(this.mTraceFile, true);
                    os.write(proto.getBytes());
                    $closeResource(null, os);
                    Trace.traceEnd(32);
                } catch (IOException e) {
                    try {
                        Log.e(TAG, "Failed to write file " + this.mTraceFile, e);
                        Trace.traceEnd(32);
                    } catch (Throwable th) {
                        Trace.traceEnd(32);
                        throw th;
                    }
                } catch (Throwable th2) {
                    $closeResource(r5, os);
                    throw th2;
                }
                this.mLock.notify();
            }
        } catch (InterruptedException e2) {
            Thread.currentThread().interrupt();
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isEnabled() {
        return this.mEnabledLockFree;
    }

    static WindowTracing createDefaultAndStartLooper(Context context) {
        WindowTracing windowTracing = new WindowTracing(new File("/data/misc/wmtrace/wm_trace.pb"));
        if (!Build.IS_USER) {
            Objects.requireNonNull(windowTracing);
            new Thread(new Runnable() {
                public final void run() {
                    WindowTracing.this.loop();
                }
            }, "window_tracing").start();
        }
        return windowTracing;
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:15:0x002f A[Catch:{ IOException -> 0x004d }] */
    /* JADX WARNING: Removed duplicated region for block: B:16:0x0032 A[Catch:{ IOException -> 0x004d }] */
    /* JADX WARNING: Removed duplicated region for block: B:18:0x0036 A[Catch:{ IOException -> 0x004d }] */
    public int onShellCommand(ShellCommand shell, String cmd) {
        boolean z;
        PrintWriter pw = shell.getOutPrintWriter();
        try {
            int hashCode = cmd.hashCode();
            if (hashCode == 3540994) {
                if (cmd.equals("stop")) {
                    z = true;
                    switch (z) {
                        case false:
                            break;
                        case true:
                            break;
                    }
                }
            } else if (hashCode == 109757538) {
                if (cmd.equals("start")) {
                    z = false;
                    switch (z) {
                        case false:
                            startTrace(pw);
                            return 0;
                        case true:
                            stopTrace(pw);
                            return 0;
                        default:
                            pw.println("Unknown command: " + cmd);
                            return -1;
                    }
                }
            }
            z = true;
            switch (z) {
                case false:
                    break;
                case true:
                    break;
            }
        } catch (IOException e) {
            logAndPrintln(pw, e.toString());
            throw new RuntimeException(e);
        }
    }

    /* access modifiers changed from: package-private */
    public void traceStateLocked(String where, WindowManagerService service) {
        if (isEnabled()) {
            ProtoOutputStream os = new ProtoOutputStream();
            long tokenOuter = os.start(2246267895810L);
            os.write(1125281431553L, SystemClock.elapsedRealtimeNanos());
            os.write(1138166333442L, where);
            Trace.traceBegin(32, "writeToProtoLocked");
            try {
                long tokenInner = os.start(1146756268035L);
                service.writeToProtoLocked(os, true);
                os.end(tokenInner);
                Trace.traceEnd(32);
                os.end(tokenOuter);
                appendTraceEntry(os);
            } finally {
                Trace.traceEnd(32);
            }
        }
    }
}
