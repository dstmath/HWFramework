package android.view;

import android.annotation.UnsupportedAppUsage;
import android.common.HwFrameworkFactory;
import android.graphics.FrameInfo;
import android.hardware.display.DisplayManager;
import android.hardware.display.DisplayManagerGlobal;
import android.iawareperf.IHwRtgSchedImpl;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.Trace;
import android.rms.iaware.HwDynBufManager;
import android.util.Jlog;
import android.util.Log;
import android.util.TimeUtils;
import android.view.animation.AnimationUtils;
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
    private static final int MSG_DO_ADD_VSYNC = 3;
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
    static boolean sIsSingleton = true;
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
    @UnsupportedAppUsage
    private final FrameDisplayListener mFrameDisplayListener;
    FrameInfo mFrameInfo;
    @UnsupportedAppUsage
    private long mFrameIntervalNanos;
    private boolean mFrameScheduled;
    private final FrameHandler mHandler;
    IHwViewRootImpl mHwViewRootImpl;
    private boolean mIsEmptyCallback;
    private boolean mIsInAnim;
    private boolean mIsMainThread;
    private long mLastAnimTimeNanos;
    private long mLastFrameDoneTime;
    @UnsupportedAppUsage
    private long mLastFrameTimeNanos;
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    private final Object mLock;
    private final Looper mLooper;
    private Runnable postAnim;

    public interface FrameCallback {
        void doFrame(long j);
    }

    public /* synthetic */ void lambda$new$0$Choreographer() {
        long frameTimeNanos = getNextFrameTimeNanos();
        if (frameTimeNanos <= this.mLastFrameTimeNanos + this.mFrameIntervalNanos) {
            this.mIsInAnim = true;
            try {
                AnimationUtils.lockAnimationClock(frameTimeNanos / TimeUtils.NANOS_PER_MS);
                doCallbacks(1, frameTimeNanos);
            } finally {
                this.mLastAnimTimeNanos = frameTimeNanos;
                AnimationUtils.unlockAnimationClock();
                this.mIsInAnim = false;
            }
        }
    }

    private Choreographer(Looper looper, int vsyncSource) {
        boolean z = false;
        this.isWmsAnimClass = false;
        this.mHwViewRootImpl = HwFrameworkFactory.getHwViewRootImpl();
        this.mLastFrameDoneTime = 0;
        this.mLock = new Object();
        this.mFPSDivisor = 1;
        this.mIsEmptyCallback = false;
        this.mIsInAnim = false;
        this.mIsMainThread = Process.myPid() == Process.myTid();
        this.mLastAnimTimeNanos = Long.MIN_VALUE;
        this.postAnim = new Runnable() {
            /* class android.view.$$Lambda$Choreographer$zXV0PrqwmpdPajenUBozqc6c8Hs */

            @Override // java.lang.Runnable
            public final void run() {
                Choreographer.this.lambda$new$0$Choreographer();
            }
        };
        this.mFrameInfo = new FrameInfo();
        this.mLooper = looper;
        this.mHandler = new FrameHandler(looper);
        this.mDisplayEventReceiver = USE_VSYNC ? new FrameDisplayEventReceiver(looper, vsyncSource) : null;
        this.mFrameDisplayListener = USE_VSYNC ? new FrameDisplayListener() : null;
        FrameDisplayListener frameDisplayListener = this.mFrameDisplayListener;
        if (frameDisplayListener != null) {
            frameDisplayListener.register(this.mHandler);
        }
        this.mLastFrameTimeNanos = Long.MIN_VALUE;
        this.mFrameIntervalNanos = (long) (1.0E9f / getRefreshRate());
        this.mCallbackQueues = new CallbackQueue[5];
        HwDynBufManager.getImpl().initFrameInterval(this.mFrameIntervalNanos);
        if (sIsSingleton && Process.myPid() == Process.myTid()) {
            z = true;
        }
        sIsSingleton = z;
        for (int i = 0; i <= 4; i++) {
            this.mCallbackQueues[i] = new CallbackQueue();
        }
        setFPSDivisor(SystemProperties.getInt(ThreadedRenderer.DEBUG_FPS_DIVISOR, 1));
    }

    /* access modifiers changed from: private */
    public static float getRefreshRate() {
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

    /* access modifiers changed from: private */
    public class AnimAdvance implements Runnable {
        private Object act;
        private long delay;
        private Object tok;

        AnimAdvance(Object action, Object token, long delayMillis) {
            this.act = action;
            this.tok = token;
            this.delay = delayMillis;
        }

        @Override // java.lang.Runnable
        public void run() {
            Choreographer.this.postCallbackDelayedInternal(1, this.act, this.tok, this.delay);
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void postCallbackDelayedInternal(int callbackType, Object action, Object token, long delayMillis) {
        Object token2;
        Object token3;
        synchronized (this.mLock) {
            try {
                long now = SystemClock.uptimeMillis();
                long dueTime = now + delayMillis;
                if (!this.mIsInAnim || callbackType != 1) {
                    token3 = action;
                    token2 = token;
                } else {
                    dueTime = now;
                    token2 = null;
                    token3 = new AnimAdvance(action, token, delayMillis);
                }
                this.mCallbackQueues[callbackType].addCallbackLocked(dueTime, token3, token2);
                this.mHwViewRootImpl.onChgCallBackCountsChanged(1);
                if (dueTime <= now) {
                    scheduleFrameLocked(now);
                } else {
                    Message msg = this.mHandler.obtainMessage(2, token3);
                    msg.arg1 = callbackType;
                    msg.setAsynchronous(true);
                    this.mHandler.sendMessageAtTime(msg, dueTime);
                }
            } catch (Throwable th) {
                th = th;
                throw th;
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
        synchronized (this.mLock) {
            if (!this.mCallbacksRunning) {
                throw new IllegalStateException("This method must only be called as part of a callback while a frame is in progress.");
            } else if (this.mIsInAnim) {
                return getNextFrameTimeNanos();
            } else {
                return USE_FRAME_TIME ? this.mLastFrameTimeNanos : System.nanoTime();
            }
        }
    }

    public long getLastFrameTimeNanos() {
        long nanoTime;
        synchronized (this.mLock) {
            nanoTime = USE_FRAME_TIME ? this.mLastFrameTimeNanos : System.nanoTime();
        }
        return nanoTime;
    }

    public long getNextFrameTimeNanos() {
        long j;
        synchronized (this.mLock) {
            long currentTime = System.nanoTime();
            j = USE_FRAME_TIME ? ((((currentTime - this.mLastFrameTimeNanos) / this.mFrameIntervalNanos) + 1) * this.mFrameIntervalNanos) + this.mLastFrameTimeNanos : currentTime;
        }
        return j;
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
    @UnsupportedAppUsage
    public void doFrame(long frameTimeNanos, int frame) {
        Throwable th;
        long lastFrameOffset;
        boolean hasDueCallbacks;
        synchronized (this.mLock) {
            try {
                if (!this.mFrameScheduled) {
                    try {
                        return;
                    } catch (Throwable th2) {
                        th = th2;
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
                        hasDueCallbacks = this.mCallbackQueues[3].hasDueCallbacksLocked(lastFrameOffset);
                    } catch (Throwable th3) {
                        th = th3;
                        throw th;
                    }
                }
            } catch (Throwable th4) {
                th = th4;
                throw th;
            }
        }
        IHwRtgSchedImpl hwRtgSchedImpl = null;
        try {
            this.mHwViewRootImpl.setIsNeedDraw(false);
            this.mHwViewRootImpl.updateDoframeStatus(true);
            Trace.traceBegin(8, "Choreographer#doFrame");
            long animTimeNanos = lastFrameOffset;
            if (animTimeNanos < this.mLastAnimTimeNanos) {
                animTimeNanos = this.mLastAnimTimeNanos;
            }
            AnimationUtils.lockAnimationClock(animTimeNanos / TimeUtils.NANOS_PER_MS);
            IHwRtgSchedImpl hwRtgSchedImpl2 = HwFrameworkFactory.getHwRtgSchedImpl();
            if (hwRtgSchedImpl2 != null) {
                hwRtgSchedImpl2.beginDoFrame(hasDueCallbacks);
            }
            this.mFrameInfo.markInputHandlingStart();
            doCallbacks(0, lastFrameOffset);
            this.mFrameInfo.markAnimationsStart();
            doCallbacks(1, lastFrameOffset);
            doCallbacks(2, lastFrameOffset);
            this.mFrameInfo.markPerformTraversalsStart();
            doCallbacks(3, lastFrameOffset);
            doCallbacks(4, lastFrameOffset);
            this.mHwViewRootImpl.reportDrawRequestResult();
            this.mHwViewRootImpl.updateDoframeStatus(false);
            AnimationUtils.unlockAnimationClock();
            if (hwRtgSchedImpl2 != null) {
                hwRtgSchedImpl2.endDoFrame(hasDueCallbacks);
            }
            Trace.traceEnd(8);
            this.mLastFrameDoneTime = System.nanoTime();
            this.mHwViewRootImpl.setLastFrameDoneTime(this.mLastFrameDoneTime);
            if (this.isWmsAnimClass) {
                Jlog.recordWmsAnimJankFrame(lastFrameOffset, this.mLastFrameDoneTime);
            }
            Looper.myQueue().setLastFrameDoneTime(this.mLastFrameDoneTime / TimeUtils.NANOS_PER_MS);
        } catch (Throwable th5) {
            this.mHwViewRootImpl.reportDrawRequestResult();
            this.mHwViewRootImpl.updateDoframeStatus(false);
            AnimationUtils.unlockAnimationClock();
            if (0 != 0) {
                hwRtgSchedImpl.endDoFrame(hasDueCallbacks);
            }
            Trace.traceEnd(8);
            throw th5;
        }
    }

    /* access modifiers changed from: package-private */
    public void doCallbacks(int callbackType, long frameTimeNanos) {
        Throwable th;
        CallbackRecord callbacks;
        long frameTimeNanos2;
        IHwRtgSchedImpl hwRtgSchedImpl;
        synchronized (this.mLock) {
            try {
                long now = System.nanoTime();
                callbacks = this.mCallbackQueues[callbackType].extractDueCallbacksLocked(now / TimeUtils.NANOS_PER_MS);
                this.mIsEmptyCallback = callbackType == 3 ? callbacks == null : this.mIsEmptyCallback;
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
                            } catch (Throwable th2) {
                                th = th2;
                                throw th;
                            }
                        }
                    }
                    frameTimeNanos2 = frameTimeNanos;
                } else {
                    return;
                }
            } catch (Throwable th3) {
                th = th3;
                throw th;
            }
        }
        try {
            Trace.traceBegin(8, CALLBACK_TRACE_TITLES[callbackType]);
            if (callbackType == 3 && callbacks.next != null) {
                HwDynBufManager.getImpl().updateMultiViews();
            }
            for (CallbackRecord c = callbacks; c != null; c = c.next) {
                if (callbackType == 3 && c.next == null) {
                    this.mHwViewRootImpl.updateLastTraversal(true);
                }
                c.run(frameTimeNanos2);
            }
            synchronized (this.mLock) {
                this.mCallbacksRunning = false;
                do {
                    CallbackRecord next = callbacks.next;
                    recycleCallbackLocked(callbacks);
                    callbacks = next;
                    this.mHwViewRootImpl.onChgCallBackCountsChanged(-1);
                } while (callbacks != null);
            }
            Trace.traceEnd(8);
        } catch (Throwable th4) {
            synchronized (this.mLock) {
                this.mCallbacksRunning = false;
                while (true) {
                    CallbackRecord next2 = callbacks.next;
                    recycleCallbackLocked(callbacks);
                    callbacks = next2;
                    this.mHwViewRootImpl.onChgCallBackCountsChanged(-1);
                    if (callbacks == null) {
                        Trace.traceEnd(8);
                        throw th4;
                    }
                }
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
            } else if (i == 3 && !hasMessages(0) && !hasMessages(3)) {
                Choreographer.this.doFrame(System.nanoTime(), 0);
            }
        }
    }

    private final class FrameDisplayListener implements DisplayManager.DisplayListener {
        public FrameDisplayListener() {
        }

        public void register(Handler handler) {
            if (handler != null) {
                DisplayManagerGlobal.getInstance().registerDisplayListener(this, handler);
            }
        }

        @Override // android.hardware.display.DisplayManager.DisplayListener
        public void onDisplayAdded(int displayId) {
        }

        @Override // android.hardware.display.DisplayManager.DisplayListener
        public void onDisplayRemoved(int displayId) {
        }

        @Override // android.hardware.display.DisplayManager.DisplayListener
        public void onDisplayChanged(int displayId) {
            if (displayId == 0) {
                Choreographer.this.mFrameIntervalNanos = (long) (1.0E9f / Choreographer.getRefreshRate());
                HwDynBufManager.getImpl().initFrameInterval(Choreographer.this.mFrameIntervalNanos);
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
            Choreographer.this.mHandler.removeCallbacks(Choreographer.this.postAnim);
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

        private void triggerNewVsync() {
            long scheduleTimeNano = (Choreographer.this.getNextFrameTimeNanos() - ((Choreographer.this.mFrameIntervalNanos * 2) / 3)) / TimeUtils.NANOS_PER_MS;
            this.mFrame++;
            Message msg = Choreographer.this.mHandler.obtainMessage(3);
            msg.setAsynchronous(true);
            Choreographer.this.mHandler.sendMessageAtTime(msg, scheduleTimeNano);
        }

        @Override // java.lang.Runnable
        public void run() {
            boolean canPreAnim = false;
            this.mHavePendingVsync = false;
            HwDynBufManager.getImpl().onVsync();
            Choreographer.this.doFrame(this.mTimestampNanos, this.mFrame);
            if (HwDynBufManager.getImpl().canAddVsync()) {
                triggerNewVsync();
                return;
            }
            if (Choreographer.this.mIsMainThread && Choreographer.this.mHwViewRootImpl.isAnimInAdvance() && !Choreographer.this.mHwViewRootImpl.isTouchDownEvent() && !Choreographer.this.mIsEmptyCallback) {
                canPreAnim = true;
            }
            if (canPreAnim) {
                Message msg = Message.obtain(Choreographer.this.mHandler, Choreographer.this.postAnim);
                msg.setAsynchronous(true);
                Choreographer.this.mHandler.sendMessageAtTime(msg, (Choreographer.this.getNextFrameTimeNanos() - ((Choreographer.this.mFrameIntervalNanos * 2) / 3)) / TimeUtils.NANOS_PER_MS);
            }
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
                if (((action == null || callback.action == action) && (token == null || callback.token == token)) || removeAnimAdvance(callback, action, token)) {
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

        private boolean removeAnimAdvance(CallbackRecord callback, Object action, Object token) {
            if (!(callback.action instanceof AnimAdvance)) {
                return false;
            }
            AnimAdvance animAdvance = (AnimAdvance) callback.action;
            if (action != null && animAdvance.act != action) {
                return false;
            }
            if (token == null || animAdvance.tok == token) {
                return true;
            }
            return false;
        }
    }
}
