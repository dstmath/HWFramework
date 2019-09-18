package android.view;

import android.hardware.display.DisplayManagerGlobal;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.Trace;
import android.telephony.SubscriptionPlan;
import android.util.Jlog;
import android.util.Log;
import android.util.TimeUtils;
import android.view.animation.AnimationUtils;
import java.io.PrintWriter;

public final class Choreographer {
    public static final int CALLBACK_ANIMATION = 1;
    public static final int CALLBACK_COMMIT = 3;
    public static final int CALLBACK_INPUT = 0;
    private static final int CALLBACK_LAST = 3;
    private static final String[] CALLBACK_TRACE_TITLES = {"input", "animation", "traversal", "commit"};
    public static final int CALLBACK_TRAVERSAL = 2;
    private static final boolean DEBUG_FRAMES = false;
    private static final boolean DEBUG_JANK = false;
    private static final long DEFAULT_FRAME_DELAY = 10;
    /* access modifiers changed from: private */
    public static final Object FRAME_CALLBACK_TOKEN = new Object() {
        public String toString() {
            return "FRAME_CALLBACK_TOKEN";
        }
    };
    private static final boolean IS_DEBUG_VERSION;
    private static final int MSG_DO_FRAME = 0;
    private static final int MSG_DO_SCHEDULE_CALLBACK = 2;
    private static final int MSG_DO_SCHEDULE_VSYNC = 1;
    private static final int SKIPPED_FRAME_WARNING_LIMIT = SystemProperties.getInt("debug.choreographer.skipwarning", 30);
    private static final String TAG = "Choreographer";
    public static final long TOUNCH_RESPONSE_TIME_LIMIT = 500000000;
    private static final boolean USE_FRAME_TIME = SystemProperties.getBoolean("debug.choreographer.frametime", true);
    private static final boolean USE_VSYNC = SystemProperties.getBoolean("debug.choreographer.vsync", true);
    /* access modifiers changed from: private */
    public static volatile Choreographer mMainInstance;
    private static volatile long sFrameDelay = DEFAULT_FRAME_DELAY;
    private static final ThreadLocal<Choreographer> sSfThreadInstance = new ThreadLocal<Choreographer>() {
        /* access modifiers changed from: protected */
        public Choreographer initialValue() {
            Looper looper = Looper.myLooper();
            if (looper != null) {
                return new Choreographer(looper, 1);
            }
            throw new IllegalStateException("The current thread must have a looper!");
        }
    };
    private static final ThreadLocal<Choreographer> sThreadInstance = new ThreadLocal<Choreographer>() {
        /* access modifiers changed from: protected */
        public Choreographer initialValue() {
            Looper looper = Looper.myLooper();
            if (looper != null) {
                Choreographer choreographer = new Choreographer(looper, 0);
                if (looper == Looper.getMainLooper()) {
                    Choreographer unused = Choreographer.mMainInstance = choreographer;
                }
                return choreographer;
            }
            throw new IllegalStateException("The current thread must have a looper!");
        }
    };
    protected boolean isNeedDraw;
    private int mCallBackCounts;
    private CallbackRecord mCallbackPool;
    private final CallbackQueue[] mCallbackQueues;
    private boolean mCallbacksRunning;
    private boolean mDebugPrintNextFrameTimeDelta;
    private final FrameDisplayEventReceiver mDisplayEventReceiver;
    private int mFPSDivisor;
    private long mFrameDelayTimeOnSync;
    FrameInfo mFrameInfo;
    private long mFrameIntervalNanos;
    private boolean mFrameScheduled;
    /* access modifiers changed from: private */
    public final FrameHandler mHandler;
    private boolean mInDoframe;
    private long mLastFrameDoneTime;
    private long mLastFrameTimeNanos;
    private long mLastInputTime;
    private long mLastSkippedFrameEnd;
    private boolean mLastTraversal;
    private final Object mLock;
    private final Looper mLooper;
    private long mOldestInputTime;
    private long mRealFrameTime;

    private final class CallbackQueue {
        private CallbackRecord mHead;

        private CallbackQueue() {
        }

        public boolean hasDueCallbacksLocked(long now) {
            return this.mHead != null && this.mHead.dueTime <= now;
        }

