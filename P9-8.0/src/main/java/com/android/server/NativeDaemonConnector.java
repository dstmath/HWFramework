package com.android.server;

import android.net.LocalSocket;
import android.net.LocalSocketAddress;
import android.net.LocalSocketAddress.Namespace;
import android.os.Build;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager.WakeLock;
import android.os.SystemClock;
import android.util.LocalLog;
import android.util.Slog;
import com.android.internal.util.Preconditions;
import com.android.server.Watchdog.Monitor;
import com.android.server.os.HwBootFail;
import com.google.android.collect.Lists;
import java.io.FileDescriptor;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

final class NativeDaemonConnector implements Runnable, Callback, Monitor {
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
    private final WakeLock mWakeLock;
    private volatile Object mWarnIfHeld;
    private HwNativeDaemonConnector mhwNativeDaemonConnector;

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
        private final LinkedList<PendingCmd> mPendingCmds = new LinkedList();

        private static class PendingCmd {
            public int availableResponseCount;
            public final int cmdNum;
            public final String logCmd;
            public BlockingQueue<NativeDaemonEvent> responses = new ArrayBlockingQueue(20);

            public PendingCmd(int cmdNum, String logCmd) {
                this.cmdNum = cmdNum;
                this.logCmd = logCmd;
            }
        }

        ResponseQueue(int maxCount) {
            this.mMaxCount = maxCount;
        }

