package android.os;

import android.common.HwFrameworkFactory;
import android.net.wifi.WifiEnterpriseConfig;
import android.util.HwEtrace;
import android.util.Log;
import android.util.Printer;
import android.util.Slog;
import android.util.proto.ProtoOutputStream;
import android.zrhung.IAppEyeUiProbe;
import java.lang.annotation.RCUnownedRef;

public final class Looper {
    private static final String TAG = "Looper";
    private static IAppEyeUiProbe mZrHungAppEyeUiProbe;
    private static Looper sMainLooper;
    static final ThreadLocal<Looper> sThreadLocal = new ThreadLocal<>();
    private Printer mLogging;
    final MessageQueue mQueue;
    private long mSlowDeliveryThresholdMs;
    private long mSlowDispatchThresholdMs;
    @RCUnownedRef
    final Thread mThread = Thread.currentThread();
    private long mTraceTag;

    public static void prepare() {
        prepare(true);
    }

    private static void prepare(boolean quitAllowed) {
        if (sThreadLocal.get() == null) {
            sThreadLocal.set(new Looper(quitAllowed));
            return;
        }
        throw new RuntimeException("Only one Looper may be created per thread");
    }

    public static void prepareMainLooper() {
        prepare(false);
        synchronized (Looper.class) {
            if (sMainLooper == null) {
                sMainLooper = myLooper();
            } else {
                throw new IllegalStateException("The main Looper has already been prepared.");
            }
        }
    }

    public static Looper getMainLooper() {
        Looper looper;
        synchronized (Looper.class) {
            looper = sMainLooper;
        }
        return looper;
    }

