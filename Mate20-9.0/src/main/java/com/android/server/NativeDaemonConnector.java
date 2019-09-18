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
import com.android.server.os.HwBootFail;
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

final class NativeDaemonConnector implements Runnable, Handler.Callback, Watchdog.Monitor {
    /* access modifiers changed from: private */
    public static boolean DEBUG_NETD = false;
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

    public static class Command {
        /* access modifiers changed from: private */
        public ArrayList<Object> mArguments = Lists.newArrayList();
        /* access modifiers changed from: private */
        public String mCmd;

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

    private static class NativeDaemonArgumentException extends NativeDaemonConnectorException {
        public NativeDaemonArgumentException(String command, NativeDaemonEvent event) {
            super(command, event);
        }

        public IllegalArgumentException rethrowAsParcelableException() {
            throw new IllegalArgumentException(getMessage(), this);
        }
    }

    private static class NativeDaemonFailureException extends NativeDaemonConnectorException {
        public NativeDaemonFailureException(String command, NativeDaemonEvent event) {
            super(command, event);
        }
    }

    private static class ResponseQueue {
        private int mMaxCount;
        private final LinkedList<PendingCmd> mPendingCmds = new LinkedList<>();

        private static class PendingCmd {
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
                Iterator it = this.mPendingCmds.iterator();
                while (true) {
                    if (!it.hasNext()) {
                        break;
                    }
                    PendingCmd pendingCmd = (PendingCmd) it.next();
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
                Iterator it = this.mPendingCmds.iterator();
                while (true) {
                    if (!it.hasNext()) {
                        break;
                    }
                    PendingCmd pendingCmd = (PendingCmd) it.next();
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
                Iterator it = this.mPendingCmds.iterator();
                while (it.hasNext()) {
                    PendingCmd pendingCmd = (PendingCmd) it.next();
                    pw.println("  Cmd " + pendingCmd.cmdNum + " - " + pendingCmd.logCmd);
                }
            }
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
        if (this.mWakeLock != null) {
            this.mWakeLock.setReferenceCounted(true);
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
        return ((int) SystemClock.uptimeMillis()) & HwBootFail.STAGE_BOOT_SUCCESS;
    }

    public void setWarnIfHeld(Object warnIfHeld) {
        Preconditions.checkState(this.mWarnIfHeld == null);
        this.mWarnIfHeld = Preconditions.checkNotNull(warnIfHeld);
    }

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
        String shutdownAct = SystemProperties.get(ShutdownThread.SHUTDOWN_ACTION_PROPERTY, BackupManagerConstants.DEFAULT_BACKUP_FINISHED_NOTIFICATION_RECEIVERS);
        return shutdownAct != null && shutdownAct.length() > 0;
    }