        /* JADX WARNING: Missing block: B:31:?, code:
            r1.responses.put(r11);
     */
        /* JADX WARNING: Missing block: B:33:0x00de, code:
            android.util.Slog.e("NDC put", "InterruptedException happen");
     */
        /* JADX WARNING: Missing block: B:39:?, code:
            return;
     */
        /* JADX WARNING: Missing block: B:40:?, code:
            return;
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void add(int cmdNum, NativeDaemonEvent response) {
            Throwable th;
            synchronized (this.mPendingCmds) {
                try {
                    PendingCmd pendingCmd;
                    PendingCmd found;
                    for (PendingCmd pendingCmd2 : this.mPendingCmds) {
                        if (pendingCmd2.cmdNum == cmdNum) {
                            found = pendingCmd2;
                            break;
                        }
                    }
                    found = null;
                    try {
                        PendingCmd found2;
                        if (this.mPendingCmds.size() == 11) {
                            Slog.d("NativeDaemonConnector.ResponseQueue", "mPendingCmds.size = " + this.mPendingCmds.size());
                        }
                        if (found == null) {
                            while (this.mPendingCmds.size() >= this.mMaxCount) {
                                Slog.e("NativeDaemonConnector.ResponseQueue", "more buffered than allowed: " + this.mPendingCmds.size() + " >= " + this.mMaxCount);
                                pendingCmd2 = (PendingCmd) this.mPendingCmds.remove();
                                Slog.e("NativeDaemonConnector.ResponseQueue", "Removing request: " + pendingCmd2.logCmd + " (" + pendingCmd2.cmdNum + ")");
                            }
                            found2 = new PendingCmd(cmdNum, null);
                            this.mPendingCmds.add(found2);
                        } else {
                            found2 = found;
                        }
                        found2.availableResponseCount++;
                        if (found2.availableResponseCount == 0) {
                            this.mPendingCmds.remove(found2);
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    throw th;
                }
            }
        }

        /* JADX WARNING: Missing block: B:18:0x0038, code:
            r6 = null;
     */
        /* JADX WARNING: Missing block: B:20:?, code:
            r6 = (com.android.server.NativeDaemonEvent) r2.responses.poll(r12, java.util.concurrent.TimeUnit.MILLISECONDS);
     */
        /* JADX WARNING: Missing block: B:28:0x0055, code:
            android.util.Slog.e("NDC poll", "InterruptedException happen");
     */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public NativeDaemonEvent remove(int cmdNum, long timeoutMs, String logCmd) {
            Throwable th;
            synchronized (this.mPendingCmds) {
                try {
                    PendingCmd found;
                    PendingCmd found2;
                    for (PendingCmd pendingCmd : this.mPendingCmds) {
                        if (pendingCmd.cmdNum == cmdNum) {
                            found = pendingCmd;
                            break;
                        }
                    }
                    found = null;
                    if (found == null) {
                        try {
                            found2 = new PendingCmd(cmdNum, logCmd);
                            this.mPendingCmds.add(found2);
                        } catch (Throwable th2) {
                            th = th2;
                            throw th;
                        }
                    }
                    found2 = found;
                    found2.availableResponseCount--;
                    if (found2.availableResponseCount == 0) {
                        this.mPendingCmds.remove(found2);
                    }
                } catch (Throwable th3) {
                    th = th3;
                    throw th;
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
                for (PendingCmd pendingCmd : this.mPendingCmds) {
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

    NativeDaemonConnector(INativeDaemonConnectorCallbacks callbacks, String socket, int responseQueueSize, String logTag, int maxLogSize, WakeLock wl) {
        this(callbacks, socket, responseQueueSize, logTag, maxLogSize, wl, FgThread.get().getLooper());
    }

    NativeDaemonConnector(INativeDaemonConnectorCallbacks callbacks, String socket, int responseQueueSize, String logTag, int maxLogSize, WakeLock wl, Looper looper) {
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
        if (logTag == null) {
            logTag = "NativeDaemonConnector";
        }
        this.TAG = logTag;
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
        while (true) {
            try {
                listenToSocket();
            } catch (Exception e) {
                loge("Error in NativeDaemonConnector: " + e);
                SystemClock.sleep(5000);
            }
        }
    }

    public boolean handleMessage(Message msg) {
        String event = msg.obj;
        String encryption_event = event.replaceAll(encryption_ip, " ******** ");
        log("RCV unsolicited event from native daemon, event = " + msg.what);
        int start = uptimeMillisInt();
        int sent = msg.arg1;
        int end;
        try {
            if (!this.mCallbacks.onEvent(msg.what, event, NativeDaemonEvent.unescapeArgs(event))) {
                log(String.format("Unhandled event '%s'", new Object[]{event}));
            }
            if (this.mCallbacks.onCheckHoldWakeLock(msg.what) && this.mWakeLock != null) {
                this.mWakeLock.release();
            }
            end = uptimeMillisInt();
            if (start > sent && ((long) (start - sent)) > 500) {
                loge(String.format("NDC event {%s} processed too late: %dms", new Object[]{event, Integer.valueOf(start - sent)}));
            }
            if (end > start && ((long) (end - start)) > 500) {
                loge(String.format("NDC event {%s} took too long: %dms", new Object[]{event, Integer.valueOf(end - start)}));
            }
        } catch (Exception e) {
            loge("Error handling '" + event + "': " + e);
            if (this.mCallbacks.onCheckHoldWakeLock(msg.what) && this.mWakeLock != null) {
                this.mWakeLock.release();
            }
            end = uptimeMillisInt();
            if (start > sent && ((long) (start - sent)) > 500) {
                loge(String.format("NDC event {%s} processed too late: %dms", new Object[]{event, Integer.valueOf(start - sent)}));
            }
            if (end > start && ((long) (end - start)) > 500) {
                loge(String.format("NDC event {%s} took too long: %dms", new Object[]{event, Integer.valueOf(end - start)}));
            }
        } catch (Throwable th) {
            if (this.mCallbacks.onCheckHoldWakeLock(msg.what) && this.mWakeLock != null) {
                this.mWakeLock.release();
            }
            end = uptimeMillisInt();
            if (start > sent && ((long) (start - sent)) > 500) {
                loge(String.format("NDC event {%s} processed too late: %dms", new Object[]{event, Integer.valueOf(start - sent)}));
            }
            if (end > start && ((long) (end - start)) > 500) {
                loge(String.format("NDC event {%s} took too long: %dms", new Object[]{event, Integer.valueOf(end - start)}));
            }
        }
        return true;
    }

    private LocalSocketAddress determineSocketAddress() {
        if (this.mSocket.startsWith("__test__") && Build.IS_DEBUGGABLE) {
            return new LocalSocketAddress(this.mSocket);
        }
        return new LocalSocketAddress(this.mSocket, Namespace.RESERVED);
    }

    private void listenToSocket() throws IOException {
        IOException ex;
        Throwable th;
        LocalSocket socket = null;
        log("listenToSocket enter");
        try {
            LocalSocket socket2 = new LocalSocket();
            try {
                socket2.connect(determineSocketAddress());
                InputStream inputStream = socket2.getInputStream();
                synchronized (this.mDaemonLock) {
                    this.mOutputStream = socket2.getOutputStream();
                }
                this.mCallbacks.onDaemonConnected();
                byte[] buffer = new byte[4096];
                int start = 0;
                while (true) {
                    int count = inputStream.read(buffer, start, 4096 - start);
                    if (count < 0) {
                        loge("got " + count + " reading with start = " + start);
                        synchronized (this.mDaemonLock) {
                            if (this.mOutputStream != null) {
                                try {
                                    loge("closing stream for " + this.mSocket);
                                    this.mOutputStream.close();
                                } catch (IOException e) {
                                    loge("Failed closing output stream: " + e);
                                }
                                this.mOutputStream = null;
                            }
                        }
                        if (socket2 != null) {
                            try {
                                socket2.close();
                                return;
                            } catch (IOException ex2) {
                                loge("Failed closing socket: " + ex2);
                                return;
                            }
                        }
                        return;
                    }
                    FileDescriptor[] fdList = socket2.getAncillaryFileDescriptors();
                    count += start;
                    start = 0;
                    for (int i = 0; i < count; i++) {
                        if (buffer[i] == (byte) 0) {
                            String str = new String(buffer, start, i - start, StandardCharsets.UTF_8);
                            String r = "([0-9a-zA-Z]{2}:){4}[0-9a-zA-Z]{2}";
                            String rv = "[A-Fa-f0-9]{2,}:{1,}";
                            Pattern pattern = Pattern.compile(r);
                            String rawEventLog = str.replaceAll(encryption_ip, " ******** ");
                            if (pattern.matcher(rawEventLog).find()) {
                                log("RCV <- {" + rawEventLog.replaceAll(r, " ******** ") + "}");
                            } else if (!shouldPrintEvent()) {
                                log("RCV <- {" + rawEventLog.replaceAll(rv, "****") + "}");
                            }
                            boolean releaseWl = false;
                            try {
                                NativeDaemonEvent event = NativeDaemonEvent.parseRawEvent(str, fdList);
                                String eventLog = event.toString().replaceAll(encryption_ip, " ******** ");
                                if (shouldPrintEvent()) {
                                    log("RCV <- {" + event + "}");
                                } else if (pattern.matcher(eventLog).find()) {
                                    log("RCV <- {" + eventLog.replaceAll(r, " ******** ") + "}");
                                } else {
                                    log("RCV <- {" + eventLog.replaceAll(rv, "****") + "}");
                                }
                                if (event.isClassUnsolicited()) {
                                    if (this.mCallbacks.onCheckHoldWakeLock(event.getCode()) && this.mWakeLock != null) {
                                        this.mWakeLock.acquire();
                                        releaseWl = true;
                                    }
                                    if (this.mCallbackHandler.sendMessage(this.mCallbackHandler.obtainMessage(event.getCode(), uptimeMillisInt(), 0, event.getRawEvent()))) {
                                        releaseWl = false;
                                    }
                                } else {
                                    this.mResponseQueue.add(event.getCmdNumber(), event);
                                }
                                if (releaseWl) {
                                    this.mWakeLock.release();
                                }
                            } catch (IllegalArgumentException e2) {
                                log("Problem parsing message " + e2);
                                if (null != null) {
                                    this.mWakeLock.release();
                                }
                            } catch (Throwable th2) {
                                if (null != null) {
                                    this.mWakeLock.release();
                                }
                            }
                            start = i + 1;
                        }
                    }
                    if (start == 0) {
                        log("RCV incomplete");
                    }
                    if (start != count) {
                        int remaining = 4096 - start;
                        System.arraycopy(buffer, start, buffer, 0, remaining);
                        start = remaining;
                    } else {
                        start = 0;
                    }
                }
            } catch (IOException e3) {
                ex2 = e3;
                socket = socket2;
            } catch (Throwable th3) {
                th = th3;
                socket = socket2;
            }
        } catch (IOException e4) {
            ex2 = e4;
            try {
                loge("Communications error: " + ex2);
                throw ex2;
            } catch (Throwable th4) {
                th = th4;
                synchronized (this.mDaemonLock) {
                    if (this.mOutputStream != null) {
                        try {
                            loge("closing stream for " + this.mSocket);
                            this.mOutputStream.close();
                        } catch (IOException e5) {
                            loge("Failed closing output stream: " + e5);
                        }
                        this.mOutputStream = null;
                    }
                }
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (IOException ex22) {
                        loge("Failed closing socket: " + ex22);
                    }
                }
                throw th;
            }
        }
    }

    static void makeCommand(StringBuilder rawBuilder, StringBuilder logBuilder, int sequenceNumber, String cmd, Object... args) {
        if (cmd.indexOf(0) >= 0) {
            Slog.e("NDC makeCommand", "Unexpected command, cmd = " + cmd);
            throw new IllegalArgumentException("Unexpected command: " + cmd);
        } else if (cmd.indexOf(32) >= 0) {
            Slog.e("NDC makeCommand", "Error, arguments must be separate from command");
            throw new IllegalArgumentException("Arguments must be separate from command");
        } else {
            rawBuilder.append(sequenceNumber).append(' ').append(cmd);
            logBuilder.append(sequenceNumber).append(' ').append(cmd);
            for (Object arg : args) {
                String argString = String.valueOf(arg);
                if (argString.indexOf(0) >= 0) {
                    Slog.e("NDC makeCommand", "Unexpected argument: " + arg);
                    throw new IllegalArgumentException("Unexpected argument: " + arg);
                }
                rawBuilder.append(' ');
                logBuilder.append(' ');
                appendEscaped(rawBuilder, argString);
                if (arg instanceof SensitiveArg) {
                    logBuilder.append("[scrubbed]");
                } else {
                    appendEscaped(logBuilder, argString);
                }
            }
            rawBuilder.append(0);
        }
    }

    public void waitForCallbacks() {
        if (Thread.currentThread() == this.mLooper.getThread()) {
            throw new IllegalStateException("Must not call this method on callback thread");
        }
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
        NativeDaemonEvent event;
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
        String r_ipv6 = "([A-Fa-f0-9]{2,}:{1,}){2,}";
        log("SND -> {" + logCmd.replaceAll(encryption_ip, " ******** ").replaceAll("([A-Fa-f0-9]{2,}:{1,}){2,}", " **** ") + "}");
        if (shouldPrintEvent()) {
            log("SND -> {" + logCmd + "}");
        }
        synchronized (this.mDaemonLock) {
            if (this.mOutputStream == null) {
                loge("NDC missing output stream throw NativeDaemonConnectorException");
                throw new NativeDaemonConnectorException("missing output stream");
            }
            try {
                this.mOutputStream.write(rawCmd.getBytes(StandardCharsets.UTF_8));
            } catch (Throwable e) {
                loge("NDC problem sending command throw NativeDaemonConnectorException");
                throw new NativeDaemonConnectorException("problem sending command", e);
            }
        }
        do {
            event = this.mResponseQueue.remove(sequenceNumber, timeoutMs, logCmd);
            if (event == null) {
                loge("timed-out waiting for response to " + logCmd);
                if (this.mhwNativeDaemonConnector != null) {
                    this.mhwNativeDaemonConnector.reportChrForAddRouteFail(logCmd, null);
                }
                loge("timed-out waiting for response mOutputStream = " + this.mOutputStream + ", mSocket = " + this.mSocket);
                throw new NativeDaemonTimeoutException(logCmd, event);
            }
            String rmvLogStr = "RMV <- {" + event + "}";
            if (this.mhwNativeDaemonConnector != null) {
                this.mhwNativeDaemonConnector.reportChrForAddRouteFail(logCmd, rmvLogStr);
            }
            events.add(event);
        } while (event.isClassContinue());
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
        this.mLocalLog.log(logstring);
    }

    private void loge(String logstring) {
        Slog.e(this.TAG, logstring);
        this.mLocalLog.log(logstring);
    }

    private boolean shouldPrintEvent() {
        String str = this.TAG;
        if (str.equals("SdCryptdConnector") || str.equals("VoldConnector") || str.equals("CryptdConnector")) {
            return true;
        }
        return false;
    }
}
