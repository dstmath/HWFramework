package android.view;

import android.annotation.UnsupportedAppUsage;
import android.common.HwFrameworkFactory;
import android.graphics.FrameInfo;
import android.hardware.display.DisplayManagerGlobal;
import android.iawareperf.IHwRtgSchedImpl;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.Trace;
import android.util.Jlog;
import android.util.Log;
import android.util.TimeUtils;
import java.io.PrintWriter;

public final class Choreographer {
    public static final int CALLBACK_ANIMATION = 1;
    public static final int CALLBACK_COMMIT = 4;
    public static final int CALLBACK_INPUT = 0;
    public static final int CALLBACK_INSETS_ANIMATION = 2;
    private static final int CALLBACK_LAST = 4;
    private static final String[] CALLBACK_TRACE_TITLES = {"input", "animation", "insets_animation", "traversal", "commit"};
    public static final int CALLBACK_TRAVERSAL = 3;
    private static final boolean DEBUG_FRAMES = false;
    private static final boolean DEBUG_JANK = false;
    private static final long DEFAULT_FRAME_DELAY = 10;
    private static final Object FRAME_CALLBACK_TOKEN = new Object() {
        /* class android.view.Choreographer.AnonymousClass3 */

        public String toString() {
            return "FRAME_CALLBACK_TOKEN";
        }
    };
    private static final int MSG_DO_FRAME = 0;
    private static final int MSG_DO_SCHEDULE_CALLBACK = 2;
    private static final int MSG_DO_SCHEDULE_VSYNC = 1;
    private static final int SKIPPED_FRAME_WARNING_LIMIT = SystemProperties.getInt("debug.choreographer.skipwarning", 30);
    private static final String TAG = "Choreographer";
    private static final boolean USE_FRAME_TIME = SystemProperties.getBoolean("debug.choreographer.frametime", true);
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 123769497)
    private static final boolean USE_VSYNC = SystemProperties.getBoolean("debug.choreographer.vsync", true);
    private static volatile Choreographer mMainInstance;
    private static volatile long sFrameDelay = DEFAULT_FRAME_DELAY;
    private static final ThreadLocal<Choreographer> sSfThreadInstance = new ThreadLocal<Choreographer>() {
        /* class android.view.Choreographer.AnonymousClass2 */

        /* access modifiers changed from: protected */
        @Override // java.lang.ThreadLocal
        public Choreographer initialValue() {
            Looper looper = Looper.myLooper();
            if (looper != null) {
                return new Choreographer(looper, 1);
            }
            throw new IllegalStateException("The current thread must have a looper!");
        }
    };
    private static final ThreadLocal<Choreographer> sThreadInstance = new ThreadLocal<Choreographer>() {
        /* class android.view.Choreographer.AnonymousClass1 */

        /* access modifiers changed from: protected */
        @Override // java.lang.ThreadLocal
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
    public boolean isWmsAnimClass;
    private CallbackRecord mCallbackPool;
    @UnsupportedAppUsage
    private final CallbackQueue[] mCallbackQueues;
    private boolean mCallbacksRunning;
    private boolean mDebugPrintNextFrameTimeDelta;
    @UnsupportedAppUsage
    private final FrameDisplayEventReceiver mDisplayEventReceiver;
    private int mFPSDivisor;
    FrameInfo mFrameInfo;
    @UnsupportedAppUsage
    private long mFrameIntervalNanos;
    private boolean mFrameScheduled;
    private final FrameHandler mHandler;
    IHwViewRootImpl mHwViewRootImpl;
    private long mLastFrameDoneTime;
    @UnsupportedAppUsage
    private long mLastFrameTimeNanos;
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    private final Object mLock;
    private final Looper mLooper;

    public interface FrameCallback {
        void doFrame(long j);
    }

    private Choreographer(Looper looper, int vsyncSource) {
        this.mLock = new Object();
        this.mFPSDivisor = 1;
        this.mFrameInfo = new FrameInfo();
        this.mLastFrameDoneTime = 0;
        this.mHwViewRootImpl = HwFrameworkFactory.getHwViewRootImpl();
        this.isWmsAnimClass = false;
        this.mLooper = looper;
        this.mHandler = new FrameHandler(looper);
        this.mDisplayEventReceiver = USE_VSYNC ? new FrameDisplayEventReceiver(looper, vsyncSource) : null;
        this.mLastFrameTimeNanos = Long.MIN_VALUE;
        this.mFrameIntervalNanos = (long) (1.0E9f / getRefreshRate());
        this.mCallbackQueues = new CallbackQueue[5];
        for (int i = 0; i <= 4; i++) {
            this.mCallbackQueues[i] = new CallbackQueue();
        }
        setFPSDivisor(SystemProperties.getInt(ThreadedRenderer.DEBUG_FPS_DIVISOR, 1));
    }

    private static float getRefreshRate() {
        return DisplayManagerGlobal.getInstance().getDisplayInfo(0).getMode().getRefreshRate();
    }

    public static Choreographer getInstance() {
        return sThreadInstance.get();
    }

    @UnsupportedAppUsage
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
        } else if (callbackType < 0 || callbackType > 4) {
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
            this.mHwViewRootImpl.onChgCallBackCountsChanged(1);
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
        if (callbackType < 0 || callbackType > 4) {
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

    @UnsupportedAppUsage
    public long getFrameTime() {
        return getFrameTimeNanos() / TimeUtils.NANOS_PER_MS;
    }

    @UnsupportedAppUsage
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

    public long getLastFrameTimeNanos() {
        long nanoTime;
        synchronized (this.mLock) {
            nanoTime = USE_FRAME_TIME ? this.mLastFrameTimeNanos : System.nanoTime();
        }
        return nanoTime;
    }

    /* access modifiers changed from: package-private */
    public void scheduleFrameNow() {
        if (!this.mHandler.hasMessages(0)) {
            Message msg = this.mHandler.obtainMessage(0);
            msg.setAsynchronous(true);
            this.mHandler.sendMessageAtFrontOfQueue(msg);
        }
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
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x00a4, code lost:
        r5 = null;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:?, code lost:
        r19.mHwViewRootImpl.setIsNeedDraw(false);
        r19.mHwViewRootImpl.updateDoframeStatus(true);
        android.os.Trace.traceBegin(8, "Choreographer#doFrame");
        android.view.animation.AnimationUtils.lockAnimationClock(r2 / android.util.TimeUtils.NANOS_PER_MS);
        r0 = android.common.HwFrameworkFactory.getHwRtgSchedImpl();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x00c5, code lost:
        if (r0 == null) goto L_0x00ca;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x00c7, code lost:
        r0.beginDoFrame(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x00ca, code lost:
        r19.mFrameInfo.markInputHandlingStart();
        doCallbacks(0, r2);
        r19.mFrameInfo.markAnimationsStart();
        doCallbacks(1, r2);
        doCallbacks(2, r2);
        r19.mFrameInfo.markPerformTraversalsStart();
        doCallbacks(3, r2);
        doCallbacks(4, r2);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x00ec, code lost:
        r19.mHwViewRootImpl.updateDoframeStatus(false);
        android.view.animation.AnimationUtils.unlockAnimationClock();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x00f5, code lost:
        if (r0 == null) goto L_0x00fa;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:42:0x00f7, code lost:
        r0.endDoFrame(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:43:0x00fa, code lost:
        android.os.Trace.traceEnd(8);
        r19.mLastFrameDoneTime = java.lang.System.nanoTime();
        r19.mHwViewRootImpl.setLastFrameDoneTime(r19.mLastFrameDoneTime);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:44:0x010d, code lost:
        if (r19.isWmsAnimClass == false) goto L_0x0114;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:0x010f, code lost:
        android.util.Jlog.recordWmsAnimJankFrame(r2, r19.mLastFrameDoneTime);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:46:0x0114, code lost:
        android.os.Looper.myQueue().setLastFrameDoneTime(r19.mLastFrameDoneTime / android.util.TimeUtils.NANOS_PER_MS);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:47:0x011e, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:48:0x011f, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:49:0x0120, code lost:
        r19.mHwViewRootImpl.updateDoframeStatus(false);
        android.view.animation.AnimationUtils.unlockAnimationClock();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x0129, code lost:
        if (0 != 0) goto L_0x012b;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:51:0x012b, code lost:
        r5.endDoFrame(r0);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:52:0x012e, code lost:
        android.os.Trace.traceEnd(8);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:53:0x0131, code lost:
        throw r0;
     */
    @UnsupportedAppUsage
    public void doFrame(long frameTimeNanos, int frame) {
        long lastFrameOffset;
        synchronized (this.mLock) {
            try {
                if (!this.mFrameScheduled) {
                    try {
                    } catch (Throwable th) {
                        th = th;
                        throw th;
                    }
                } else {
                    long startNanos = System.nanoTime();
                    long jitterNanos = startNanos - frameTimeNanos;
                    this.mHwViewRootImpl.setRealFrameTime(frameTimeNanos);
                    this.mHwViewRootImpl.setFrameDelayTime(jitterNanos);
                    this.mHwViewRootImpl.updateLastTraversal(false);
                    if (jitterNanos >= this.mFrameIntervalNanos) {
                        long skippedFrames = jitterNanos / this.mFrameIntervalNanos;
                        Jlog.animationSkipFrames(skippedFrames);
                        if (skippedFrames >= ((long) SKIPPED_FRAME_WARNING_LIMIT)) {
                            Log.i(TAG, "Skipped " + skippedFrames + " frames!  The application may be doing too much work on its main thread.");
                        }
                        lastFrameOffset = startNanos - (jitterNanos % this.mFrameIntervalNanos);
                    } else {
                        lastFrameOffset = frameTimeNanos;
                    }
                    try {
                        if (lastFrameOffset < this.mLastFrameTimeNanos) {
                            scheduleVsyncLocked();
                            return;
                        }
                        if (this.mFPSDivisor > 1) {
                            long timeSinceVsync = lastFrameOffset - this.mLastFrameTimeNanos;
                            if (timeSinceVsync < this.mFrameIntervalNanos * ((long) this.mFPSDivisor) && timeSinceVsync > 0) {
                                scheduleVsyncLocked();
                                return;
                            }
                        }
                        this.mFrameInfo.setVsync(frameTimeNanos, lastFrameOffset);
                        this.mFrameScheduled = false;
                        this.mLastFrameTimeNanos = lastFrameOffset;
                        boolean hasDueCallbacks = this.mCallbackQueues[3].hasDueCallbacksLocked(lastFrameOffset);
                    } catch (Throwable th2) {
                        th = th2;
                        throw th;
                    }
                }
            } catch (Throwable th3) {
                th = th3;
                throw th;
            }
        }
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Code restructure failed: missing block: B:24:?, code lost:
        android.os.Trace.traceBegin(8, android.view.Choreographer.CALLBACK_TRACE_TITLES[r17]);
        r5 = r6;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x005a, code lost:
        if (r5 == null) goto L_0x006f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:27:0x005d, code lost:
        if (r17 != 3) goto L_0x0068;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:29:0x0061, code lost:
        if (r5.next != null) goto L_0x0068;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:30:0x0063, code lost:
        r16.mHwViewRootImpl.updateLastTraversal(true);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x0068, code lost:
        r5.run(r14);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:32:0x006d, code lost:
        r5 = r5.next;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:33:0x006f, code lost:
        r5 = r16.mLock;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:34:0x0071, code lost:
        monitor-enter(r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:36:?, code lost:
        r16.mCallbacksRunning = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:37:0x0074, code lost:
        r0 = r6.next;
        recycleCallbackLocked(r6);
        r6 = r0;
        r16.mHwViewRootImpl.onChgCallBackCountsChanged(-1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:38:0x007f, code lost:
        if (r6 != null) goto L_0x0074;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:39:0x0081, code lost:
        monitor-exit(r5);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:40:0x0082, code lost:
        android.os.Trace.traceEnd(8);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:41:0x0086, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:45:0x008a, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:47:0x008d, code lost:
        monitor-enter(r16.mLock);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:49:?, code lost:
        r16.mCallbacksRunning = false;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:50:0x0090, code lost:
        r4 = r6.next;
        recycleCallbackLocked(r6);
        r6 = r4;
        r16.mHwViewRootImpl.onChgCallBackCountsChanged(-1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:51:0x009b, code lost:
        if (r6 != null) goto L_0x009d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:54:0x009f, code lost:
        android.os.Trace.traceEnd(8);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:55:0x00a2, code lost:
        throw r0;
     */
    public void doCallbacks(int callbackType, long frameTimeNanos) {
        long frameTimeNanos2;
        IHwRtgSchedImpl hwRtgSchedImpl;
        synchronized (this.mLock) {
            try {
                long now = System.nanoTime();
                CallbackRecord callbacks = this.mCallbackQueues[callbackType].extractDueCallbacksLocked(now / TimeUtils.NANOS_PER_MS);
                if (callbacks != null) {
                    if (callbackType == 1 && (hwRtgSchedImpl = HwFrameworkFactory.getHwRtgSchedImpl()) != null) {
                        hwRtgSchedImpl.doAnimation();
                    }
                    this.mCallbacksRunning = true;
                    if (callbackType == 4) {
                        long jitterNanos = now - frameTimeNanos;
                        Trace.traceCounter(8, "jitterNanos", (int) jitterNanos);
                        if (jitterNanos >= this.mFrameIntervalNanos * 2) {
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

    @UnsupportedAppUsage
    private void scheduleVsyncLocked() {
        this.mDisplayEventReceiver.scheduleVsync();
    }

    private boolean isRunningOnLooperThreadLocked() {
        return Looper.myLooper() == this.mLooper;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
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

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void recycleCallbackLocked(CallbackRecord callback) {
        callback.action = null;
        callback.token = null;
        callback.next = this.mCallbackPool;
        this.mCallbackPool = callback;
    }

    /* access modifiers changed from: private */
    public final class FrameHandler extends Handler {
        public FrameHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            int i = msg.what;
            if (i == 0) {
                Choreographer.this.doFrame(System.nanoTime(), 0);
            } else if (i == 1) {
                Choreographer.this.doScheduleVsync();
            } else if (i == 2) {
                Choreographer.this.doScheduleCallback(msg.arg1);
            }
        }
    }

    /* access modifiers changed from: private */
    public final class FrameDisplayEventReceiver extends DisplayEventReceiver implements Runnable {
        private int mFrame;
        private boolean mHavePendingVsync;
        private long mTimestampNanos;

        public FrameDisplayEventReceiver(Looper looper, int vsyncSource) {
            super(looper, vsyncSource);
        }

        @Override // android.view.DisplayEventReceiver
        public void onVsync(long timestampNanos, long physicalDisplayId, int frame) {
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
            Message msg = Message.obtain(Choreographer.this.mHandler, this);
            msg.setAsynchronous(true);
            msg.setVsync(true);
            Choreographer.this.mHandler.sendMessageAtTime(msg, timestampNanos / TimeUtils.NANOS_PER_MS);
        }

        public void run() {
            this.mHavePendingVsync = false;
            Choreographer.this.doFrame(this.mTimestampNanos, this.mFrame);
        }
    }

    /* access modifiers changed from: private */
    public static final class CallbackRecord {
        public Object action;
        public long dueTime;
        public CallbackRecord next;
        public Object token;

        private CallbackRecord() {
        }

        @UnsupportedAppUsage
        public void run(long frameTimeNanos) {
            if (this.token == Choreographer.FRAME_CALLBACK_TOKEN) {
                ((FrameCallback) this.action).doFrame(frameTimeNanos);
            } else {
                ((Runnable) this.action).run();
            }
        }
    }

    /* access modifiers changed from: private */
    public final class CallbackQueue {
        private CallbackRecord mHead;

        private CallbackQueue() {
        }

        public boolean hasDueCallbacksLocked(long now) {
            CallbackRecord callbackRecord = this.mHead;
            return callbackRecord != null && callbackRecord.dueTime <= now;
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

        @UnsupportedAppUsage
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
                    Choreographer.this.mHwViewRootImpl.onChgCallBackCountsChanged(-1);
                } else {
                    predecessor = callback;
                }
                callback = next;
            }
        }
    }
}