    public boolean handleMessage(Message msg) {
        String str;
        Object[] objArr;
        String event = (String) msg.obj;
        String replaceAll = event.replaceAll(encryption_ip, " ******** ");
        log("RCV unsolicited event from native daemon, event = " + msg.what);
        int start = uptimeMillisInt();
        int sent = msg.arg1;
        try {
            if (!this.mCallbacks.onEvent(msg.what, event, NativeDaemonEvent.unescapeArgs(event))) {
                log(String.format("Unhandled event '%s'", new Object[]{event}));
            }
            if (this.mCallbacks.onCheckHoldWakeLock(msg.what) && this.mWakeLock != null) {
                this.mWakeLock.release();
            }
            int end = uptimeMillisInt();
            if (start > sent && ((long) (start - sent)) > 500) {
                loge(String.format("NDC event {%s} processed too late: %dms", new Object[]{event, Integer.valueOf(start - sent)}));
            }
            if (end > start && ((long) (end - start)) > 500) {
                str = "NDC event {%s} took too long: %dms";
                objArr = new Object[]{event, Integer.valueOf(end - start)};
                loge(String.format(str, objArr));
            }
        } catch (Exception e) {
            loge("Error handling '" + event + "': " + e);
            if (this.mCallbacks.onCheckHoldWakeLock(msg.what) && this.mWakeLock != null) {
                this.mWakeLock.release();
            }
            int end2 = uptimeMillisInt();
            if (start > sent && ((long) (start - sent)) > 500) {
                loge(String.format("NDC event {%s} processed too late: %dms", new Object[]{event, Integer.valueOf(start - sent)}));
            }
            if (end2 > start && ((long) (end2 - start)) > 500) {
                str = "NDC event {%s} took too long: %dms";
                objArr = new Object[]{event, Integer.valueOf(end2 - start)};
            }
        } catch (Throwable th) {
            if (this.mCallbacks.onCheckHoldWakeLock(msg.what) && this.mWakeLock != null) {
                this.mWakeLock.release();
            }
            int end3 = uptimeMillisInt();
            if (start > sent && ((long) (start - sent)) > 500) {
                loge(String.format("NDC event {%s} processed too late: %dms", new Object[]{event, Integer.valueOf(start - sent)}));
            }
            if (end3 > start && ((long) (end3 - start)) > 500) {
                loge(String.format("NDC event {%s} took too long: %dms", new Object[]{event, Integer.valueOf(end3 - start)}));
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

    /* JADX WARNING: Code restructure failed: missing block: B:143:0x02d3, code lost:
        r0 = th;
     */
    /* JADX WARNING: Removed duplicated region for block: B:116:0x0276 A[SYNTHETIC, Splitter:B:116:0x0276] */
    /* JADX WARNING: Removed duplicated region for block: B:121:0x0280 A[Catch:{ all -> 0x02c1, IOException -> 0x02cf, all -> 0x02ca }] */
    /* JADX WARNING: Removed duplicated region for block: B:156:0x02fd A[SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:167:0x0338 A[SYNTHETIC, Splitter:B:167:0x0338] */
    private void listenToSocket() throws IOException {
        LocalSocket socket;
        Throwable th;
        LocalSocket socket2;
        int count;
        InputStream inputStream;
        char c;
        LocalSocketAddress address;
        PowerManager.WakeLock wakeLock;
        LocalSocket socket3 = null;
        log("listenToSocket enter");
        OutputStream outputStream = null;
        try {
            socket3 = new LocalSocket();
            try {
                LocalSocketAddress address2 = determineSocketAddress();
                socket3.connect(address2);
                InputStream inputStream2 = socket3.getInputStream();
                synchronized (this.mDaemonLock) {
                    try {
                        this.mOutputStream = socket3.getOutputStream();
                    } catch (IOException e) {
                        ex = e;
                        socket3 = socket2;
                    } catch (Throwable th2) {
                        th = th2;
                        socket = socket2;
                        synchronized (this.mDaemonLock) {
                        }
                        if (socket != null) {
                        }
                        throw th;
                    }
                }
                this.mCallbacks.onDaemonConnected();
                byte[] buffer = new byte[4096];
                int start = 0;
                while (true) {
                    count = inputStream2.read(buffer, start, 4096 - start);
                    if (count < 0) {
                        break;
                    }
                    FileDescriptor[] fdList = socket3.getAncillaryFileDescriptors();
                    int count2 = count + start;
                    int start2 = 0;
                    int start3 = 0;
                    while (true) {
                        int i = start3;
                        if (i >= count2) {
                            break;
                        }
                        if (buffer[i] == 0) {
                            String rawEvent = new String(buffer, start2, i - start2, StandardCharsets.UTF_8);
                            String r = "([0-9a-zA-Z]{2}:){4}[0-9a-zA-Z]{2}";
                            String rv = "[A-Fa-f0-9]{2,}:{1,}";
                            Pattern pattern = Pattern.compile(r);
                            String rawEventLog = rawEvent.replaceAll(encryption_ip, " ******** ");
                            Pattern pattern2 = pattern;
                            if (pattern2.matcher(rawEventLog).find()) {
                                rawEventLog = rawEventLog.replaceAll(r, " ******** ");
                                log("RCV <- {" + rawEventLog + "}");
                            } else if (!shouldPrintEvent()) {
                                rawEventLog = rawEventLog.replaceAll(rv, "****");
                                log("RCV <- {" + rawEventLog + "}");
                            }
                            String str = rawEventLog;
                            boolean releaseWl = false;
                            try {
                                NativeDaemonEvent event = NativeDaemonEvent.parseRawEvent(rawEvent, fdList);
                                socket2 = socket3;
                                address = address2;
                                try {
                                    String eventLog = event.toString().replaceAll(encryption_ip, " ******** ");
                                    if (shouldPrintEvent()) {
                                        try {
                                            log("RCV <- {" + event + "}");
                                        } catch (IllegalArgumentException e2) {
                                            e = e2;
                                            Pattern pattern3 = pattern2;
                                            inputStream = inputStream2;
                                        } catch (Throwable th3) {
                                            th = th3;
                                            Pattern pattern4 = pattern2;
                                            InputStream inputStream3 = inputStream2;
                                            if (releaseWl) {
                                            }
                                            throw th;
                                        }
                                    } else if (pattern2.matcher(eventLog).find()) {
                                        eventLog = eventLog.replaceAll(r, " ******** ");
                                        log("RCV <- {" + eventLog + "}");
                                    } else {
                                        eventLog = eventLog.replaceAll(rv, "****");
                                        log("RCV <- {" + eventLog + "}");
                                    }
                                    if (event.isClassUnsolicited()) {
                                        if (this.mCallbacks.onCheckHoldWakeLock(event.getCode())) {
                                            if (this.mWakeLock != null) {
                                                this.mWakeLock.acquire();
                                                releaseWl = true;
                                            }
                                        }
                                        String str2 = eventLog;
                                        Pattern pattern5 = pattern2;
                                        try {
                                            inputStream = inputStream2;
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
                                                    start2 = i + 1;
                                                    start3 = i + 1;
                                                    socket3 = socket2;
                                                    address2 = address;
                                                    inputStream2 = inputStream;
                                                } catch (Throwable th4) {
                                                    th = th4;
                                                    if (releaseWl) {
                                                    }
                                                    throw th;
                                                }
                                            }
                                        } catch (IllegalArgumentException e4) {
                                            e = e4;
                                            inputStream = inputStream2;
                                            log("Problem parsing message " + e);
                                            if (releaseWl) {
                                            }
                                            start2 = i + 1;
                                            start3 = i + 1;
                                            socket3 = socket2;
                                            address2 = address;
                                            inputStream2 = inputStream;
                                        } catch (Throwable th5) {
                                            th = th5;
                                            InputStream inputStream4 = inputStream2;
                                            if (releaseWl) {
                                            }
                                            throw th;
                                        }
                                    } else {
                                        String str3 = eventLog;
                                        Pattern pattern6 = pattern2;
                                        inputStream = inputStream2;
                                        this.mResponseQueue.add(event.getCmdNumber(), event);
                                    }
                                    if (releaseWl) {
                                        wakeLock = this.mWakeLock;
                                        wakeLock.release();
                                    }
                                } catch (IllegalArgumentException e5) {
                                    e = e5;
                                    Pattern pattern7 = pattern2;
                                    inputStream = inputStream2;
                                    log("Problem parsing message " + e);
                                    if (releaseWl) {
                                    }
                                    start2 = i + 1;
                                    start3 = i + 1;
                                    socket3 = socket2;
                                    address2 = address;
                                    inputStream2 = inputStream;
                                } catch (Throwable th6) {
                                    th = th6;
                                    Pattern pattern8 = pattern2;
                                    InputStream inputStream5 = inputStream2;
                                    if (releaseWl) {
                                    }
                                    throw th;
                                }
                            } catch (IllegalArgumentException e6) {
                                e = e6;
                                socket2 = socket3;
                                Pattern pattern9 = pattern2;
                                address = address2;
                                inputStream = inputStream2;
                                log("Problem parsing message " + e);
                                if (releaseWl) {
                                    wakeLock = this.mWakeLock;
                                    wakeLock.release();
                                }
                                start2 = i + 1;
                                start3 = i + 1;
                                socket3 = socket2;
                                address2 = address;
                                inputStream2 = inputStream;
                            } catch (Throwable th7) {
                                th = th7;
                                LocalSocket localSocket = socket3;
                                Pattern pattern10 = pattern2;
                                LocalSocketAddress localSocketAddress = address2;
                                InputStream inputStream6 = inputStream2;
                                if (releaseWl) {
                                    this.mWakeLock.release();
                                }
                                throw th;
                            }
                            start2 = i + 1;
                        } else {
                            socket2 = socket3;
                            address = address2;
                            inputStream = inputStream2;
                        }
                        start3 = i + 1;
                        socket3 = socket2;
                        address2 = address;
                        inputStream2 = inputStream;
                    }
                    LocalSocket socket4 = socket3;
                    LocalSocketAddress address3 = address2;
                    inputStream = inputStream2;
                    if (start2 == 0) {
                        log("RCV incomplete");
                    }
                    if (start2 != count2) {
                        c = 4096;
                        int remaining = 4096 - start2;
                        System.arraycopy(buffer, start2, buffer, 0, remaining);
                        start = remaining;
                    } else {
                        c = 4096;
                        start = 0;
                    }
                    char c2 = c;
                    socket3 = socket4;
                    address2 = address3;
                    inputStream2 = inputStream;
                    outputStream = null;
                }
                loge("got " + count + " reading with start = " + start);
                synchronized (this.mDaemonLock) {
                    if (this.mOutputStream != null) {
                        try {
                            loge("closing stream for " + this.mSocket);
                            this.mOutputStream.close();
                        } catch (IOException e7) {
                            loge("Failed closing output stream: " + e7);
                        }
                        this.mOutputStream = outputStream;
                    }
                }
                try {
                    socket3.close();
                } catch (IOException ex) {
                    IOException iOException = ex;
                    loge("Failed closing socket: " + ex);
                }
            } catch (IOException e8) {
                ex = e8;
                LocalSocket localSocket2 = socket3;
                try {
                    loge("Communications error: " + ex);
                    throw ex;
                } catch (Throwable th8) {
                    socket = socket3;
                    th = th8;
                    synchronized (this.mDaemonLock) {
                        if (this.mOutputStream != null) {
                            try {
                                loge("closing stream for " + this.mSocket);
                                this.mOutputStream.close();
                            } catch (IOException e9) {
                                loge("Failed closing output stream: " + e9);
                            }
                            this.mOutputStream = null;
                        }
                    }
                    if (socket != null) {
                        try {
                            socket.close();
                        } catch (IOException ex2) {
                            IOException iOException2 = ex2;
                            loge("Failed closing socket: " + ex2);
                        }
                    }
                    throw th;
                }
            } catch (Throwable th9) {
                LocalSocket localSocket3 = socket3;
                th = th9;
                socket = localSocket3;
                synchronized (this.mDaemonLock) {
                }
                if (socket != null) {
                }
                throw th;
            }
        } catch (IOException e10) {
            ex = e10;
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
            int length = args.length;
            int i = 0;
            while (i < length) {
                Object arg = args[i];
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
                    i++;
                } else {
                    Slog.e("NDC makeCommand", "Unexpected argument: " + arg);
                    throw new IllegalArgumentException("Unexpected argument: " + arg);
                }
            }
            rawBuilder.append(0);
        }
    }

