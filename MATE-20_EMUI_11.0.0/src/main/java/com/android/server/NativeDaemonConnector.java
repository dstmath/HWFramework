package com.android.server;

import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.util.LocalLog;
import android.util.Log;
import android.util.Slog;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.util.Preconditions;
import com.android.server.Watchdog;
import com.android.server.power.ShutdownThread;
import com.google.android.collect.Lists;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

/* access modifiers changed from: package-private */
public final class NativeDaemonConnector implements Runnable, Handler.Callback, Watchdog.Monitor {
    private static boolean DEBUG_NETD = false;
    private static boolean DEBUG_ON = SystemProperties.getBoolean("persist.sys.huawei.debug.on", false);
    private static final long DEFAULT_TIMEOUT = 60000;
    private static final boolean VDBG = false;
    private static final long WARN_EXECUTE_DELAY_MS = 500;
    private static final String encryption_ip = "\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}";
    private final int BUFFER_SIZE;
    private final String TAG;
    private Handler mCallbackHandler;
    private INativeDaemonConnectorCallbacks mCallbacks;
    private final Object mDaemonLock;
    private volatile boolean mDebug;
    private LocalLog mLocalLog;
    private final Looper mLooper;
    private OutputStream mOutputStream;
    private final ResponseQueue mResponseQueue;
    private AtomicInteger mSequenceNumber;
    private String mSocket;
    private final PowerManager.WakeLock mWakeLock;
    private volatile Object mWarnIfHeld;
    private HwNativeDaemonConnector mhwNativeDaemonConnector;

    static {
        boolean z = false;
        if (Log.HWModuleLog || DEBUG_ON) {
            z = true;
        }
        DEBUG_NETD = z;
    }

    NativeDaemonConnector(INativeDaemonConnectorCallbacks callbacks, String socket, int responseQueueSize, String logTag, int maxLogSize, PowerManager.WakeLock wl) {
        this(callbacks, socket, responseQueueSize, logTag, maxLogSize, wl, FgThread.get().getLooper());
    }

    NativeDaemonConnector(INativeDaemonConnectorCallbacks callbacks, String socket, int responseQueueSize, String logTag, int maxLogSize, PowerManager.WakeLock wl, Looper looper) {
        this.mDebug = false;
        this.mDaemonLock = new Object();
        this.BUFFER_SIZE = 4096;
        this.mCallbacks = callbacks;
        this.mSocket = socket;
        this.mResponseQueue = new ResponseQueue(responseQueueSize);
        this.mWakeLock = wl;
        PowerManager.WakeLock wakeLock = this.mWakeLock;
        if (wakeLock != null) {
            wakeLock.setReferenceCounted(true);
        }
        this.mLooper = looper;
        this.mSequenceNumber = new AtomicInteger(0);
        this.TAG = logTag != null ? logTag : "NativeDaemonConnector";
        this.mLocalLog = new LocalLog(maxLogSize);
    }

    public void setDebug(boolean debug) {
        this.mDebug = debug;
    }

    private int uptimeMillisInt() {
        return ((int) SystemClock.uptimeMillis()) & Integer.MAX_VALUE;
    }

    public void setWarnIfHeld(Object warnIfHeld) {
        Preconditions.checkState(this.mWarnIfHeld == null);
        this.mWarnIfHeld = Preconditions.checkNotNull(warnIfHeld);
    }

    @Override // java.lang.Runnable
    public void run() {
        this.mCallbackHandler = new Handler(this.mLooper, this);
        while (!isShuttingDown()) {
            try {
                listenToSocket();
            } catch (Exception e) {
                loge("Error in NativeDaemonConnector: " + e);
                if (!isShuttingDown()) {
                    SystemClock.sleep(5000);
                } else {
                    return;
                }
            }
        }
    }

    private static boolean isShuttingDown() {
        String shutdownAct = SystemProperties.get(ShutdownThread.SHUTDOWN_ACTION_PROPERTY, "");
        return shutdownAct != null && shutdownAct.length() > 0;
    }

