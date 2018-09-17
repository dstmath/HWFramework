package android.os;

import android.net.wifi.WifiEnterpriseConfig;
import android.util.Log;
import android.util.Printer;

public final class Looper {
    private static final String TAG = "Looper";
    private static Looper sMainLooper;
    static final ThreadLocal<Looper> sThreadLocal = null;
    private Printer mLogging;
    final MessageQueue mQueue;
    final Thread mThread;
    private long mTraceTag;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.os.Looper.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.os.Looper.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.os.Looper.<clinit>():void");
    }

    public static void prepare() {
        prepare(true);
    }

    private static void prepare(boolean quitAllowed) {
        if (sThreadLocal.get() != null) {
            throw new RuntimeException("Only one Looper may be created per thread");
        }
        sThreadLocal.set(new Looper(quitAllowed));
    }

    public static void prepareMainLooper() {
        prepare(false);
        synchronized (Looper.class) {
            if (sMainLooper != null) {
                throw new IllegalStateException("The main Looper has already been prepared.");
            }
            sMainLooper = myLooper();
        }
    }

    public static Looper getMainLooper() {
        Looper looper;
        synchronized (Looper.class) {
            looper = sMainLooper;
        }
        return looper;
    }

    public static void loop() {
        Looper me = myLooper();
        if (me == null) {
            throw new RuntimeException("No Looper; Looper.prepare() wasn't called on this thread.");
        }
        MessageQueue queue = me.mQueue;
        Binder.clearCallingIdentity();
        long ident = Binder.clearCallingIdentity();
        Message msg;
        Printer logging;
        long newIdent;
        if (!BlockMonitor.isNeedMonitor()) {
            while (true) {
                msg = queue.next();
                if (msg != null) {
                    logging = me.mLogging;
                    if (logging != null) {
                        logging.println(">>>>> Dispatching to " + msg.target + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + msg.callback + ": " + msg.what);
                    }
                    long traceTag = me.mTraceTag;
                    if (traceTag != 0) {
                        Trace.traceBegin(traceTag, msg.target.getTraceName(msg));
                    }
                    try {
                        msg.target.dispatchMessage(msg);
                        if (logging != null) {
                            logging.println("<<<<< Finished to " + msg.target + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + msg.callback);
                        }
                        newIdent = Binder.clearCallingIdentity();
                        if (ident != newIdent) {
                            Log.wtf(TAG, "Thread identity changed from 0x" + Long.toHexString(ident) + " to 0x" + Long.toHexString(newIdent) + " while dispatching to " + msg.target.getClass().getName() + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + msg.callback + " what=" + msg.what);
                        }
                        msg.recycleUnchecked();
                    } finally {
                        if (traceTag != 0) {
                            Trace.traceEnd(traceTag);
                        }
                        break;
                    }
                }
                return;
            }
        }
        while (true) {
            msg = queue.next();
            if (msg != null) {
                logging = me.mLogging;
                if (logging != null) {
                    logging.println(">>>>> Dispatching to " + msg.target + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + msg.callback + ": " + msg.what);
                }
                long startTime = SystemClock.uptimeMillis();
                msg.target.dispatchMessage(msg);
                BlockMonitor.checkMessageTime(startTime, msg);
                BlockMonitor.checkMessageCount(queue);
                if (logging != null) {
                    logging.println("<<<<< Finished to " + msg.target + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + msg.callback);
                }
                newIdent = Binder.clearCallingIdentity();
                if (ident != newIdent) {
                    Log.wtf(TAG, "Thread identity changed from 0x" + Long.toHexString(ident) + " to 0x" + Long.toHexString(newIdent) + " while dispatching to " + msg.target.getClass().getName() + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + msg.callback + " what=" + msg.what);
                }
                msg.recycleUnchecked();
            } else {
                return;
            }
        }
    }

    public static Looper myLooper() {
        return (Looper) sThreadLocal.get();
    }

    public static MessageQueue myQueue() {
        return myLooper().mQueue;
    }

    private Looper(boolean quitAllowed) {
        this.mQueue = new MessageQueue(quitAllowed);
        this.mThread = Thread.currentThread();
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
        this.mQueue.dump(pw, prefix + "  ");
    }

    public String toString() {
        return "Looper (" + this.mThread.getName() + ", tid " + this.mThread.getId() + ") {" + Integer.toHexString(System.identityHashCode(this)) + "}";
    }
}