    /* JADX WARNING: Removed duplicated region for block: B:123:0x01fe A[SYNTHETIC] */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x00a3  */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x00a6  */
    /* JADX WARNING: Removed duplicated region for block: B:35:0x00cb  */
    /* JADX WARNING: Removed duplicated region for block: B:36:0x00d0  */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x00da A[SYNTHETIC, Splitter:B:41:0x00da] */
    /* JADX WARNING: Removed duplicated region for block: B:51:0x0104 A[SYNTHETIC, Splitter:B:51:0x0104] */
    /* JADX WARNING: Removed duplicated region for block: B:56:0x010d A[SYNTHETIC, Splitter:B:56:0x010d] */
    /* JADX WARNING: Removed duplicated region for block: B:61:0x0118 A[Catch:{ all -> 0x00e8 }] */
    /* JADX WARNING: Removed duplicated region for block: B:63:0x011f  */
    /* JADX WARNING: Removed duplicated region for block: B:66:0x0127  */
    /* JADX WARNING: Removed duplicated region for block: B:68:0x012c  */
    /* JADX WARNING: Removed duplicated region for block: B:76:0x0170  */
    /* JADX WARNING: Removed duplicated region for block: B:79:0x017d  */
    /* JADX WARNING: Removed duplicated region for block: B:81:0x018a  */
    /* JADX WARNING: Removed duplicated region for block: B:84:0x01b2  */
    /* JADX WARNING: Removed duplicated region for block: B:90:0x0218  */
    public static void loop() {
        int thresholdOverride;
        boolean z;
        boolean logSlowDelivery;
        boolean logSlowDispatch;
        long traceTag;
        Message msg;
        Printer logging;
        boolean slowDeliveryDetected;
        boolean slowDeliveryDetected2;
        Looper me = myLooper();
        if (me != null) {
            MessageQueue queue = me.mQueue;
            Binder.clearCallingIdentity();
            long ident = Binder.clearCallingIdentity();
            int thresholdOverride2 = SystemProperties.getInt("log.looper." + Process.myUid() + "." + Thread.currentThread().getName() + ".slow", 0);
            if (!BlockMonitor.isNeedMonitor()) {
                boolean slowDeliveryDetected3 = false;
                while (true) {
                    Message msg2 = queue.next();
                    if (msg2 != null) {
                        Printer logging2 = me.mLogging;
                        if (logging2 != null) {
                            logging2.println(">>>>> Dispatching to " + msg2.target + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + msg2.callback + ": " + msg2.what);
                        }
                        long traceTag2 = me.mTraceTag;
                        long slowDispatchThresholdMs = me.mSlowDispatchThresholdMs;
                        long slowDeliveryThresholdMs = me.mSlowDeliveryThresholdMs;
                        if (thresholdOverride2 > 0) {
                            slowDispatchThresholdMs = (long) thresholdOverride2;
                            slowDeliveryThresholdMs = (long) thresholdOverride2;
                        }
                        long slowDispatchThresholdMs2 = slowDispatchThresholdMs;
                        boolean z2 = true;
                        if (slowDeliveryThresholdMs > 0) {
                            thresholdOverride = thresholdOverride2;
                            if (msg2.when > 0) {
                                z = true;
                                logSlowDelivery = z;
                                logSlowDispatch = slowDispatchThresholdMs2 <= 0;
                                if (!logSlowDelivery && !logSlowDispatch) {
                                    z2 = false;
                                }
                                boolean needStartTime = z2;
                                boolean needEndTime = logSlowDispatch;
                                if (traceTag2 != 0 && Trace.isTagEnabled(traceTag2)) {
                                    Trace.traceBegin(traceTag2, msg2.target.getTraceName(msg2));
                                }
                                long dispatchStart = !needStartTime ? SystemClock.uptimeMillis() : 0;
                                if (BlockMonitor.isInMainThread()) {
                                    try {
                                        if (mZrHungAppEyeUiProbe != null) {
                                            mZrHungAppEyeUiProbe.beginDispatching(msg2, msg2.target, msg2.callback);
                                        }
                                    } catch (Throwable th) {
                                        th = th;
                                        boolean z3 = logSlowDelivery;
                                        boolean z4 = slowDeliveryDetected3;
                                        long j = slowDeliveryThresholdMs;
                                        traceTag = traceTag2;
                                        Message message = msg2;
                                        Printer printer = logging2;
                                        if (traceTag != 0) {
                                        }
                                        throw th;
                                    }
                                }
                                writeEtraceIdFromMsgToTls(msg2);
                                msg2.target.dispatchMessage(msg2);
                                if (msg2.etraceID != 0) {
                                    HwEtrace.clearTlsId();
                                }
                                if (BlockMonitor.isInMainThread()) {
                                    if (mZrHungAppEyeUiProbe != null) {
                                        mZrHungAppEyeUiProbe.endDispatching();
                                    }
                                }
                                long dispatchEnd = !needEndTime ? SystemClock.uptimeMillis() : 0;
                                if (traceTag2 != 0) {
                                    Trace.traceEnd(traceTag2);
                                }
                                if (logSlowDelivery) {
                                    slowDeliveryDetected2 = slowDeliveryDetected3;
                                    long j2 = slowDeliveryThresholdMs;
                                    long j3 = traceTag2;
                                    msg = msg2;
                                    logging = logging2;
                                } else if (!slowDeliveryDetected3) {
                                    slowDeliveryDetected2 = slowDeliveryDetected3;
                                    long j4 = slowDeliveryThresholdMs;
                                    long j5 = traceTag2;
                                    boolean z5 = logSlowDelivery;
                                    msg = msg2;
                                    logging = logging2;
                                    if (showSlowLog(slowDeliveryThresholdMs, msg2.when, dispatchStart, "delivery", msg)) {
                                        slowDeliveryDetected = true;
                                        if (logSlowDispatch) {
                                        }
                                        if (logging != null) {
                                        }
                                        if (ident == Binder.clearCallingIdentity()) {
                                        }
                                        msg.recycleUnchecked();
                                        slowDeliveryDetected3 = slowDeliveryDetected;
                                        thresholdOverride2 = thresholdOverride;
                                    }
                                } else if (dispatchStart - msg2.when <= 10) {
                                    Slog.w(TAG, "Drained");
                                    slowDeliveryDetected = false;
                                    boolean z6 = logSlowDelivery;
                                    long j6 = slowDeliveryThresholdMs;
                                    long j7 = traceTag2;
                                    msg = msg2;
                                    logging = logging2;
                                    if (logSlowDispatch) {
                                        showSlowLog(slowDispatchThresholdMs2, dispatchStart, dispatchEnd, "dispatch", msg);
                                    }
                                    if (logging != null) {
                                        logging.println("<<<<< Finished to " + msg.target + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + msg.callback);
                                    }
                                    if (ident == Binder.clearCallingIdentity()) {
                                        Log.wtf(TAG, "Thread identity changed from 0x" + Long.toHexString(ident) + " to 0x" + Long.toHexString(newIdent) + " while dispatching to " + msg.target.getClass().getName() + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + msg.callback + " what=" + msg.what);
                                    }
                                    msg.recycleUnchecked();
                                    slowDeliveryDetected3 = slowDeliveryDetected;
                                    thresholdOverride2 = thresholdOverride;
                                } else {
                                    boolean z7 = logSlowDelivery;
                                    slowDeliveryDetected2 = slowDeliveryDetected3;
                                    long j8 = slowDeliveryThresholdMs;
                                    long j9 = traceTag2;
                                    msg = msg2;
                                    logging = logging2;
                                }
                                slowDeliveryDetected = slowDeliveryDetected2;
                                if (logSlowDispatch) {
                                }
                                if (logging != null) {
                                }
                                if (ident == Binder.clearCallingIdentity()) {
                                }
                                msg.recycleUnchecked();
                                slowDeliveryDetected3 = slowDeliveryDetected;
                                thresholdOverride2 = thresholdOverride;
                            }
                        } else {
                            thresholdOverride = thresholdOverride2;
                        }
                        z = false;
                        logSlowDelivery = z;
                        logSlowDispatch = slowDispatchThresholdMs2 <= 0;
                        z2 = false;
                        boolean needStartTime2 = z2;
                        boolean needEndTime2 = logSlowDispatch;
                        Trace.traceBegin(traceTag2, msg2.target.getTraceName(msg2));
                        long dispatchStart2 = !needStartTime2 ? SystemClock.uptimeMillis() : 0;
                        if (BlockMonitor.isInMainThread()) {
                        }
                        try {
                            writeEtraceIdFromMsgToTls(msg2);
                            msg2.target.dispatchMessage(msg2);
                            if (msg2.etraceID != 0) {
                            }
                            if (BlockMonitor.isInMainThread()) {
                            }
                            if (!needEndTime2) {
                            }
                            if (traceTag2 != 0) {
                            }
                            if (logSlowDelivery) {
                            }
                            slowDeliveryDetected = slowDeliveryDetected2;
                            if (logSlowDispatch) {
                            }
                            if (logging != null) {
                            }
                            if (ident == Binder.clearCallingIdentity()) {
                            }
                            msg.recycleUnchecked();
                            slowDeliveryDetected3 = slowDeliveryDetected;
                            thresholdOverride2 = thresholdOverride;
                        } catch (Throwable th2) {
                            th = th2;
                            boolean z8 = logSlowDelivery;
                            boolean z9 = slowDeliveryDetected3;
                            long j10 = slowDeliveryThresholdMs;
                            traceTag = traceTag2;
                            Message message2 = msg2;
                            Printer printer2 = logging2;
                            if (traceTag != 0) {
                                Trace.traceEnd(traceTag);
                            }
                            throw th;
                        }
                    } else {
                        return;
                    }
                }
            } else {
                while (true) {
                    Message msg3 = queue.next();
                    if (msg3 != null) {
                        Printer logging3 = me.mLogging;
                        if (logging3 != null) {
                            logging3.println(">>>>> Dispatching to " + msg3.target + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + msg3.callback + ": " + msg3.what);
                        }
                        long dispatchTime = SystemClock.uptimeMillis();
                        if (BlockMonitor.isInMainThread() && mZrHungAppEyeUiProbe != null) {
                            mZrHungAppEyeUiProbe.beginDispatching(msg3, msg3.target, msg3.callback);
                        }
                        writeEtraceIdFromMsgToTls(msg3);
                        msg3.target.dispatchMessage(msg3);
                        if (msg3.etraceID != 0) {
                            HwEtrace.clearTlsId();
                        }
                        if (BlockMonitor.isInMainThread() && mZrHungAppEyeUiProbe != null) {
                            mZrHungAppEyeUiProbe.endDispatching();
                        }
                        BlockMonitor.checkMessageDelayTime(dispatchTime, msg3, queue);
                        if (logging3 != null) {
                            logging3.println("<<<<< Finished to " + msg3.target + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + msg3.callback);
                        }
                        if (ident != Binder.clearCallingIdentity()) {
                            Log.wtf(TAG, "Thread identity changed from 0x" + Long.toHexString(ident) + " to 0x" + Long.toHexString(newIdent) + " while dispatching to " + msg3.target.getClass().getName() + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + msg3.callback + " what=" + msg3.what);
                        }
                        msg3.recycleUnchecked();
                    } else {
                        return;
                    }
                }
            }
        } else {
            throw new RuntimeException("No Looper; Looper.prepare() wasn't called on this thread.");
        }
    }

