package android.view;

import android.hardware.display.DisplayManagerGlobal;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.os.Trace;
import android.util.Jlog;
import android.util.Log;
import android.util.TimeUtils;
import java.io.PrintWriter;

public final class Choreographer {
    public static final int CALLBACK_ANIMATION = 1;
    public static final int CALLBACK_COMMIT = 3;
    public static final int CALLBACK_INPUT = 0;
    private static final int CALLBACK_LAST = 3;
    private static final String[] CALLBACK_TRACE_TITLES = null;
    public static final int CALLBACK_TRAVERSAL = 2;
    private static final boolean DEBUG_FRAMES = false;
    private static final boolean DEBUG_JANK = false;
    private static final long DEFAULT_FRAME_DELAY = 10;
    private static final Object FRAME_CALLBACK_TOKEN = null;
    private static final int MSG_DO_FRAME = 0;
    private static final int MSG_DO_SCHEDULE_CALLBACK = 2;
    private static final int MSG_DO_SCHEDULE_VSYNC = 1;
    private static final int SKIPPED_FRAME_WARNING_LIMIT = 0;
    private static final String TAG = "Choreographer";
    private static final boolean USE_FRAME_TIME = false;
    private static final boolean USE_VSYNC = false;
    private static volatile long sFrameDelay;
    private static final ThreadLocal<Choreographer> sThreadInstance = null;
    private CallbackRecord mCallbackPool;
    private final CallbackQueue[] mCallbackQueues;
    private boolean mCallbacksRunning;
    private boolean mDebugPrintNextFrameTimeDelta;
    private final FrameDisplayEventReceiver mDisplayEventReceiver;
    private long mFrameDelayTimeOnSync;
    FrameInfo mFrameInfo;
    private long mFrameIntervalNanos;
    private boolean mFrameScheduled;
    private final FrameHandler mHandler;
    private long mLastFrameDoneTime;
    private long mLastFrameTimeNanos;
    private long mLastSkippedFrameEnd;
    private boolean mLastTraversal;
    private final Object mLock;
    private final Looper mLooper;
    private long mRealFrameTime;

    private final class CallbackQueue {
        private CallbackRecord mHead;

        private CallbackQueue() {
        }

        public boolean hasDueCallbacksLocked(long now) {
            return (this.mHead == null || this.mHead.dueTime > now) ? Choreographer.DEBUG_JANK : true;
        }

        public CallbackRecord extractDueCallbacksLocked(long now) {
            CallbackRecord callbacks = this.mHead;
            if (callbacks == null || callbacks.dueTime > now) {
                return null;
            }
            CallbackRecord last = callbacks;
            CallbackRecord next = callbacks.next;
            while (next != null) {
                if (next.dueTime > now) {
                    last.next = null;
                    break;
                }
                last = next;
                next = next.next;
            }
            this.mHead = next;
            return callbacks;
        }

        public void addCallbackLocked(long dueTime, Object action, Object token) {
            CallbackRecord callback = Choreographer.this.obtainCallbackLocked(dueTime, action, token);
            CallbackRecord entry = this.mHead;
            if (entry == null) {
                this.mHead = callback;
            } else if (dueTime < entry.dueTime) {
                callback.next = entry;
                this.mHead = callback;
            } else {
                while (entry.next != null) {
                    if (dueTime < entry.next.dueTime) {
                        callback.next = entry.next;
                        break;
                    }
                    entry = entry.next;
                }
                entry.next = callback;
            }
        }

        public void removeCallbacksLocked(Object action, Object token) {
            CallbackRecord predecessor = null;
            CallbackRecord callback = this.mHead;
            while (callback != null) {
                CallbackRecord next = callback.next;
                if ((action == null || callback.action == action) && (token == null || callback.token == token)) {
                    if (predecessor != null) {
                        predecessor.next = next;
                    } else {
                        this.mHead = next;
                    }
                    Choreographer.this.recycleCallbackLocked(callback);
                } else {
                    predecessor = callback;
                }
                callback = next;
            }
        }
    }

