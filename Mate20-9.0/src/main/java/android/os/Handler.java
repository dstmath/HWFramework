package android.os;

import android.common.HwFrameworkFactory;
import android.os.IMessenger;
import android.util.HwEtrace;
import android.util.Log;
import android.util.Printer;

public class Handler {
    private static final boolean FIND_POTENTIAL_LEAKS = false;
    private static Handler MAIN_THREAD_HANDLER = null;
    private static final String TAG = "Handler";
    final boolean mAsynchronous;
    final Callback mCallback;
    Looper mLooper;
    IMessenger mMessenger;
    final MessageQueue mQueue;

    private static final class BlockingRunnable implements Runnable {
        private boolean mDone;
        private final Runnable mTask;

        public BlockingRunnable(Runnable task) {
            this.mTask = task;
        }

        public void run() {
            try {
                this.mTask.run();
                synchronized (this) {
                    this.mDone = true;
                    notifyAll();
                }
            } catch (Throwable th) {
                synchronized (this) {
                    this.mDone = true;
                    notifyAll();
                    throw th;
                }
            }
        }

        public boolean postAndWait(Handler handler, long timeout) {
            if (!handler.post(this)) {
                return false;
            }
            synchronized (this) {
                if (timeout > 0) {
                    long expirationTime = SystemClock.uptimeMillis() + timeout;
                    while (!this.mDone) {
                        long delay = expirationTime - SystemClock.uptimeMillis();
                        if (delay <= 0) {
                            return false;
                        }
                        try {
                            wait(delay);
                        } catch (InterruptedException e) {
                        }
                    }
                } else {
                    while (!this.mDone) {
                        try {
                            wait();
                        } catch (InterruptedException e2) {
                        }
                    }
                }
                return true;
            }
        }
    }

    public interface Callback {
        boolean handleMessage(Message message);
    }

    private final class MessengerImpl extends IMessenger.Stub {
        private MessengerImpl() {
        }

        public void send(Message msg) {
            msg.sendingUid = Binder.getCallingUid();
            Handler.this.sendMessage(msg);
        }
    }

    public void handleMessage(Message msg) {
    }

    public void dispatchMessage(Message msg) {
        if (msg.callback != null) {
            handleCallback(msg);
        } else if (this.mCallback == null || !this.mCallback.handleMessage(msg)) {
            handleMessage(msg);
        }
    }

    public Handler() {
        this((Callback) null, false);
    }

    public Handler(Callback callback) {
        this(callback, false);
    }

    public Handler(Looper looper) {
        this(looper, null, false);
    }

    public Handler(Looper looper, Callback callback) {
        this(looper, callback, false);
    }

    public Handler(boolean async) {
        this((Callback) null, async);
    }

    public Handler(Callback callback, boolean async) {
        this.mLooper = Looper.myLooper();
        if (this.mLooper == null) {
            Looper.prepare();
            this.mLooper = Looper.myLooper();
            if (this.mLooper == null) {
                throw new RuntimeException("Can't create handler inside thread " + Thread.currentThread() + " that has not called Looper.prepare()");
            }
        }
        this.mQueue = this.mLooper.mQueue;
        this.mCallback = callback;
        this.mAsynchronous = async;
    }

    public Handler(Looper looper, Callback callback, boolean async) {
        this.mLooper = looper;
        this.mQueue = looper.mQueue;
        this.mCallback = callback;
        this.mAsynchronous = async;
    }

    public static Handler createAsync(Looper looper) {
        if (looper != null) {
            return new Handler(looper, null, true);
        }
        throw new NullPointerException("looper must not be null");
    }

    public static Handler createAsync(Looper looper, Callback callback) {
        if (looper == null) {
            throw new NullPointerException("looper must not be null");
        } else if (callback != null) {
            return new Handler(looper, callback, true);
        } else {
            throw new NullPointerException("callback must not be null");
        }
    }