    @Override // android.os.Handler.Callback
    public boolean handleMessage(Message msg) {
        Object[] objArr;
        PowerManager.WakeLock wakeLock;
        int end;
        PowerManager.WakeLock wakeLock2;
        PowerManager.WakeLock wakeLock3;
        String event = (String) msg.obj;
        event.replaceAll(encryption_ip, " ******** ");
        log("RCV unsolicited event from native daemon, event = " + msg.what);
        int start = uptimeMillisInt();
        int sent = msg.arg1;
        try {
            if (!this.mCallbacks.onEvent(msg.what, event, NativeDaemonEvent.unescapeArgs(event))) {
                log(String.format("Unhandled event '%s'", event));
            }
            if (this.mCallbacks.onCheckHoldWakeLock(msg.what) && (wakeLock3 = this.mWakeLock) != null) {
                wakeLock3.release();
            }
            int end2 = uptimeMillisInt();
            if (start > sent && ((long) (start - sent)) > 500) {
                loge(String.format("NDC event {%s} processed too late: %dms", event, Integer.valueOf(start - sent)));
            }
            if (end2 > start && ((long) (end2 - start)) > 500) {
                objArr = new Object[]{event, Integer.valueOf(end2 - start)};
                loge(String.format("NDC event {%s} took too long: %dms", objArr));
            }
        } catch (Exception e) {
            loge("Error handling '" + event + "': " + e);
            if (this.mCallbacks.onCheckHoldWakeLock(msg.what) && (wakeLock = this.mWakeLock) != null) {
                wakeLock.release();
            }
            int end3 = uptimeMillisInt();
            if (start > sent && ((long) (start - sent)) > 500) {
                loge(String.format("NDC event {%s} processed too late: %dms", event, Integer.valueOf(start - sent)));
            }
            if (end3 > start && ((long) (end3 - start)) > 500) {
                objArr = new Object[]{event, Integer.valueOf(end3 - start)};
            }
        } catch (Throwable th) {
            if (this.mCallbacks.onCheckHoldWakeLock(msg.what) && (wakeLock2 = this.mWakeLock) != null) {
                wakeLock2.release();
            }
            int end4 = uptimeMillisInt();
            if (start > sent) {
                end = end4;
                if (((long) (start - sent)) > 500) {
                    loge(String.format("NDC event {%s} processed too late: %dms", event, Integer.valueOf(start - sent)));
                }
            } else {
                end = end4;
            }
            if (end > start && ((long) (end - start)) > 500) {
                loge(String.format("NDC event {%s} took too long: %dms", event, Integer.valueOf(end - start)));
            }
            throw th;
        }
        return true;
    }

