package android.os;

import android.annotation.UnsupportedAppUsage;
import android.common.HwFrameworkFactory;
import android.net.wifi.WifiEnterpriseConfig;
import android.provider.Telephony;
import android.util.Log;
import android.util.Printer;
import android.util.Slog;
import android.util.proto.ProtoOutputStream;
import huawei.hiview.HiTraceHandler;

public final class Looper {
    private static final String TAG = "Looper";
    @UnsupportedAppUsage
    private static Looper sMainLooper;
    private static Observer sObserver;
    @UnsupportedAppUsage
    static final ThreadLocal<Looper> sThreadLocal = new ThreadLocal<>();
    @UnsupportedAppUsage
    private Printer mLogging;
    @UnsupportedAppUsage
    final MessageQueue mQueue;
    private long mSlowDeliveryThresholdMs;
    private long mSlowDispatchThresholdMs;
    final Thread mThread = Thread.currentThread();
    private long mTraceTag;

    public interface Observer {
        void dispatchingThrewException(Object obj, Message message, Exception exc);

        Object messageDispatchStarting();

        void messageDispatched(Object obj, Message message);
    }

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

    public static void setObserver(Observer observer) {
        sObserver = observer;
    }

    /* JADX INFO: Multiple debug info for r8v1 long: [D('slowDeliveryThresholdMs' long), D('slowDispatchThresholdMs' long)] */
    /* JADX INFO: Multiple debug info for r1v15 long: [D('me' android.os.Looper), D('slowDispatchThresholdMs' long)] */
    /* JADX WARNING: Removed duplicated region for block: B:22:0x00ad  */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x00b0  */
    /* JADX WARNING: Removed duplicated region for block: B:33:0x00d1  */
    /* JADX WARNING: Removed duplicated region for block: B:34:0x00d6  */
    /* JADX WARNING: Removed duplicated region for block: B:37:0x00dd  */
    /* JADX WARNING: Removed duplicated region for block: B:42:0x00f8 A[SYNTHETIC, Splitter:B:42:0x00f8] */
    /* JADX WARNING: Removed duplicated region for block: B:49:0x011c A[Catch:{ Exception -> 0x010b, all -> 0x00fc }] */
    /* JADX WARNING: Removed duplicated region for block: B:51:0x0123  */
    /* JADX WARNING: Removed duplicated region for block: B:54:0x012e  */
    /* JADX WARNING: Removed duplicated region for block: B:57:0x0135  */
    /* JADX WARNING: Removed duplicated region for block: B:65:0x0187  */
    /* JADX WARNING: Removed duplicated region for block: B:68:0x0199  */
    /* JADX WARNING: Removed duplicated region for block: B:70:0x01a6  */
    /* JADX WARNING: Removed duplicated region for block: B:71:0x01c7  */
    /* JADX WARNING: Removed duplicated region for block: B:74:0x01d1  */
    /* JADX WARNING: Removed duplicated region for block: B:81:0x0243 A[SYNTHETIC, Splitter:B:81:0x0243] */
    /* JADX WARNING: Removed duplicated region for block: B:87:0x0252  */
    /* JADX WARNING: Removed duplicated region for block: B:93:0x0219 A[SYNTHETIC] */
    public static void loop() {
        long slowDispatchThresholdMs;
        MessageQueue queue;
        Looper me;
        long slowDispatchThresholdMs2;
        int thresholdOverride;
        boolean logSlowDelivery;
        boolean logSlowDispatch;
        Object token;
        long origWorkSource;
        long traceTag;
        Exception exception;
        Message msg;
        Object token2;
        Observer observer;
        Exception exception2;
        Message msg2;
        boolean slowDeliveryDetected;
        Printer logging;
        String str;
        Message msg3;
        long newIdent;
        boolean slowDeliveryDetected2;
        Looper me2 = myLooper();
        if (me2 != null) {
            MessageQueue queue2 = me2.mQueue;
            Binder.clearCallingIdentity();
            long ident = Binder.clearCallingIdentity();
            int thresholdOverride2 = SystemProperties.getInt("log.looper." + Process.myUid() + "." + Thread.currentThread().getName() + ".slow", 0);
            boolean slowDeliveryDetected3 = false;
            while (true) {
                Message msg4 = queue2.next();
                if (msg4 != null) {
                    Printer logging2 = me2.mLogging;
                    if (logging2 != null) {
                        logging2.println(">>>>> Dispatching to " + msg4.target + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + msg4.callback + ": " + msg4.what);
                    }
                    Observer observer2 = sObserver;
                    long traceTag2 = me2.mTraceTag;
                    long slowDispatchThresholdMs3 = me2.mSlowDispatchThresholdMs;
                    long slowDispatchThresholdMs4 = me2.mSlowDeliveryThresholdMs;
                    if (thresholdOverride2 > 0) {
                        me = me2;
                        queue = queue2;
                        slowDispatchThresholdMs = (long) thresholdOverride2;
                        slowDispatchThresholdMs2 = (long) thresholdOverride2;
                    } else {
                        me = me2;
                        queue = queue2;
                        slowDispatchThresholdMs2 = slowDispatchThresholdMs4;
                        slowDispatchThresholdMs = slowDispatchThresholdMs3;
                    }
                    boolean needStartTime = true;
                    if (slowDispatchThresholdMs2 > 0) {
                        thresholdOverride = thresholdOverride2;
                        if (msg4.when > 0) {
                            logSlowDelivery = true;
                            logSlowDispatch = slowDispatchThresholdMs <= 0;
                            if (!logSlowDelivery && !logSlowDispatch) {
                                needStartTime = false;
                            }
                            if (traceTag2 != 0 && Trace.isTagEnabled(traceTag2)) {
                                Trace.traceBegin(traceTag2, msg4.target.getTraceName(msg4));
                            }
                            long dispatchStart = !needStartTime ? SystemClock.uptimeMillis() : 0;
                            token = null;
                            if (observer2 != null) {
                                token = observer2.messageDispatchStarting();
                            }
                            origWorkSource = ThreadLocalWorkSource.setUid(msg4.workSourceUid);
                            HiTraceHandler hitraceHandler = HwFrameworkFactory.getHiTraceHandler();
                            hitraceHandler.srTraceInLooper(msg4);
                            msg4.target.dispatchMessage(msg4);
                            hitraceHandler.ssTraceInLooper(msg4);
                            if (observer2 != null) {
                                try {
                                    observer2.messageDispatched(token, msg4);
                                } catch (Exception e) {
                                    exception2 = e;
                                    traceTag = traceTag2;
                                    observer = observer2;
                                    token2 = token;
                                    msg = msg4;
                                    if (observer != null) {
                                        try {
                                            observer.dispatchingThrewException(token2, msg, exception2);
                                        } catch (Throwable th) {
                                            exception = th;
                                            ThreadLocalWorkSource.restore(origWorkSource);
                                            if (traceTag != 0) {
                                                Trace.traceEnd(traceTag);
                                            }
                                            throw exception;
                                        }
                                    }
                                    throw exception2;
                                } catch (Throwable th2) {
                                    exception = th2;
                                    traceTag = traceTag2;
                                    ThreadLocalWorkSource.restore(origWorkSource);
                                    if (traceTag != 0) {
                                    }
                                    throw exception;
                                }
                            }
                            long dispatchEnd = !logSlowDispatch ? SystemClock.uptimeMillis() : 0;
                            ThreadLocalWorkSource.restore(origWorkSource);
                            if (traceTag2 != 0) {
                                Trace.traceEnd(traceTag2);
                            }
                            if (logSlowDelivery) {
                                slowDeliveryDetected2 = slowDeliveryDetected3;
                                str = TAG;
                                logging = logging2;
                                msg2 = msg4;
                            } else if (!slowDeliveryDetected3) {
                                long traceTag3 = msg4.when;
                                str = TAG;
                                slowDeliveryDetected2 = slowDeliveryDetected3;
                                logging = logging2;
                                msg2 = msg4;
                                if (showSlowLog(slowDispatchThresholdMs2, traceTag3, dispatchStart, Telephony.RcsColumns.RcsMessageDeliveryColumns.DELIVERY_URI_PART, msg4)) {
                                    slowDeliveryDetected = true;
                                    if (logSlowDispatch) {
                                    }
                                    if (logging == null) {
                                    }
                                    newIdent = Binder.clearCallingIdentity();
                                    if (ident == newIdent) {
                                    }
                                    msg3.recycleUnchecked();
                                    slowDeliveryDetected3 = slowDeliveryDetected;
                                    me2 = me;
                                    queue2 = queue;
                                    thresholdOverride2 = thresholdOverride;
                                }
                            } else if (dispatchStart - msg4.when <= 10) {
                                Slog.w(TAG, "Drained");
                                str = TAG;
                                msg2 = msg4;
                                slowDeliveryDetected = false;
                                logging = logging2;
                                if (logSlowDispatch) {
                                    showSlowLog(slowDispatchThresholdMs, dispatchStart, dispatchEnd, "dispatch", msg2);
                                }
                                if (logging == null) {
                                    StringBuilder sb = new StringBuilder();
                                    sb.append("<<<<< Finished to ");
                                    msg3 = msg2;
                                    sb.append(msg3.target);
                                    sb.append(WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER);
                                    sb.append(msg3.callback);
                                    logging.println(sb.toString());
                                } else {
                                    msg3 = msg2;
                                }
                                newIdent = Binder.clearCallingIdentity();
                                if (ident == newIdent) {
                                    Log.wtf(str, "Thread identity changed from 0x" + Long.toHexString(ident) + " to 0x" + Long.toHexString(newIdent) + " while dispatching to " + msg3.target.getClass().getName() + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + msg3.callback + " what=" + msg3.what);
                                }
                                msg3.recycleUnchecked();
                                slowDeliveryDetected3 = slowDeliveryDetected;
                                me2 = me;
                                queue2 = queue;
                                thresholdOverride2 = thresholdOverride;
                            } else {
                                slowDeliveryDetected2 = slowDeliveryDetected3;
                                str = TAG;
                                logging = logging2;
                                msg2 = msg4;
                            }
                            slowDeliveryDetected = slowDeliveryDetected2;
                            if (logSlowDispatch) {
                            }
                            if (logging == null) {
                            }
                            newIdent = Binder.clearCallingIdentity();
                            if (ident == newIdent) {
                            }
                            msg3.recycleUnchecked();
                            slowDeliveryDetected3 = slowDeliveryDetected;
                            me2 = me;
                            queue2 = queue;
                            thresholdOverride2 = thresholdOverride;
                        }
                    } else {
                        thresholdOverride = thresholdOverride2;
                    }
                    logSlowDelivery = false;
                    if (slowDispatchThresholdMs <= 0) {
                    }
                    needStartTime = false;
                    Trace.traceBegin(traceTag2, msg4.target.getTraceName(msg4));
                    if (!needStartTime) {
                    }
                    token = null;
                    if (observer2 != null) {
                    }
                    origWorkSource = ThreadLocalWorkSource.setUid(msg4.workSourceUid);
                    try {
                        HiTraceHandler hitraceHandler2 = HwFrameworkFactory.getHiTraceHandler();
                        hitraceHandler2.srTraceInLooper(msg4);
                        msg4.target.dispatchMessage(msg4);
                        hitraceHandler2.ssTraceInLooper(msg4);
                        if (observer2 != null) {
                        }
                        if (!logSlowDispatch) {
                        }
                        ThreadLocalWorkSource.restore(origWorkSource);
                        if (traceTag2 != 0) {
                        }
                        if (logSlowDelivery) {
                        }
                        slowDeliveryDetected = slowDeliveryDetected2;
                        if (logSlowDispatch) {
                        }
                        if (logging == null) {
                        }
                        newIdent = Binder.clearCallingIdentity();
                        if (ident == newIdent) {
                        }
                        msg3.recycleUnchecked();
                        slowDeliveryDetected3 = slowDeliveryDetected;
                        me2 = me;
                        queue2 = queue;
                        thresholdOverride2 = thresholdOverride;
                    } catch (Exception e2) {
                        exception2 = e2;
                        traceTag = traceTag2;
                        observer = observer2;
                        token2 = token;
                        msg = msg4;
                        if (observer != null) {
                        }
                        throw exception2;
                    } catch (Throwable th3) {
                        exception = th3;
                        traceTag = traceTag2;
                        ThreadLocalWorkSource.restore(origWorkSource);
                        if (traceTag != 0) {
                        }
                        throw exception;
                    }
                } else {
                    return;
                }
            }
        } else {
            throw new RuntimeException("No Looper; Looper.prepare() wasn't called on this thread.");
        }
    }

