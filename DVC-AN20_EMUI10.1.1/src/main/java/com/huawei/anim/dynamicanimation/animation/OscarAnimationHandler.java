package com.huawei.anim.dynamicanimation.animation;

import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.Choreographer;
import java.util.ArrayList;
import java.util.HashMap;

/* access modifiers changed from: package-private */
public class OscarAnimationHandler {
    private static final long FRAME_DELAY_MS = 10;
    public static final ThreadLocal<OscarAnimationHandler> sAnimatorHandler = new ThreadLocal<>();
    private final ArrayList<AnimationFrameCallback> mAnimationCallbacks = new ArrayList<>();
    private final AnimationCallbackDispatcher mCallbackDispatcher = new AnimationCallbackDispatcher();
    private long mCurrentFrameTime = 0;
    private final HashMap<AnimationFrameCallback, Long> mDelayedCallbackStartTime = new HashMap<>();
    private boolean mListDirty = false;
    private AnimationFrameCallbackProvider mProvider;

    /* access modifiers changed from: package-private */
    public interface AnimationFrameCallback {
        boolean doAnimationFrame(long j);
    }

    OscarAnimationHandler() {
    }

    /* access modifiers changed from: package-private */
    public class AnimationCallbackDispatcher {
        AnimationCallbackDispatcher() {
        }

        /* access modifiers changed from: package-private */
        public void dispatchAnimationFrame() {
            OscarAnimationHandler.this.mCurrentFrameTime = SystemClock.uptimeMillis();
            OscarAnimationHandler oscarAnimationHandler = OscarAnimationHandler.this;
            oscarAnimationHandler.doAnimationFrame(oscarAnimationHandler.mCurrentFrameTime);
            if (OscarAnimationHandler.this.mAnimationCallbacks.size() > 0) {
                OscarAnimationHandler.this.getProvider().postFrameCallback();
            }
        }
    }

    public static OscarAnimationHandler getInstance() {
        if (sAnimatorHandler.get() == null) {
            sAnimatorHandler.set(new OscarAnimationHandler());
        }
        return sAnimatorHandler.get();
    }

    public static long getFrameTime() {
        if (sAnimatorHandler.get() == null) {
            return 0;
        }
        return sAnimatorHandler.get().mCurrentFrameTime;
    }

    public void setProvider(AnimationFrameCallbackProvider provider) {
        this.mProvider = provider;
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private AnimationFrameCallbackProvider getProvider() {
        if (this.mProvider == null) {
            if (Build.VERSION.SDK_INT >= 16) {
                this.mProvider = new FrameCallbackProvider16(this.mCallbackDispatcher);
            } else {
                this.mProvider = new FrameCallbackProvider14(this.mCallbackDispatcher);
            }
        }
        return this.mProvider;
    }

    public void addAnimationFrameCallback(AnimationFrameCallback callback, long delay) {
        if (this.mAnimationCallbacks.size() == 0) {
            getProvider().postFrameCallback();
        }
        if (!this.mAnimationCallbacks.contains(callback)) {
            this.mAnimationCallbacks.add(callback);
        }
        if (delay > 0) {
            this.mDelayedCallbackStartTime.put(callback, Long.valueOf(SystemClock.uptimeMillis() + delay));
        }
    }

    public void removeCallback(AnimationFrameCallback callback) {
        this.mDelayedCallbackStartTime.remove(callback);
        int id = this.mAnimationCallbacks.indexOf(callback);
        if (id >= 0) {
            this.mAnimationCallbacks.set(id, null);
            this.mListDirty = true;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void doAnimationFrame(long frameTime) {
        long currentTime = SystemClock.uptimeMillis();
        for (int i = 0; i < this.mAnimationCallbacks.size(); i++) {
            AnimationFrameCallback callback = this.mAnimationCallbacks.get(i);
            if (callback != null && isCallbackDue(callback, currentTime)) {
                callback.doAnimationFrame(frameTime);
            }
        }
        cleanUpList();
    }

    private boolean isCallbackDue(AnimationFrameCallback callback, long currentTime) {
        Long startTime = this.mDelayedCallbackStartTime.get(callback);
        if (startTime == null) {
            return true;
        }
        if (startTime.longValue() >= currentTime) {
            return false;
        }
        this.mDelayedCallbackStartTime.remove(callback);
        return true;
    }

    private void cleanUpList() {
        if (this.mListDirty) {
            for (int i = this.mAnimationCallbacks.size() - 1; i >= 0; i--) {
                if (this.mAnimationCallbacks.get(i) == null) {
                    this.mAnimationCallbacks.remove(i);
                }
            }
            this.mListDirty = false;
        }
    }

    /* access modifiers changed from: private */
    public static class FrameCallbackProvider16 extends AnimationFrameCallbackProvider {
        private final Choreographer mChoreographer = Choreographer.getInstance();
        private final Choreographer.FrameCallback mChoreographerCallback = new Choreographer.FrameCallback() {
            /* class com.huawei.anim.dynamicanimation.animation.OscarAnimationHandler.FrameCallbackProvider16.AnonymousClass1 */

            public void doFrame(long frameTimeNanos) {
                FrameCallbackProvider16.this.mDispatcher.dispatchAnimationFrame();
            }
        };

        FrameCallbackProvider16(AnimationCallbackDispatcher dispatcher) {
            super(dispatcher);
        }

        /* access modifiers changed from: package-private */
        @Override // com.huawei.anim.dynamicanimation.animation.OscarAnimationHandler.AnimationFrameCallbackProvider
        public void postFrameCallback() {
            this.mChoreographer.postFrameCallback(this.mChoreographerCallback);
        }
    }

    /* access modifiers changed from: private */
    public static class FrameCallbackProvider14 extends AnimationFrameCallbackProvider {
        private final Handler mHandler = new Handler(Looper.myLooper());
        private long mLastFrameTime = -1;
        private final Runnable mRunnable = new Runnable() {
            /* class com.huawei.anim.dynamicanimation.animation.OscarAnimationHandler.FrameCallbackProvider14.AnonymousClass1 */

            public void run() {
                FrameCallbackProvider14.this.mLastFrameTime = SystemClock.uptimeMillis();
                FrameCallbackProvider14.this.mDispatcher.dispatchAnimationFrame();
            }
        };

        FrameCallbackProvider14(AnimationCallbackDispatcher dispatcher) {
            super(dispatcher);
        }

        /* access modifiers changed from: package-private */
        @Override // com.huawei.anim.dynamicanimation.animation.OscarAnimationHandler.AnimationFrameCallbackProvider
        public void postFrameCallback() {
            this.mHandler.postDelayed(this.mRunnable, Math.max(OscarAnimationHandler.FRAME_DELAY_MS - (SystemClock.uptimeMillis() - this.mLastFrameTime), 0L));
        }
    }

    /* access modifiers changed from: package-private */
    public static abstract class AnimationFrameCallbackProvider {
        final AnimationCallbackDispatcher mDispatcher;

        /* access modifiers changed from: package-private */
        public abstract void postFrameCallback();

        AnimationFrameCallbackProvider(AnimationCallbackDispatcher dispatcher) {
            this.mDispatcher = dispatcher;
        }
    }
}