    private LocalSocketAddress determineSocketAddress() {
        if (!this.mSocket.startsWith("__test__") || !Build.IS_DEBUGGABLE) {
            return new LocalSocketAddress(this.mSocket, LocalSocketAddress.Namespace.RESERVED);
        }
        return new LocalSocketAddress(this.mSocket);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:127:0x02ef, code lost:
        r0 = th;
     */
    /* JADX WARNING: Removed duplicated region for block: B:103:0x028f  */
    /* JADX WARNING: Removed duplicated region for block: B:107:0x0299  */
    /* JADX WARNING: Removed duplicated region for block: B:140:0x0319 A[SYNTHETIC] */
    private void listenToSocket() throws IOException {
        LocalSocket socket;
        Throwable th;
        IOException ex;
        int count;
        boolean z;
        char c;
        int i;
        InputStream inputStream;
        LocalSocketAddress address;
        String rawEventLog;
        Throwable th2;
        IllegalArgumentException e;
        PowerManager.WakeLock wakeLock;
        LocalSocket socket2 = null;
        log("listenToSocket enter");
        OutputStream outputStream = null;
        try {
            socket2 = new LocalSocket();
            try {
                LocalSocketAddress address2 = determineSocketAddress();
                socket2.connect(address2);
                InputStream inputStream2 = socket2.getInputStream();
                synchronized (this.mDaemonLock) {
                    this.mOutputStream = socket2.getOutputStream();
                }
                this.mCallbacks.onDaemonConnected();
                byte[] buffer = new byte[4096];
                int start = 0;
                while (true) {
                    count = inputStream2.read(buffer, start, 4096 - start);
                    if (count < 0) {
                        break;
                    }
                    FileDescriptor[] fdList = socket2.getAncillaryFileDescriptors();
                    int count2 = count + start;
                    int i2 = 0;
                    int start2 = 0;
                    while (i2 < count2) {
                        if (buffer[i2] == 0) {
                            String rawEvent = new String(buffer, start2, i2 - start2, StandardCharsets.UTF_8);
                            Pattern pattern = Pattern.compile("([0-9a-zA-Z]{2}:){4}[0-9a-zA-Z]{2}");
                            String rawEventLog2 = rawEvent.replaceAll(encryption_ip, " ******** ");
                            if (pattern.matcher(rawEventLog2).find()) {
                                String rawEventLog3 = rawEventLog2.replaceAll("([0-9a-zA-Z]{2}:){4}[0-9a-zA-Z]{2}", " ******** ");
                                log("RCV <- {" + rawEventLog3 + "}");
                                rawEventLog = rawEventLog3;
                            } else if (!shouldPrintEvent()) {
                                String rawEventLog4 = rawEventLog2.replaceAll("[A-Fa-f0-9]{2,}:{1,}", "****");
                                log("RCV <- {" + rawEventLog4 + "}");
                                rawEventLog = rawEventLog4;
                            } else {
                                rawEventLog = rawEventLog2;
                            }
                            boolean releaseWl = false;
                            try {
                                NativeDaemonEvent event = NativeDaemonEvent.parseRawEvent(rawEvent, fdList);
                                socket = socket2;
                                try {
                                    address = address2;
                                    inputStream = inputStream2;
                                    try {
                                        String eventLog = event.toString().replaceAll(encryption_ip, " ******** ");
                                        if (shouldPrintEvent()) {
                                            try {
                                                log("RCV <- {" + event + "}");
                                            } catch (IllegalArgumentException e2) {
                                                e = e2;
                                            } catch (Throwable th3) {
                                                th2 = th3;
                                                if (releaseWl) {
                                                }
                                                throw th2;
                                            }
                                        } else if (pattern.matcher(eventLog).find()) {
                                            eventLog = eventLog.replaceAll("([0-9a-zA-Z]{2}:){4}[0-9a-zA-Z]{2}", " ******** ");
                                            log("RCV <- {" + eventLog + "}");
                                        } else {
                                            eventLog = eventLog.replaceAll("[A-Fa-f0-9]{2,}:{1,}", "****");
                                            log("RCV <- {" + eventLog + "}");
                                        }
                                        if (event.isClassUnsolicited()) {
                                            if (this.mCallbacks.onCheckHoldWakeLock(event.getCode()) && this.mWakeLock != null) {
                                                this.mWakeLock.acquire();
                                                releaseWl = true;
                                            }
                                            try {
                                                try {
                                                    if (this.mCallbackHandler.sendMessage(this.mCallbackHandler.obtainMessage(event.getCode(), uptimeMillisInt(), 0, event.getRawEvent()))) {
                                                        releaseWl = false;
                                                    }
                                                } catch (IllegalArgumentException e3) {
                                                    e = e3;
                                                    try {
                                                        log("Problem parsing message " + e);
                                                        if (releaseWl) {
                                                        }
                                                        start2 = i2 + 1;
                                                        i2++;
                                                        socket2 = socket;
                                                        address2 = address;
                                                        inputStream2 = inputStream;
                                                    } catch (Throwable th4) {
                                                        th2 = th4;
                                                        if (releaseWl) {
                                                        }
                                                        throw th2;
                                                    }
                                                }
                                            } catch (IllegalArgumentException e4) {
                                                e = e4;
                                                log("Problem parsing message " + e);
                                                if (releaseWl) {
                                                }
                                                start2 = i2 + 1;
                                                i2++;
                                                socket2 = socket;
                                                address2 = address;
                                                inputStream2 = inputStream;
                                            } catch (Throwable th5) {
                                                th2 = th5;
                                                if (releaseWl) {
                                                }
                                                throw th2;
                                            }
                                        } else {
                                            this.mResponseQueue.add(event.getCmdNumber(), event);
                                        }
                                        if (releaseWl) {
                                            try {
                                                wakeLock = this.mWakeLock;
                                                wakeLock.release();
                                            } catch (IOException e5) {
                                                ex = e5;
                                                socket2 = socket;
                                                try {
                                                    loge("Communications error: " + ex);
                                                    throw ex;
                                                } catch (Throwable th6) {
                                                    socket = socket2;
                                                    th = th6;
                                                    synchronized (this.mDaemonLock) {
                                                    }
                                                }
                                            } catch (Throwable th7) {
                                                th = th7;
                                                synchronized (this.mDaemonLock) {
                                                }
                                            }
                                        }
                                    } catch (IllegalArgumentException e6) {
                                        e = e6;
                                        log("Problem parsing message " + e);
                                        if (releaseWl) {
                                        }
                                        start2 = i2 + 1;
                                        i2++;
                                        socket2 = socket;
                                        address2 = address;
                                        inputStream2 = inputStream;
                                    } catch (Throwable th8) {
                                        th2 = th8;
                                        if (releaseWl) {
                                        }
                                        throw th2;
                                    }
                                } catch (IllegalArgumentException e7) {
                                    e = e7;
                                    address = address2;
                                    inputStream = inputStream2;
                                    log("Problem parsing message " + e);
                                    if (releaseWl) {
                                    }
                                    start2 = i2 + 1;
                                    i2++;
                                    socket2 = socket;
                                    address2 = address;
                                    inputStream2 = inputStream;
                                } catch (Throwable th9) {
                                    th2 = th9;
                                    if (releaseWl) {
                                    }
                                    throw th2;
                                }
                            } catch (IllegalArgumentException e8) {
                                e = e8;
                                socket = socket2;
                                address = address2;
                                inputStream = inputStream2;
                                log("Problem parsing message " + e);
                                if (releaseWl) {
                                    wakeLock = this.mWakeLock;
                                    wakeLock.release();
                                }
                                start2 = i2 + 1;
                                i2++;
                                socket2 = socket;
                                address2 = address;
                                inputStream2 = inputStream;
                            } catch (Throwable th10) {
                                th2 = th10;
                                if (releaseWl) {
                                    this.mWakeLock.release();
                                }
                                throw th2;
                            }
                            start2 = i2 + 1;
                        } else {
                            socket = socket2;
                            address = address2;
                            inputStream = inputStream2;
                        }
                        i2++;
                        socket2 = socket;
                        address2 = address;
                        inputStream2 = inputStream;
                    }
                    if (start2 == 0) {
                        log("RCV incomplete");
                    }
                    if (start2 != count2) {
                        c = 4096;
                        int remaining = 4096 - start2;
                        z = false;
                        System.arraycopy(buffer, start2, buffer, 0, remaining);
                        i = remaining;
                    } else {
                        c = 4096;
                        z = false;
                        i = 0;
                    }
                    start = i;
                    socket2 = socket2;
                    address2 = address2;
                    inputStream2 = inputStream2;
                    outputStream = null;
                }
                loge("got " + count + " reading with start = " + start);
                synchronized (this.mDaemonLock) {
                    if (this.mOutputStream != null) {
                        try {
                            loge("closing stream for " + this.mSocket);
                            this.mOutputStream.close();
                        } catch (IOException e9) {
                            loge("Failed closing output stream: " + e9);
                        }
                        this.mOutputStream = outputStream;
                    }
                }
                try {
                    socket2.close();
                    return;
                } catch (IOException ex2) {
                    loge("Failed closing socket: " + ex2);
                    return;
                }
                while (true) {
                }
            } catch (IOException e10) {
                ex = e10;
                loge("Communications error: " + ex);
                throw ex;
            } catch (Throwable th11) {
                socket = socket2;
                th = th11;
                synchronized (this.mDaemonLock) {
                    if (this.mOutputStream != null) {
                        try {
                            loge("closing stream for " + this.mSocket);
                            this.mOutputStream.close();
                        } catch (IOException e11) {
                            loge("Failed closing output stream: " + e11);
                        }
                        this.mOutputStream = null;
                    }
                }
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (IOException ex3) {
                        loge("Failed closing socket: " + ex3);
                    }
                }
                throw th;
            }
        } catch (IOException e12) {
            ex = e12;
            loge("Communications error: " + ex);
            throw ex;
        }
    }