    public void waitForCallbacks() {
        if (Thread.currentThread() != this.mLooper.getThread()) {
            final CountDownLatch latch = new CountDownLatch(1);
            this.mCallbackHandler.post(new Runnable() {
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

    /* JADX WARNING: Code restructure failed: missing block: B:19:0x00d4, code lost:
        r16 = r5;
        r17 = r6;
        r14 = r1.mResponseQueue.remove(r7, r26, r11);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x00e0, code lost:
        if (r14 != null) goto L_0x0128;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x00e2, code lost:
        loge("timed-out waiting for response to " + r11);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x00f9, code lost:
        if (r1.mhwNativeDaemonConnector == null) goto L_0x0101;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x00fb, code lost:
        r1.mhwNativeDaemonConnector.reportChrForAddRouteFail(r11, null);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0101, code lost:
        loge("timed-out waiting for response mOutputStream = " + r1.mOutputStream + ", mSocket = " + r1.mSocket);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0127, code lost:
        throw new com.android.server.NativeDaemonTimeoutException(r11, r14);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:26:0x0128, code lost:
        r0 = "RMV <- {" + r14 + "}";
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x0142, code lost:
        if (r1.mhwNativeDaemonConnector == null) goto L_0x0149;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x0144, code lost:
        r1.mhwNativeDaemonConnector.reportChrForAddRouteFail(r11, r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x0149, code lost:
        r4.add(r14);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x0150, code lost:
        if (r14.isClassContinue() != false) goto L_0x01b2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x015c, code lost:
        if ((android.os.SystemClock.elapsedRealtime() - r2) <= 500) goto L_0x0183;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x015e, code lost:
        loge("NDC Command {" + r11 + "} took too long (" + (r18 - r2) + "ms)");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:35:0x0187, code lost:
        if (r14.isClassClientError() != false) goto L_0x01a7;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x018d, code lost:
        if (r14.isClassServerError() != false) goto L_0x019c;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x019b, code lost:
        return (com.android.server.NativeDaemonEvent[]) r4.toArray(new com.android.server.NativeDaemonEvent[r4.size()]);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x019c, code lost:
        loge("NDC server error throw NativeDaemonFailureException");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x01a6, code lost:
        throw new com.android.server.NativeDaemonConnector.NativeDaemonFailureException(r11, r14);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x01a7, code lost:
        loge("NDC client error throw NativeDaemonArgumentException");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x01b1, code lost:
        throw new com.android.server.NativeDaemonConnector.NativeDaemonArgumentException(r11, r14);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x01b2, code lost:
        r5 = r16;
        r6 = r17;
     */
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
                        StringBuilder sb = rawBuilder;
                        StringBuilder sb2 = logBuilder;
                        loge("NDC problem sending command throw NativeDaemonConnectorException");
                        throw new NativeDaemonConnectorException("problem sending command", (Throwable) e);
                    } catch (Throwable th) {
                        th = th;
                        throw th;
                    }
                } else {
                    StringBuilder sb3 = logBuilder;
                    loge("NDC missing output stream throw NativeDaemonConnectorException");
                    throw new NativeDaemonConnectorException("missing output stream");
                }
            } catch (Throwable th2) {
                th = th2;
                StringBuilder sb4 = rawBuilder;
                StringBuilder sb5 = logBuilder;
                throw th;
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
        int i = 0;
        while (i < events.length - 1) {
            NativeDaemonEvent event = events[i];
            int code = event.getCode();
            if (code == expectedCode) {
                list.add(event.getMessage());
                i++;
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

    /* JADX WARNING: Removed duplicated region for block: B:17:0x003a A[RETURN] */
    /* JADX WARNING: Removed duplicated region for block: B:18:0x003b A[RETURN] */
    private boolean shouldPrintEvent() {
        char c;
        String str = this.TAG;
        int hashCode = str.hashCode();
        if (hashCode == -1339953786) {
            if (str.equals("SdCryptdConnector")) {
                c = 0;
                switch (c) {
                    case 0:
                    case 1:
                    case 2:
                        break;
                }
            }
        } else if (hashCode == 136483132) {
            if (str.equals("VoldConnector")) {
                c = 1;
                switch (c) {
                    case 0:
                    case 1:
                    case 2:
                        break;
                }
            }
        } else if (hashCode == 1813268503 && str.equals("CryptdConnector")) {
            c = 2;
            switch (c) {
                case 0:
                case 1:
                case 2:
                    return true;
                default:
                    return false;
            }
        }
        c = 65535;
        switch (c) {
            case 0:
            case 1:
            case 2:
                break;
        }
    }
}