        public CallbackRecord extractDueCallbacksLocked(long now) {
            CallbackRecord callbacks = this.mHead;
            if (callbacks == null || callbacks.dueTime > now) {
                return null;
            }
            CallbackRecord last = callbacks;
            CallbackRecord next = last.next;
            while (true) {
                if (next == null) {
                    break;
                } else if (next.dueTime > now) {
                    last.next = null;
                    break;
                } else {
                    last = next;
                    next = next.next;
                }
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
                while (true) {
                    if (entry.next == null) {
                        break;
                    } else if (dueTime < entry.next.dueTime) {
                        callback.next = entry.next;
                        break;
                    } else {
                        entry = entry.next;
                    }
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
                    Choreographer.access$810(Choreographer.this);
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

        public FrameDisplayEventReceiver(Looper looper, int vsyncSource) {
            super(looper, vsyncSource);
        }

        public void onVsync(long timestampNanos, int builtInDisplayId, int frame) {
            if (builtInDisplayId != 0) {
                Log.d(Choreographer.TAG, "Received vsync from secondary display, but we don't support this case yet.  Choreographer needs a way to explicitly request vsync for a specific display to ensure it doesn't lose track of its scheduled vsync.");
                scheduleVsync();
                return;
            }
            long now = System.nanoTime();
            if (timestampNanos > now) {
                Log.w(Choreographer.TAG, "Frame time is " + (((float) (timestampNanos - now)) * 1.0E-6f) + " ms in the future!  Check that graphics HAL is generating vsync timestamps using the correct timebase.");
                timestampNanos = now;
            }
            if (this.mHavePendingVsync) {
                Log.w(Choreographer.TAG, "Already have a pending vsync event.  There should only be one at a time.");
            } else {
                this.mHavePendingVsync = true;
            }
            this.mTimestampNanos = timestampNanos;
            this.mFrame = frame;
            Message msg = Message.obtain((Handler) Choreographer.this.mHandler, (Runnable) this);
            msg.setAsynchronous(true);
            msg.setVsync(true);
            Choreographer.this.mHandler.sendMessageAtTime(msg, timestampNanos / TimeUtils.NANOS_PER_MS);
        }

        public void run() {
            this.mHavePendingVsync = false;
            Choreographer.this.doFrame(this.mTimestampNanos, this.mFrame);
        }
    }

    private final class FrameHandler extends Handler {
        public FrameHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    Choreographer.this.doFrame(System.nanoTime(), 0);
                    return;
                case 1:
                    Choreographer.this.doScheduleVsync();
                    return;
                case 2:
                    Choreographer.this.doScheduleCallback(msg.arg1);
                    return;
                default:
                    return;
            }
        }
    }

    static /* synthetic */ int access$810(Choreographer x0) {
        int i = x0.mCallBackCounts;
        x0.mCallBackCounts = i - 1;
        return i;
    }

    static {
        boolean z = true;
        if (SystemProperties.getInt("ro.logsystem.usertype", 1) != 3) {
            z = false;
        }
        IS_DEBUG_VERSION = z;
    }

    public void updateOldestInputTime(long inputTime) {
        if (inputTime < this.mOldestInputTime) {
            this.mOldestInputTime = inputTime;
        }
    }

    public void checkOldestInputTime() {
        if (!this.mInDoframe && this.mCallBackCounts <= 0) {
            this.mOldestInputTime = SubscriptionPlan.BYTES_UNLIMITED;
        }
    }

    public void checkTounchResponseTime(CharSequence title, long nowTime) {
        if (this.mOldestInputTime != SubscriptionPlan.BYTES_UNLIMITED && this.mLastInputTime != this.mOldestInputTime) {
            this.mLastInputTime = this.mOldestInputTime;
            long tounchResponseTime = nowTime - this.mOldestInputTime;
            if (tounchResponseTime >= TOUNCH_RESPONSE_TIME_LIMIT) {
                Jlog.d(360, "#ARG1:<" + title + ">#ARG2:<" + (tounchResponseTime / TimeUtils.NANOS_PER_MS) + ">");
            }
        }
    }