    public static class SensitiveArg {
        private final Object mArg;

        public SensitiveArg(Object arg) {
            this.mArg = arg;
        }

        public String toString() {
            return String.valueOf(this.mArg);
        }
    }

    @VisibleForTesting
    static void makeCommand(StringBuilder rawBuilder, StringBuilder logBuilder, int sequenceNumber, String cmd, Object... args) {
        if (cmd.indexOf(0) >= 0) {
            if (DEBUG_NETD) {
                Slog.e("NDC makeCommand", "Unexpected command, cmd = " + cmd);
            }
            throw new IllegalArgumentException("Unexpected command: " + cmd);
        } else if (cmd.indexOf(32) >= 0) {
            if (DEBUG_NETD) {
                Slog.e("NDC makeCommand", "Error, arguments must be separate from command");
            }
            throw new IllegalArgumentException("Arguments must be separate from command");
        } else {
            rawBuilder.append(sequenceNumber);
            rawBuilder.append(' ');
            rawBuilder.append(cmd);
            logBuilder.append(sequenceNumber);
            logBuilder.append(' ');
            logBuilder.append(cmd);
            for (Object arg : args) {
                String argString = String.valueOf(arg);
                if (argString.indexOf(0) < 0) {
                    rawBuilder.append(' ');
                    logBuilder.append(' ');
                    appendEscaped(rawBuilder, argString);
                    if (arg instanceof SensitiveArg) {
                        logBuilder.append("[scrubbed]");
                    } else {
                        appendEscaped(logBuilder, argString);
                    }
                } else {
                    Slog.e("NDC makeCommand", "Unexpected argument: " + arg);
                    throw new IllegalArgumentException("Unexpected argument: " + arg);
                }
            }
            rawBuilder.append((char) 0);
        }
    }