    private static boolean showSlowLog(long threshold, long measureStart, long measureEnd, String what, Message msg) {
        long actualTime = measureEnd - measureStart;
        if (actualTime < threshold) {
            return false;
        }
        try {
            Slog.w(TAG, "Slow " + what + " took " + actualTime + "ms " + Thread.currentThread().getName() + " h=" + msg.target.getClass().getName() + " c=" + msg.callback + " m=" + msg.what);
            return true;
        } catch (IllegalArgumentException | IllegalStateException e) {
            Slog.e(TAG, "showSlowLog Exception " + e.getMessage());
            return true;
        } catch (Exception e2) {
            Slog.e(TAG, "showSlowLog exception");
            return true;
        }
    }

    public static Looper myLooper() {
        return sThreadLocal.get();
    }

    public static MessageQueue myQueue() {
        return myLooper().mQueue;
    }

    private Looper(boolean quitAllowed) {
        this.mQueue = new MessageQueue(quitAllowed);
    }

    public boolean isCurrentThread() {
        return Thread.currentThread() == this.mThread;
    }

    public void setMessageLogging(Printer printer) {
        this.mLogging = printer;
    }

    @UnsupportedAppUsage
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
        MessageQueue messageQueue = this.mQueue;
        if (messageQueue != null) {
            messageQueue.writeToProto(proto, 1146756268035L);
        }
        proto.end(looperToken);
    }

    public String toString() {
        return "Looper (" + this.mThread.getName() + ", tid " + this.mThread.getId() + ") {" + Integer.toHexString(System.identityHashCode(this)) + "}";
    }
}