    private Choreographer(Looper looper, int vsyncSource) {
        FrameDisplayEventReceiver frameDisplayEventReceiver;
        this.mLock = new Object();
        this.mFPSDivisor = 1;
        this.mFrameInfo = new FrameInfo();
        this.mFrameDelayTimeOnSync = 0;
        this.mLastFrameDoneTime = 0;
        this.mRealFrameTime = 0;
        int i = 0;
        this.mLastTraversal = false;
        this.mLastSkippedFrameEnd = 0;
        this.isNeedDraw = false;
        this.mInDoframe = false;
        this.mCallBackCounts = 0;
        this.mOldestInputTime = SubscriptionPlan.BYTES_UNLIMITED;
        this.mLastInputTime = 0;
        this.mLooper = looper;
        this.mHandler = new FrameHandler(looper);
        if (USE_VSYNC) {
            frameDisplayEventReceiver = new FrameDisplayEventReceiver(looper, vsyncSource);
        } else {
            frameDisplayEventReceiver = null;
        }
        this.mDisplayEventReceiver = frameDisplayEventReceiver;
        this.mLastFrameTimeNanos = Long.MIN_VALUE;
        this.mFrameIntervalNanos = (long) (1.0E9f / getRefreshRate());
        this.mCallbackQueues = new CallbackQueue[4];
        while (true) {
            int i2 = i;
            if (i2 <= 3) {
                this.mCallbackQueues[i2] = new CallbackQueue();
                i = i2 + 1;
            } else {
                setFPSDivisor(SystemProperties.getInt(ThreadedRenderer.DEBUG_FPS_DIVISOR, 1));
                return;
            }
        }
    }

    private static float getRefreshRate() {
        return DisplayManagerGlobal.getInstance().getDisplayInfo(0).getMode().getRefreshRate();
    }

    public static Choreographer getInstance() {
        return sThreadInstance.get();
    }

    public static Choreographer getSfInstance() {
        return sSfThreadInstance.get();
    }

    public static Choreographer getMainThreadInstance() {
        return mMainInstance;
    }

    public static void releaseInstance() {
        sThreadInstance.remove();
        sThreadInstance.get().dispose();
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
        if (delayMillis <= frameDelay) {
            return 0;
        }
        return delayMillis - frameDelay;
    }

    public long getFrameIntervalNanos() {
        return this.mFrameIntervalNanos;
    }

    /* access modifiers changed from: package-private */
    public void dump(String prefix, PrintWriter writer) {
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
        } else if (callbackType < 0 || callbackType > 3) {
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
            this.mCallBackCounts++;
            if (dueTime <= now) {
                scheduleFrameLocked(now);
            } else {
                Message msg = this.mHandler.obtainMessage(2, action);
                msg.arg1 = callbackType;
                msg.setAsynchronous(true);
                this.mHandler.sendMessageAtTime(msg, dueTime);
            }
        }
    }

    public void removeCallbacks(int callbackType, Runnable action, Object token) {
        if (callbackType < 0 || callbackType > 3) {
            throw new IllegalArgumentException("callbackType is invalid");
        }
        removeCallbacksInternal(callbackType, action, token);
    }

    private void removeCallbacksInternal(int callbackType, Object action, Object token) {
        synchronized (this.mLock) {
            this.mCallbackQueues[callbackType].removeCallbacksLocked(action, token);
            if (action != null && token == null) {
                this.mHandler.removeMessages(2, action);
            }
        }
    }

    public void postFrameCallback(FrameCallback callback) {
        postFrameCallbackDelayed(callback, 0);
    }

    public void postFrameCallbackDelayed(FrameCallback callback, long delayMillis) {
        if (callback != null) {
            postCallbackDelayedInternal(1, callback, FRAME_CALLBACK_TOKEN, delayMillis);
            return;
        }
        throw new IllegalArgumentException("callback must not be null");
    }