    public void waitForCallbacks() {
        if (Thread.currentThread() != this.mLooper.getThread()) {
            final CountDownLatch latch = new CountDownLatch(1);
            this.mCallbackHandler.post(new Runnable() {
                /* class com.android.server.NativeDaemonConnector.AnonymousClass1 */

                @Override // java.lang.Runnable
                public void run() {
                    latch.countDown();
                }
            });
            try {
                latch.await();
            } catch (InterruptedException e) {
                Slog.wtf(this.TAG, "Interrupted while waiting for unsolicited response handling", e);
            }
        } else {
            throw new IllegalStateException("Must not call this method on callback thread");
        }
    }

    public NativeDaemonEvent execute(Command cmd) throws NativeDaemonConnectorException {
        return execute(cmd.mCmd, cmd.mArguments.toArray());
    }

    public NativeDaemonEvent execute(String cmd, Object... args) throws NativeDaemonConnectorException {
        return execute(60000, cmd, args);
    }

    public NativeDaemonEvent execute(long timeoutMs, String cmd, Object... args) throws NativeDaemonConnectorException {
        NativeDaemonEvent[] events = executeForList(timeoutMs, cmd, args);
        if (events.length == 1) {
            return events[0];
        }
        loge("Expected exactly one response, but receive more, throw NativeDaemonConnectorException");
        throw new NativeDaemonConnectorException("Expected exactly one response, but received " + events.length);
    }

    public NativeDaemonEvent[] executeForList(Command cmd) throws NativeDaemonConnectorException {
        return executeForList(cmd.mCmd, cmd.mArguments.toArray());
    }

    public NativeDaemonEvent[] executeForList(String cmd, Object... args) throws NativeDaemonConnectorException {
        return executeForList(60000, cmd, args);
    }