    public static Handler getMain() {
        if (MAIN_THREAD_HANDLER == null) {
            MAIN_THREAD_HANDLER = new Handler(Looper.getMainLooper());
        }
        return MAIN_THREAD_HANDLER;
    }

    public static Handler mainIfNull(Handler handler) {
        return handler == null ? getMain() : handler;
    }

    public String getTraceName(Message message) {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getName());
        sb.append(": ");
        if (message.callback != null) {
            sb.append(message.callback.getClass().getName());
        } else {
            sb.append("#");
            sb.append(message.what);
        }
        return sb.toString();
    }

    public String getMessageName(Message message) {
        if (message.callback != null) {
            return message.callback.getClass().getName();
        }
        return "0x" + Integer.toHexString(message.what);
    }

    public final Message obtainMessage() {
        return Message.obtain(this);
    }

    public final Message obtainMessage(int what) {
        return Message.obtain(this, what);
    }

    public final Message obtainMessage(int what, Object obj) {
        return Message.obtain(this, what, obj);
    }

    public final Message obtainMessage(int what, int arg1, int arg2) {
        return Message.obtain(this, what, arg1, arg2);
    }

    public final Message obtainMessage(int what, int arg1, int arg2, Object obj) {
        return Message.obtain(this, what, arg1, arg2, obj);
    }

    public final boolean post(Runnable r) {
        return sendMessageDelayed(getPostMessage(r), 0);
    }

    public final boolean postAtTime(Runnable r, long uptimeMillis) {
        return sendMessageAtTime(getPostMessage(r), uptimeMillis);
    }

    public final boolean postAtTime(Runnable r, Object token, long uptimeMillis) {
        return sendMessageAtTime(getPostMessage(r, token), uptimeMillis);
    }

    public final boolean postDelayed(Runnable r, long delayMillis) {
        return sendMessageDelayed(getPostMessage(r), delayMillis);
    }

    public final boolean postDelayed(Runnable r, Object token, long delayMillis) {
        return sendMessageDelayed(getPostMessage(r, token), delayMillis);
    }

    public final boolean postAtFrontOfQueue(Runnable r) {
        return sendMessageAtFrontOfQueue(getPostMessage(r));
    }

    public final boolean runWithScissors(Runnable r, long timeout) {
        if (r == null) {
            throw new IllegalArgumentException("runnable must not be null");
        } else if (timeout < 0) {
            throw new IllegalArgumentException("timeout must be non-negative");
        } else if (Looper.myLooper() != this.mLooper) {
            return new BlockingRunnable(r).postAndWait(this, timeout);
        } else {
            r.run();
            return true;
        }
    }

    public final void removeCallbacks(Runnable r) {
        this.mQueue.removeMessages(this, r, (Object) null);
    }

    public final void removeCallbacks(Runnable r, Object token) {
        this.mQueue.removeMessages(this, r, token);
    }

    public final boolean sendMessage(Message msg) {
        return sendMessageDelayed(msg, 0);
    }

    public final boolean sendEmptyMessage(int what) {
        return sendEmptyMessageDelayed(what, 0);
    }

    public final boolean sendEmptyMessageDelayed(int what, long delayMillis) {
        Message msg = Message.obtain();
        msg.what = what;
        return sendMessageDelayed(msg, delayMillis);
    }

    public final boolean sendEmptyMessageAtTime(int what, long uptimeMillis) {
        Message msg = Message.obtain();
        msg.what = what;
        return sendMessageAtTime(msg, uptimeMillis);
    }

    public final boolean sendMessageDelayed(Message msg, long delayMillis) {
        if (delayMillis < 0) {
            delayMillis = 0;
        }
        if (this.mQueue != null && this.mQueue.isReduceDelay() && delayMillis >= 400 && delayMillis <= 600 && Looper.myLooper() == Looper.getMainLooper()) {
            delayMillis = 10;
        }
        return sendMessageAtTime(msg, SystemClock.uptimeMillis() + HwFrameworkFactory.getHwHandler().resetMessageDelayMillis(msg, delayMillis));
    }

    public boolean sendMessageAtTime(Message msg, long uptimeMillis) {
        MessageQueue queue = this.mQueue;
        if (queue != null) {
            return enqueueMessage(queue, msg, uptimeMillis);
        }
        RuntimeException e = new RuntimeException(this + " sendMessageAtTime() called with no mQueue");
        Log.w("Looper", e.getMessage(), e);
        return false;
    }

    public final boolean sendMessageAtFrontOfQueue(Message msg) {
        MessageQueue queue = this.mQueue;
        if (queue != null) {
            return enqueueMessage(queue, msg, 0);
        }
        RuntimeException e = new RuntimeException(this + " sendMessageAtTime() called with no mQueue");
        Log.w("Looper", e.getMessage(), e);
        return false;
    }

    public final boolean executeOrSendMessage(Message msg) {
        if (this.mLooper != Looper.myLooper()) {
            return sendMessage(msg);
        }
        dispatchMessage(msg);
        return true;
    }

    private boolean enqueueMessage(MessageQueue queue, Message msg, long uptimeMillis) {
        msg.target = this;
        writeEtraceIdFromTlsToMsg(msg);
        if (this.mAsynchronous) {
            msg.setAsynchronous(true);
        }
        return queue.enqueueMessage(msg, uptimeMillis);
    }

    private static void writeEtraceIdFromTlsToMsg(Message msg) {
        msg.etraceID = HwEtrace.getTlsIdAndNewSpanForCaller(2);
    }

    public final void removeMessages(int what) {
        this.mQueue.removeMessages(this, what, (Object) null);
    }

    public final void removeMessages(int what, Object object) {
        this.mQueue.removeMessages(this, what, object);
    }

    public final void removeCallbacksAndMessages(Object token) {
        this.mQueue.removeCallbacksAndMessages(this, token);
    }

    public final boolean hasMessages(int what) {
        return this.mQueue.hasMessages(this, what, (Object) null);
    }

    public final boolean hasMessagesOrCallbacks() {
        return this.mQueue.hasMessages(this);
    }

    public final boolean hasMessages(int what, Object object) {
        return this.mQueue.hasMessages(this, what, object);
    }

    public final boolean hasCallbacks(Runnable r) {
        return this.mQueue.hasMessages(this, r, (Object) null);
    }

    public final Looper getLooper() {
        return this.mLooper;
    }

    public final void dump(Printer pw, String prefix) {
        pw.println(prefix + this + " @ " + SystemClock.uptimeMillis());
        if (this.mLooper == null) {
            pw.println(prefix + "looper uninitialized");
            return;
        }
        Looper looper = this.mLooper;
        looper.dump(pw, prefix + "  ");
    }

    public final void dumpMine(Printer pw, String prefix) {
        pw.println(prefix + this + " @ " + SystemClock.uptimeMillis());
        if (this.mLooper == null) {
            pw.println(prefix + "looper uninitialized");
            return;
        }
        Looper looper = this.mLooper;
        looper.dump(pw, prefix + "  ", this);
    }

    public String toString() {
        return "Handler (" + getClass().getName() + ") {" + Integer.toHexString(System.identityHashCode(this)) + "}";
    }

    /* access modifiers changed from: package-private */
    public final IMessenger getIMessenger() {
        synchronized (this.mQueue) {
            if (this.mMessenger != null) {
                IMessenger iMessenger = this.mMessenger;
                return iMessenger;
            }
            this.mMessenger = new MessengerImpl();
            IMessenger iMessenger2 = this.mMessenger;
            return iMessenger2;
        }
    }

    private static Message getPostMessage(Runnable r) {
        Message m = Message.obtain();
        m.callback = r;
        return m;
    }

    private static Message getPostMessage(Runnable r, Object token) {
        Message m = Message.obtain();
        m.obj = token;
        m.callback = r;
        return m;
    }

    private static void handleCallback(Message message) {
        message.callback.run();
    }
}