    private static void writeEtraceIdFromMsgToTls(Message msg) {
        long etraceID = msg.etraceID;
        if (etraceID != 0) {
            HwEtrace.setTlsIdAndNewSpanForCallee(etraceID);
        } else {
            HwEtrace.clearTlsId();
        }
    }

    private static boolean showSlowLog(long threshold, long measureStart, long measureEnd, String what, Message msg) {
        long actualTime = measureEnd - measureStart;
        if (actualTime < threshold) {
            return false;
        }
        Slog.w(TAG, "Slow " + what + " took " + actualTime + "ms " + Thread.currentThread().getName() + " h=" + msg.target.getClass().getName() + " c=" + msg.callback + " m=" + msg.what);
        return true;
    }

    public static Looper myLooper() {
        return sThreadLocal.get();
    }

    public static MessageQueue myQueue() {
        return myLooper().mQueue;
    }

    private Looper(boolean quitAllowed) {
        this.mQueue = new MessageQueue(quitAllowed);
        mZrHungAppEyeUiProbe = HwFrameworkFactory.getAppEyeUiProbe();
    }

    public boolean isCurrentThread() {
        return Thread.currentThread() == this.mThread;
    }

    public void setMessageLogging(Printer printer) {
        this.mLogging = printer;
    }

    public void setTraceTag(long traceTag) {
        this.mTraceTag = traceTag;
    }