    private int countCharacterAppearTimes(String str, String character) {
        int x = 0;
        for (int i = 0; i <= str.length() - 1; i++) {
            if (str.substring(i, i + 1).equals(character)) {
                x++;
            }
        }
        return x;
    }

    public NativeDaemonEvent[] executeForList(long timeoutMs, String cmd, Object... args) throws NativeDaemonConnectorException {
        if (this.mWarnIfHeld != null && Thread.holdsLock(this.mWarnIfHeld)) {
            Slog.wtf(this.TAG, "Calling thread " + Thread.currentThread().getName() + " is holding 0x" + Integer.toHexString(System.identityHashCode(this.mWarnIfHeld)), new Throwable());
        }
        long startTime = SystemClock.elapsedRealtime();
        ArrayList<NativeDaemonEvent> events = Lists.newArrayList();
        StringBuilder rawBuilder = new StringBuilder();
        StringBuilder logBuilder = new StringBuilder();
        int sequenceNumber = this.mSequenceNumber.incrementAndGet();
        this.mhwNativeDaemonConnector = HwServiceFactory.getHwNativeDaemonConnector();
        makeCommand(rawBuilder, logBuilder, sequenceNumber, cmd, args);
        String rawCmd = rawBuilder.toString();
        String logCmd = logBuilder.toString();
        String encryption_logCmd = logCmd.replaceAll(encryption_ip, " ******** ").replaceAll("([A-Fa-f0-9]{2,}:{1,}){2,}", " **** ");
        if (shouldPrintEvent()) {
            log("SND -> {" + logCmd + "}");
        } else {
            log("SND -> {" + encryption_logCmd + "}");
        }
        synchronized (this.mDaemonLock) {
            try {
                if (this.mOutputStream != null) {
                    try {
                        this.mOutputStream.write(rawCmd.getBytes(StandardCharsets.UTF_8));
                    } catch (IOException e) {
                        loge("NDC problem sending command throw NativeDaemonConnectorException");
                        throw new NativeDaemonConnectorException("problem sending command", e);
                    } catch (Throwable th) {
                        th = th;
                        throw th;
                    }
                } else {
                    loge("NDC missing output stream throw NativeDaemonConnectorException");
                    throw new NativeDaemonConnectorException("missing output stream");
                }
            } catch (Throwable th2) {
                th = th2;
                throw th;
            }
        }
        while (true) {
            NativeDaemonEvent event = this.mResponseQueue.remove(sequenceNumber, timeoutMs, logCmd);
            if (event == null) {
                loge("timed-out waiting for response to " + logCmd);
                HwNativeDaemonConnector hwNativeDaemonConnector = this.mhwNativeDaemonConnector;
                if (hwNativeDaemonConnector != null) {
                    hwNativeDaemonConnector.reportChrForAddRouteFail(logCmd, null);
                }
                throw new NativeDaemonTimeoutException(logCmd, event);
            }
            String rmvLogStr = "RMV <- {" + event + "}";
            HwNativeDaemonConnector hwNativeDaemonConnector2 = this.mhwNativeDaemonConnector;
            if (hwNativeDaemonConnector2 != null) {
                hwNativeDaemonConnector2.reportChrForAddRouteFail(logCmd, rmvLogStr);
            }
            events.add(event);
            if (!event.isClassContinue()) {
                long endTime = SystemClock.elapsedRealtime();
                if (endTime - startTime > 500) {
                    loge("NDC Command {" + logCmd + "} took too long (" + (endTime - startTime) + "ms)");
                }
                if (event.isClassClientError()) {
                    loge("NDC client error throw NativeDaemonArgumentException");
                    throw new NativeDaemonArgumentException(logCmd, event);
                } else if (!event.isClassServerError()) {
                    return (NativeDaemonEvent[]) events.toArray(new NativeDaemonEvent[events.size()]);
                } else {
                    loge("NDC server error throw NativeDaemonFailureException");
                    throw new NativeDaemonFailureException(logCmd, event);
                }
            } else {
                rawBuilder = rawBuilder;
                logBuilder = logBuilder;
            }
        }
    }