    private static final class CallbackRecord {
        public Object action;
        public long dueTime;
        public CallbackRecord next;
        public Object token;

        private CallbackRecord() {
        }

        public void run(long frameTimeNanos) {
            if (this.token == Choreographer.FRAME_CALLBACK_TOKEN) {
                ((FrameCallback) this.action).doFrame(frameTimeNanos);
            } else {
                ((Runnable) this.action).run();
            }
        }
    }

    public interface FrameCallback {
        void doFrame(long j);
    }

    private final class FrameDisplayEventReceiver extends DisplayEventReceiver implements Runnable {
        private int mFrame;
        private boolean mHavePendingVsync;
        private long mTimestampNanos;

        public FrameDisplayEventReceiver(Looper looper) {
            super(looper);
        }

        public void onVsync(long timestampNanos, int builtInDisplayId, int frame) {
            if (builtInDisplayId != 0) {
                Log.d(Choreographer.TAG, "Received vsync from secondary display, but we don't support this case yet.  Choreographer needs a way to explicitly request vsync for a specific display to ensure it doesn't lose track of its scheduled vsync.");
                scheduleVsync();
                return;
            }
            long now = System.nanoTime();
            if (timestampNanos > now) {
                Log.w(Choreographer.TAG, "Frame time is " + (((float) (timestampNanos - now)) * 1.0E-6f) + " ms in the future!  Check that graphics HAL is generating vsync " + "timestamps using the correct timebase.");
                timestampNanos = now;
            }
            if (this.mHavePendingVsync) {
                Log.w(Choreographer.TAG, "Already have a pending vsync event.  There should only be one at a time.");
            } else {
                this.mHavePendingVsync = true;
            }
            this.mTimestampNanos = timestampNanos;
            this.mFrame = frame;
            Message msg = Message.obtain(Choreographer.this.mHandler, this);
            msg.setAsynchronous(true);
            msg.setVsync(true);
            Choreographer.this.mHandler.sendMessageAtTime(msg, timestampNanos / TimeUtils.NANOS_PER_MS);
        }

        public void run() {
            this.mHavePendingVsync = Choreographer.DEBUG_JANK;
            Choreographer.this.doFrame(this.mTimestampNanos, this.mFrame);
        }
    }