    public void removeFrameCallback(FrameCallback callback) {
        if (callback != null) {
            removeCallbacksInternal(1, callback, FRAME_CALLBACK_TOKEN);
            return;
        }
        throw new IllegalArgumentException("callback must not be null");
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

    public long getLastFrameTimeNanos() {
        long nanoTime;
        synchronized (this.mLock) {
            nanoTime = USE_FRAME_TIME ? this.mLastFrameTimeNanos : System.nanoTime();
        }
        return nanoTime;
    }

    private void scheduleFrameLocked(long now) {
        if (!this.mFrameScheduled) {
            this.mFrameScheduled = true;
            if (!USE_VSYNC) {
                long nextFrameTime = Math.max((this.mLastFrameTimeNanos / TimeUtils.NANOS_PER_MS) + sFrameDelay, now);
                Message msg = this.mHandler.obtainMessage(0);
                msg.setAsynchronous(true);
                this.mHandler.sendMessageAtTime(msg, nextFrameTime);
            } else if (isRunningOnLooperThreadLocked()) {
                scheduleVsyncLocked();
            } else {
                Message msg2 = this.mHandler.obtainMessage(1);
                msg2.setAsynchronous(true);
                this.mHandler.sendMessageAtFrontOfQueue(msg2);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void setFPSDivisor(int divisor) {
        if (divisor <= 0) {
            divisor = 1;
        }
        this.mFPSDivisor = divisor;
        ThreadedRenderer.setFPSDivisor(divisor);
    }

    /* JADX INFO: finally extract failed */
    /* access modifiers changed from: package-private */
    public void doFrame(long frameTimeNanos, int frame) {
        long frameTimeNanos2 = frameTimeNanos;
        synchronized (this.mLock) {
            if (this.mFrameScheduled) {
                long intendedFrameTimeNanos = frameTimeNanos2;
                long startNanos = System.nanoTime();
                long jitterNanos = startNanos - frameTimeNanos2;
                this.mRealFrameTime = frameTimeNanos2;
                this.mFrameDelayTimeOnSync = jitterNanos;
                this.mLastTraversal = false;
                if (jitterNanos >= this.mFrameIntervalNanos) {
                    long skippedFrames = jitterNanos / this.mFrameIntervalNanos;
                    Jlog.animationSkipFrames(skippedFrames);
                    if (skippedFrames >= ((long) SKIPPED_FRAME_WARNING_LIMIT)) {
                        Log.i(TAG, "Skipped " + skippedFrames + " frames!  The application may be doing too much work on its main thread.");
                    }
                    frameTimeNanos2 = startNanos - (jitterNanos % this.mFrameIntervalNanos);
                }
                if (frameTimeNanos2 < this.mLastFrameTimeNanos) {
                    scheduleVsyncLocked();
                    return;
                }
                if (this.mFPSDivisor > 1) {
                    long timeSinceVsync = frameTimeNanos2 - this.mLastFrameTimeNanos;
                    long j = jitterNanos;
                    if (timeSinceVsync < this.mFrameIntervalNanos * ((long) this.mFPSDivisor) && timeSinceVsync > 0) {
                        scheduleVsyncLocked();
                        return;
                    }
                }
                this.mFrameInfo.setVsync(intendedFrameTimeNanos, frameTimeNanos2);
                this.mFrameScheduled = false;
                this.mLastFrameTimeNanos = frameTimeNanos2;
                long j2 = startNanos;
                try {
                    this.isNeedDraw = false;
                    this.mInDoframe = true;
                    Trace.traceBegin(8, "Choreographer#doFrame");
                    AnimationUtils.lockAnimationClock(frameTimeNanos2 / TimeUtils.NANOS_PER_MS);
                    this.mFrameInfo.markInputHandlingStart();
                    doCallbacks(0, frameTimeNanos2);
                    this.mFrameInfo.markAnimationsStart();
                    doCallbacks(1, frameTimeNanos2);
                    this.mFrameInfo.markPerformTraversalsStart();
                    doCallbacks(2, frameTimeNanos2);
                    doCallbacks(3, frameTimeNanos2);
                    this.mInDoframe = false;
                    this.mOldestInputTime = SubscriptionPlan.BYTES_UNLIMITED;
                    AnimationUtils.unlockAnimationClock();
                    Trace.traceEnd(8);
                    this.mLastFrameDoneTime = System.nanoTime();
                    Looper.myQueue().setLastFrameDoneTime(this.mLastFrameDoneTime / TimeUtils.NANOS_PER_MS);
                } catch (Throwable th) {
                    this.mInDoframe = false;
                    this.mOldestInputTime = SubscriptionPlan.BYTES_UNLIMITED;
                    AnimationUtils.unlockAnimationClock();
                    Trace.traceEnd(8);
                    throw th;
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:17:0x0045, code lost:
        r3 = r0;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:?, code lost:
        android.os.Trace.traceBegin(8, CALLBACK_TRACE_TITLES[r2]);
        r0 = r3;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x004f, code lost:
        if (r0 == null) goto L_0x0082;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0052, code lost:
        if (r2 != 2) goto L_0x005b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0056, code lost:
        if (r0.next != null) goto L_0x005b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x0058, code lost:
        r1.mLastTraversal = true;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x005d, code lost:
        if (IS_DEBUG_VERSION == false) goto L_0x006e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:28:0x005f, code lost:
        if (r2 != 2) goto L_0x006e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x0063, code lost:
        if (r0.action == null) goto L_0x006e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x0065, code lost:
        android.os.Trace.traceBegin(8, r0.action.toString());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x006e, code lost:
        r0.run(r10);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x0073, code lost:
        if (IS_DEBUG_VERSION == false) goto L_0x007e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x0075, code lost:
        if (r2 != 2) goto L_0x007e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:0x0079, code lost:
        if (r0.action == null) goto L_0x007e;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x007b, code lost:
        android.os.Trace.traceEnd(8);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x0080, code lost:
        r0 = r0.next;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x0082, code lost:
        r5 = r1.mLock;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x0084, code lost:
        monitor-enter(r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:?, code lost:
        r1.mCallbacksRunning = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x0087, code lost:
        r0 = r3.next;
        recycleCallbackLocked(r3);
        r3 = r0;
        r1.mCallBackCounts--;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:0x0093, code lost:
        if (r3 != null) goto L_0x0087;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:0x0095, code lost:
        monitor-exit(r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:47:0x0096, code lost:
        android.os.Trace.traceEnd(8);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:48:0x009a, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:0x009e, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:55:0x00a1, code lost:
        monitor-enter(r1.mLock);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:57:?, code lost:
        r1.mCallbacksRunning = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:58:0x00a4, code lost:
        r4 = r3.next;
        recycleCallbackLocked(r3);
        r3 = r4;
        r1.mCallBackCounts--;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:59:0x00b0, code lost:
        if (r3 != null) goto L_0x00b2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:62:0x00b4, code lost:
        android.os.Trace.traceEnd(8);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:63:0x00b7, code lost:
        throw r0;
     */
    public void doCallbacks(int callbackType, long frameTimeNanos) {
        long frameTimeNanos2;
        int i = callbackType;
        synchronized (this.mLock) {
            try {
                long now = System.nanoTime();
                CallbackRecord callbacks = this.mCallbackQueues[i].extractDueCallbacksLocked(now / TimeUtils.NANOS_PER_MS);
                if (callbacks != null) {
                    this.mCallbacksRunning = true;
                    if (i == 3) {
                        long jitterNanos = now - frameTimeNanos;
                        Trace.traceCounter(8, "jitterNanos", (int) jitterNanos);
                        if (jitterNanos >= 2 * this.mFrameIntervalNanos) {
                            frameTimeNanos2 = now - ((jitterNanos % this.mFrameIntervalNanos) + this.mFrameIntervalNanos);
                            try {
                                this.mLastFrameTimeNanos = frameTimeNanos2;
                            } catch (Throwable th) {
                                th = th;
                                throw th;
                            }
                        }
                    }
                    frameTimeNanos2 = frameTimeNanos;
                }
            } catch (Throwable th2) {
                th = th2;
                long j = frameTimeNanos;
                throw th;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void doScheduleVsync() {
        synchronized (this.mLock) {
            if (this.mFrameScheduled) {
                scheduleVsyncLocked();
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void doScheduleCallback(int callbackType) {
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
        return Looper.myLooper() == this.mLooper;
    }

    /* access modifiers changed from: private */
    public CallbackRecord obtainCallbackLocked(long dueTime, Object action, Object token) {
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

    /* access modifiers changed from: private */
    public void recycleCallbackLocked(CallbackRecord callback) {
        callback.action = null;
        callback.token = null;
        callback.next = this.mCallbackPool;
        this.mCallbackPool = callback;
    }
}