    @Deprecated
    public ArrayList<String> doCommand(String cmd, Object... args) throws NativeDaemonConnectorException {
        ArrayList<String> rawEvents = Lists.newArrayList();
        for (NativeDaemonEvent event : executeForList(cmd, args)) {
            rawEvents.add(event.getRawEvent());
        }
        return rawEvents;
    }

    @Deprecated
    public String[] doListCommand(String cmd, int expectedCode, Object... args) throws NativeDaemonConnectorException {
        ArrayList<String> list = Lists.newArrayList();
        NativeDaemonEvent[] events = executeForList(cmd, args);
        for (int i = 0; i < events.length - 1; i++) {
            NativeDaemonEvent event = events[i];
            int code = event.getCode();
            if (code == expectedCode) {
                list.add(event.getMessage());
            } else {
                throw new NativeDaemonConnectorException("unexpected list response " + code + " instead of " + expectedCode);
            }
        }
        NativeDaemonEvent finalEvent = events[events.length - 1];
        if (finalEvent.isClassOk()) {
            return (String[]) list.toArray(new String[list.size()]);
        }
        throw new NativeDaemonConnectorException("unexpected final event: " + finalEvent);
    }

    @VisibleForTesting
    static void appendEscaped(StringBuilder builder, String arg) {
        boolean hasSpaces = arg.indexOf(32) >= 0;
        if (hasSpaces) {
            builder.append('\"');
        }
        int length = arg.length();
        for (int i = 0; i < length; i++) {
            char c = arg.charAt(i);
            if (c == '\"') {
                builder.append("\\\"");
            } else if (c == '\\') {
                builder.append("\\\\");
            } else {
                builder.append(c);
            }
        }
        if (hasSpaces) {
            builder.append('\"');
        }
    }

    /* access modifiers changed from: private */
    public static class NativeDaemonArgumentException extends NativeDaemonConnectorException {
        public NativeDaemonArgumentException(String command, NativeDaemonEvent event) {
            super(command, event);
        }

        @Override // com.android.server.NativeDaemonConnectorException
        public IllegalArgumentException rethrowAsParcelableException() {
            throw new IllegalArgumentException(getMessage(), this);
        }
    }

    /* access modifiers changed from: private */
    public static class NativeDaemonFailureException extends NativeDaemonConnectorException {
        public NativeDaemonFailureException(String command, NativeDaemonEvent event) {
            super(command, event);
        }
    }

    public static class Command {
        private ArrayList<Object> mArguments = Lists.newArrayList();
        private String mCmd;

        public Command(String cmd, Object... args) {
            this.mCmd = cmd;
            for (Object arg : args) {
                appendArg(arg);
            }
        }

        public Command appendArg(Object arg) {
            this.mArguments.add(arg);
            return this;
        }
    }

    @Override // com.android.server.Watchdog.Monitor
    public void monitor() {
        synchronized (this.mDaemonLock) {
        }
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        this.mLocalLog.dump(fd, pw, args);
        pw.println();
        this.mResponseQueue.dump(fd, pw, args);
    }

    private void log(String logstring) {
        if (this.mDebug) {
            Slog.d(this.TAG, logstring);
        }
        if (DEBUG_NETD) {
            this.mLocalLog.log(logstring);
        }
    }

    private void loge(String logstring) {
        if (DEBUG_NETD) {
            Slog.e(this.TAG, logstring);
            this.mLocalLog.log(logstring);
        }
    }

    /* access modifiers changed from: private */
    public static class ResponseQueue {
        private int mMaxCount;
        private final LinkedList<PendingCmd> mPendingCmds = new LinkedList<>();

        /* access modifiers changed from: private */
        public static class PendingCmd {
            public int availableResponseCount;
            public final int cmdNum;
            public final String logCmd;
            public BlockingQueue<NativeDaemonEvent> responses = new ArrayBlockingQueue(20);

            public PendingCmd(int cmdNum2, String logCmd2) {
                this.cmdNum = cmdNum2;
                this.logCmd = logCmd2;
            }
        }