    private final class FrameHandler extends Handler {
        public FrameHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Choreographer.SKIPPED_FRAME_WARNING_LIMIT /*0*/:
                    Choreographer.this.doFrame(System.nanoTime(), Choreographer.SKIPPED_FRAME_WARNING_LIMIT);
                case Choreographer.MSG_DO_SCHEDULE_VSYNC /*1*/:
                    Choreographer.this.doScheduleVsync();
                case Choreographer.MSG_DO_SCHEDULE_CALLBACK /*2*/:
                    Choreographer.this.doScheduleCallback(msg.arg1);
                default:
            }
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.view.Choreographer.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.view.Choreographer.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.view.Choreographer.<clinit>():void");
    }

    private Choreographer(Looper looper) {
        FrameDisplayEventReceiver frameDisplayEventReceiver;
        this.mLock = new Object();
        this.mFrameInfo = new FrameInfo();
        this.mFrameDelayTimeOnSync = 0;
        this.mLastFrameDoneTime = 0;
        this.mRealFrameTime = 0;
        this.mLastTraversal = DEBUG_JANK;
        this.mLastSkippedFrameEnd = 0;
        this.mLooper = looper;
        this.mHandler = new FrameHandler(looper);
        if (USE_VSYNC) {
            frameDisplayEventReceiver = new FrameDisplayEventReceiver(looper);
        } else {
            frameDisplayEventReceiver = null;
        }
        this.mDisplayEventReceiver = frameDisplayEventReceiver;
        this.mLastFrameTimeNanos = Long.MIN_VALUE;
        this.mFrameIntervalNanos = (long) (1.0E9f / getRefreshRate());
        this.mCallbackQueues = new CallbackQueue[4];
        for (int i = SKIPPED_FRAME_WARNING_LIMIT; i <= CALLBACK_LAST; i += MSG_DO_SCHEDULE_VSYNC) {
            this.mCallbackQueues[i] = new CallbackQueue();
        }
    }

    private static float getRefreshRate() {
        return DisplayManagerGlobal.getInstance().getDisplayInfo(SKIPPED_FRAME_WARNING_LIMIT).getMode().getRefreshRate();
    }

    public static Choreographer getInstance() {
        return (Choreographer) sThreadInstance.get();
    }

    public static void releaseInstance() {
        Choreographer old = (Choreographer) sThreadInstance.get();
        sThreadInstance.remove();
        old.dispose();
    }

    private void dispose() {
        this.mDisplayEventReceiver.dispose();
    }

    public static long getFrameDelay() {
        return sFrameDelay;
    }

    public static void setFrameDelay(long frameDelay) {
        sFrameDelay = frameDelay;
    }

    public static long subtractFrameDelay(long delayMillis) {
        long frameDelay = sFrameDelay;
        return delayMillis <= frameDelay ? 0 : delayMillis - frameDelay;
    }

    public long getFrameIntervalNanos() {
        return this.mFrameIntervalNanos;
    }

    void dump(String prefix, PrintWriter writer) {
        String innerPrefix = prefix + "  ";
        writer.print(prefix);
        writer.println("Choreographer:");
        writer.print(innerPrefix);
        writer.print("mFrameScheduled=");
        writer.println(this.mFrameScheduled);
        writer.print(innerPrefix);
        writer.print("mLastFrameTime=");
        writer.println(TimeUtils.formatUptime(this.mLastFrameTimeNanos / TimeUtils.NANOS_PER_MS));
    }

    public void postCallback(int callbackType, Runnable action, Object token) {
        postCallbackDelayed(callbackType, action, token, 0);
    }

    public void postCallbackDelayed(int callbackType, Runnable action, Object token, long delayMillis) {
        if (action == null) {
            throw new IllegalArgumentException("action must not be null");
        } else if (callbackType < 0 || callbackType > CALLBACK_LAST) {
            throw new IllegalArgumentException("callbackType is invalid");
        } else {
            postCallbackDelayedInternal(callbackType, action, token, delayMillis);
        }
    }

    private void postCallbackDelayedInternal(int callbackType, Object action, Object token, long delayMillis) {
        synchronized (this.mLock) {
            long now = SystemClock.uptimeMillis();
            long dueTime = now + delayMillis;
            this.mCallbackQueues[callbackType].addCallbackLocked(dueTime, action, token);
            if (dueTime <= now) {
                scheduleFrameLocked(now);
            } else {
                Message msg = this.mHandler.obtainMessage(MSG_DO_SCHEDULE_CALLBACK, action);
                msg.arg1 = callbackType;
                msg.setAsynchronous(true);
                this.mHandler.sendMessageAtTime(msg, dueTime);
            }
        }
    }

    public void removeCallbacks(int callbackType, Runnable action, Object token) {
        if (callbackType < 0 || callbackType > CALLBACK_LAST) {
            throw new IllegalArgumentException("callbackType is invalid");
        }
        removeCallbacksInternal(callbackType, action, token);
    }

    private void removeCallbacksInternal(int callbackType, Object action, Object token) {
        synchronized (this.mLock) {
            this.mCallbackQueues[callbackType].removeCallbacksLocked(action, token);
            if (action != null && token == null) {
                this.mHandler.removeMessages(MSG_DO_SCHEDULE_CALLBACK, action);
            }
        }
    }

    public void postFrameCallback(FrameCallback callback) {
        postFrameCallbackDelayed(callback, 0);
    }

    public void postFrameCallbackDelayed(FrameCallback callback, long delayMillis) {
        if (callback == null) {
            throw new IllegalArgumentException("callback must not be null");
        }
        postCallbackDelayedInternal(MSG_DO_SCHEDULE_VSYNC, callback, FRAME_CALLBACK_TOKEN, delayMillis);
    }

    public void removeFrameCallback(FrameCallback callback) {
        if (callback == null) {
            throw new IllegalArgumentException("callback must not be null");
        }
        removeCallbacksInternal(MSG_DO_SCHEDULE_VSYNC, callback, FRAME_CALLBACK_TOKEN);
    }

    public long getFrameTime() {
        return getFrameTimeNanos() / TimeUtils.NANOS_PER_MS;
    }

    public long getFrameTimeNanos() {
        long nanoTime;
        synchronized (this.mLock) {
            if (this.mCallbacksRunning) {
                nanoTime = USE_FRAME_TIME ? this.mLastFrameTimeNanos : System.nanoTime();
            } else {
                throw new IllegalStateException("This method must only be called as part of a callback while a frame is in progress.");
            }
        }
        return nanoTime;
    }

    public long getLastFrameDoneTime() {
        return this.mLastFrameDoneTime;
    }

    public long getRealFrameTime() {
        return this.mRealFrameTime;
    }

    public long getDoFrameDelayTime() {
        return this.mFrameDelayTimeOnSync;
    }

    public boolean isLastTraversal() {
        return this.mLastTraversal;
    }

    public void setLastSkippedFrameEndTime(long endtime) {
        this.mLastSkippedFrameEnd = endtime;
    }

    public long getLastSkippedFrameEndTime() {
        return this.mLastSkippedFrameEnd;
    }

    private void scheduleFrameLocked(long now) {
        if (!this.mFrameScheduled) {
            this.mFrameScheduled = true;
            Message msg;
            if (!USE_VSYNC) {
                long nextFrameTime = Math.max((this.mLastFrameTimeNanos / TimeUtils.NANOS_PER_MS) + sFrameDelay, now);
                msg = this.mHandler.obtainMessage(SKIPPED_FRAME_WARNING_LIMIT);
                msg.setAsynchronous(true);
                this.mHandler.sendMessageAtTime(msg, nextFrameTime);
            } else if (isRunningOnLooperThreadLocked()) {
                scheduleVsyncLocked();
            } else {
                msg = this.mHandler.obtainMessage(MSG_DO_SCHEDULE_VSYNC);
                msg.setAsynchronous(true);
                this.mHandler.sendMessageAtFrontOfQueue(msg);
            }
        }
    }

    void doFrame(long frameTimeNanos, int frame) {
        synchronized (this.mLock) {
            if (this.mFrameScheduled) {
                long intendedFrameTimeNanos = frameTimeNanos;
                long startNanos = System.nanoTime();
                long jitterNanos = startNanos - frameTimeNanos;
                this.mRealFrameTime = frameTimeNanos;
                this.mFrameDelayTimeOnSync = jitterNanos;
                this.mLastTraversal = DEBUG_JANK;
                if (jitterNanos >= this.mFrameIntervalNanos) {
                    long skippedFrames = jitterNanos / this.mFrameIntervalNanos;
                    Jlog.animationSkipFrames(skippedFrames);
                    if (skippedFrames >= ((long) SKIPPED_FRAME_WARNING_LIMIT)) {
                        Log.i(TAG, "Skipped " + skippedFrames + " frames!  " + "The application may be doing too much work on its main thread.");
                    }
                    frameTimeNanos = startNanos - (jitterNanos % this.mFrameIntervalNanos);
                }
                if (frameTimeNanos < this.mLastFrameTimeNanos) {
                    scheduleVsyncLocked();
                    return;
                }
                this.mFrameInfo.setVsync(intendedFrameTimeNanos, frameTimeNanos);
                this.mFrameScheduled = DEBUG_JANK;
                this.mLastFrameTimeNanos = frameTimeNanos;
                try {
                    Trace.traceBegin(8, "Choreographer#doFrame");
                    this.mFrameInfo.markInputHandlingStart();
                    doCallbacks(SKIPPED_FRAME_WARNING_LIMIT, frameTimeNanos);
                    this.mFrameInfo.markAnimationsStart();
                    doCallbacks(MSG_DO_SCHEDULE_VSYNC, frameTimeNanos);
                    this.mFrameInfo.markPerformTraversalsStart();
                    doCallbacks(MSG_DO_SCHEDULE_CALLBACK, frameTimeNanos);
                    doCallbacks(CALLBACK_LAST, frameTimeNanos);
                    this.mLastFrameDoneTime = System.nanoTime();
                } finally {
                    Trace.traceEnd(8);
                }
            }
        }
    }

    void doCallbacks(int callbackType, long frameTimeNanos) {
        synchronized (this.mLock) {
            long now = System.nanoTime();
            CallbackRecord callbacks = this.mCallbackQueues[callbackType].extractDueCallbacksLocked(now / TimeUtils.NANOS_PER_MS);
            if (callbacks == null) {
                return;
            }
            this.mCallbacksRunning = true;
            if (callbackType == CALLBACK_LAST) {
                long jitterNanos = now - frameTimeNanos;
                Trace.traceCounter(8, "jitterNanos", (int) jitterNanos);
                if (jitterNanos >= this.mFrameIntervalNanos * 2) {
                    long j = this.mFrameIntervalNanos;
                    frameTimeNanos = now - ((jitterNanos % r0) + this.mFrameIntervalNanos);
                    this.mLastFrameTimeNanos = frameTimeNanos;
                }
            }
            CallbackRecord next;
            try {
                Trace.traceBegin(8, CALLBACK_TRACE_TITLES[callbackType]);
                CallbackRecord c = callbacks;
                while (c != null) {
                    if (callbackType == MSG_DO_SCHEDULE_CALLBACK && c.next == null) {
                        this.mLastTraversal = true;
                    }
                    c.run(frameTimeNanos);
                    c = c.next;
                }
                synchronized (this.mLock) {
                    this.mCallbacksRunning = DEBUG_JANK;
                    do {
                        next = callbacks.next;
                        recycleCallbackLocked(callbacks);
                        callbacks = next;
                    } while (next != null);
                }
                Trace.traceEnd(8);
            } catch (Throwable th) {
                synchronized (this.mLock) {
                }
                this.mCallbacksRunning = DEBUG_JANK;
                do {
                    next = callbacks.next;
                    recycleCallbackLocked(callbacks);
                    callbacks = next;
                } while (next != null);
                Trace.traceEnd(8);
            }
        }
    }

    void doScheduleVsync() {
        synchronized (this.mLock) {
            if (this.mFrameScheduled) {
                scheduleVsyncLocked();
            }
        }
    }

    void doScheduleCallback(int callbackType) {
        synchronized (this.mLock) {
            if (!this.mFrameScheduled) {
                long now = SystemClock.uptimeMillis();
                if (this.mCallbackQueues[callbackType].hasDueCallbacksLocked(now)) {
                    scheduleFrameLocked(now);
                }
            }
        }
    }

    private void scheduleVsyncLocked() {
        this.mDisplayEventReceiver.scheduleVsync();
    }

    private boolean isRunningOnLooperThreadLocked() {
        return Looper.myLooper() == this.mLooper ? true : DEBUG_JANK;
    }

    private CallbackRecord obtainCallbackLocked(long dueTime, Object action, Object token) {
        CallbackRecord callback = this.mCallbackPool;
        if (callback == null) {
            callback = new CallbackRecord();
        } else {
            this.mCallbackPool = callback.next;
            callback.next = null;
        }
        callback.dueTime = dueTime;
        callback.action = action;
        callback.token = token;
        return callback;
    }

    private void recycleCallbackLocked(CallbackRecord callback) {
        callback.action = null;
        callback.token = null;
        callback.next = this.mCallbackPool;
        this.mCallbackPool = callback;
    }
}