    public void setSlowLogThresholdMs(long slowDispatchThresholdMs, long slowDeliveryThresholdMs) {
        this.mSlowDispatchThresholdMs = slowDispatchThresholdMs;
        this.mSlowDeliveryThresholdMs = slowDeliveryThresholdMs;
    }

    public void quit() {
        this.mQueue.quit(false);
    }

    public void quitSafely() {
        this.mQueue.quit(true);
    }

    public Thread getThread() {
        return this.mThread;
    }

    public MessageQueue getQueue() {
        return this.mQueue;
    }

    public void dump(Printer pw, String prefix) {
        pw.println(prefix + toString());
        MessageQueue messageQueue = this.mQueue;
        messageQueue.dump(pw, prefix + "  ", null);
    }

    public void dump(Printer pw, String prefix, Handler handler) {
        pw.println(prefix + toString());
        MessageQueue messageQueue = this.mQueue;
        messageQueue.dump(pw, prefix + "  ", handler);
    }

    public void writeToProto(ProtoOutputStream proto, long fieldId) {
        long looperToken = proto.start(fieldId);
        proto.write(1138166333441L, this.mThread.getName());
        proto.write(1112396529666L, this.mThread.getId());
        this.mQueue.writeToProto(proto, 1146756268035L);
        proto.end(looperToken);
    }

    public String toString() {
        return "Looper (" + this.mThread.getName() + ", tid " + this.mThread.getId() + ") {" + Integer.toHexString(System.identityHashCode(this)) + "}";
    }
}