        ResponseQueue(int maxCount) {
            this.mMaxCount = maxCount;
        }

        public void add(int cmdNum, NativeDaemonEvent response) {
            PendingCmd found = null;
            synchronized (this.mPendingCmds) {
                Iterator<PendingCmd> it = this.mPendingCmds.iterator();
                while (true) {
                    if (!it.hasNext()) {
                        break;
                    }
                    PendingCmd pendingCmd = it.next();
                    if (pendingCmd.cmdNum == cmdNum) {
                        found = pendingCmd;
                        break;
                    }
                }
                if (this.mPendingCmds.size() == 11) {
                    Slog.d("NativeDaemonConnector.ResponseQueue", "mPendingCmds.size = " + this.mPendingCmds.size());
                }
                if (found == null) {
                    while (this.mPendingCmds.size() >= this.mMaxCount) {
                        Slog.e("NativeDaemonConnector.ResponseQueue", "more buffered than allowed: " + this.mPendingCmds.size() + " >= " + this.mMaxCount);
                        PendingCmd pendingCmd2 = this.mPendingCmds.remove();
                        Slog.e("NativeDaemonConnector.ResponseQueue", "Removing request: " + pendingCmd2.logCmd + " (" + pendingCmd2.cmdNum + ")");
                    }
                    found = new PendingCmd(cmdNum, null);
                    this.mPendingCmds.add(found);
                }
                found.availableResponseCount++;
                if (found.availableResponseCount == 0) {
                    this.mPendingCmds.remove(found);
                }
            }
            try {
                found.responses.put(response);
            } catch (InterruptedException e) {
                if (NativeDaemonConnector.DEBUG_NETD) {
                    Slog.e("NDC put", "InterruptedException happen");
                }
            }
        }

        public NativeDaemonEvent remove(int cmdNum, long timeoutMs, String logCmd) {
            PendingCmd found = null;
            synchronized (this.mPendingCmds) {
                Iterator<PendingCmd> it = this.mPendingCmds.iterator();
                while (true) {
                    if (!it.hasNext()) {
                        break;
                    }
                    PendingCmd pendingCmd = it.next();
                    if (pendingCmd.cmdNum == cmdNum) {
                        found = pendingCmd;
                        break;
                    }
                }
                if (found == null) {
                    found = new PendingCmd(cmdNum, logCmd);
                    this.mPendingCmds.add(found);
                }
                found.availableResponseCount--;
                if (found.availableResponseCount == 0) {
                    this.mPendingCmds.remove(found);
                }
            }
            NativeDaemonEvent result = null;
            try {
                result = found.responses.poll(timeoutMs, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                if (NativeDaemonConnector.DEBUG_NETD) {
                    Slog.e("NDC poll", "InterruptedException happen");
                }
            }
            if (result == null) {
                Slog.e("NativeDaemonConnector.ResponseQueue", "Timeout waiting for response");
            }
            return result;
        }

        public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
            pw.println("Pending requests:");
            synchronized (this.mPendingCmds) {
                Iterator<PendingCmd> it = this.mPendingCmds.iterator();
                while (it.hasNext()) {
                    PendingCmd pendingCmd = it.next();
                    pw.println("  Cmd " + pendingCmd.cmdNum + " - " + pendingCmd.logCmd);
                }
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:17:0x003a A[ADDED_TO_REGION] */
    private boolean shouldPrintEvent() {
        char c;
        String str = this.TAG;
        int hashCode = str.hashCode();
        if (hashCode != -1339953786) {
            if (hashCode != 136483132) {
                if (hashCode == 1813268503 && str.equals("CryptdConnector")) {
                    c = 2;
                    return c != 0 || c == 1 || c == 2;
                }
            } else if (str.equals("VoldConnector")) {
                c = 1;
                if (c != 0) {
                }
            }
        } else if (str.equals("SdCryptdConnector")) {
            c = 0;
            if (c != 0) {
            }
        }
        c = 65535;
        if (c != 0) {
        }
    }
}
