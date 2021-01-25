package huawei.android.view.dynamicanimation;

import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.ArrayMap;
import android.view.Choreographer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/* access modifiers changed from: package-private */
public class AnimationHandler {
    private static final int DEFAULT_CAPACITY = 10;
    private static final long FRAME_DELAY_MS = 10;
    public static final ThreadLocal<AnimationHandler> sAnimatorHandler = new ThreadLocal<>();
    private final List<AnimationFrameCallback> mAnimationCallbacks = new ArrayList((int) DEFAULT_CAPACITY);
    private final AnimationCallbackDispatcher mCallbackDispatcher = new AnimationCallbackDispatcher();
    private long mCurrentFrameTime = 0;
    private final Map<AnimationFrameCallback, Long> mDelayedCallbackStartTimeMap = new ArrayMap((int) DEFAULT_CAPACITY);
    private boolean mIsListDirty = false;
    private AnimationFrameCallbackProvider mProvider;

    /* access modifiers changed from: package-private */
    public interface AnimationFrameCallback {
        boolean doAnimationFrame(long j);
    }

    AnimationHandler() {
    }

    /* access modifiers changed from: package-private */
    public class AnimationCallbackDispatcher {
        AnimationCallbackDispatcher() {
        }

        /* access modifiers changed from: package-private */
        public void dispatchAnimationFrame() {
            AnimationHandler.this.mCurrentFrameTime = SystemClock.uptimeMillis();
            AnimationHandler animationHandler = AnimationHandler.this;
            animationHandler.doAnimationFrame(animationHandler.mCurrentFrameTime);
            if (AnimationHandler.this.mAnimationCallbacks.size() > 0) {
                AnimationHandler.this.getProvider().postFrameCallback();
            }
        }
    }

    public static AnimationHandler getInstance() {
        if (sAnimatorHandler.get() == null) {
            sAnimatorHandler.set(new AnimationHandler());
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
            this.mDelayedCallbackStartTimeMap.put(callback, Long.valueOf(SystemClock.uptimeMillis() + delay));
        }
    }

    public void removeCallback(AnimationFrameCallback callback) {
        this.mDelayedCallbackStartTimeMap.remove(callback);
        int index = this.mAnimationCallbacks.indexOf(callback);
        if (index >= 0) {
            this.mAnimationCallbacks.set(index, null);
            this.mIsListDirty = true;
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private void doAnimationFrame(long frameTime) {
        long currentTime = SystemClock.uptimeMillis();
        int animationCallbackSize = this.mAnimationCallbacks.size();
        for (int i = 0; i < animationCallbackSize; i++) {
            AnimationFrameCallback callback = this.mAnimationCallbacks.get(i);
            if (callback != null && isCallbackDue(callback, currentTime)) {
                callback.doAnimationFrame(frameTime);
            }
        }
        cleanUpList();
    }

    private boolean isCallbackDue(AnimationFrameCallback callback, long currentTime) {
        Long startTime = this.mDelayedCallbackStartTimeMap.get(callback);
        if (startTime == null) {
            return true;
        }
        if (startTime.longValue() >= currentTime) {
            return false;
        }
        this.mDelayedCallbackStartTimeMap.remove(callback);
        return true;
    }

    private void cleanUpList() {
        if (this.mIsListDirty) {
            for (int i = this.mAnimationCallbacks.size() - 1; i >= 0; i--) {
                if (this.mAnimationCallbacks.get(i) == null) {
                    this.mAnimationCallbacks.remove(i);
                }
            }
            this.mIsListDirty = false;
        }
    }

    /* access modifiers changed from: private */
    public static class FrameCallbackProvider16 extends AnimationFrameCallbackProvider {
        private final Choreographer mChoreographer = Choreographer.getInstance();
        private final Choreographer.FrameCallback mChoreographerCallback = new Choreographer.FrameCallback() {
            /* class huawei.android.view.dynamicanimation.AnimationHandler.FrameCallbackProvider16.AnonymousClass1 */

            @Override // android.view.Choreographer.FrameCallback
            public void doFrame(long frameTimeNanos) {
                FrameCallbackProvider16.this.mDispatcher.dispatchAnimationFrame();
            }
        };

        FrameCallbackProvider16(AnimationCallbackDispatcher dispatcher) {
            super(dispatcher);
        }

        /* access modifiers changed from: package-private */
        @Override // huawei.android.view.dynamicanimation.AnimationHandler.AnimationFrameCallbackProvider
        public void postFrameCallback() {
            this.mChoreographer.postFrameCallback(this.mChoreographerCallback);
        }
    }

    /* access modifiers changed from: private */
    public static class FrameCallbackProvider14 extends AnimationFrameCallbackProvider {
        private final Handler mHandler = new Handler(Looper.myLooper());
        private long mLastFrameTime = -1;
        private final Runnable mRunnable = new Runnable() {
            /* class huawei.android.view.dynamicanimation.AnimationHandler.FrameCallbackProvider14.AnonymousClass1 */

            @Override // java.lang.Runnable
            public void run() {
                FrameCallbackProvider14.this.mLastFrameTime = SystemClock.uptimeMillis();
                FrameCallbackProvider14.this.mDispatcher.dispatchAnimationFrame();
            }
        };

        FrameCallbackProvider14(AnimationCallbackDispatcher dispatcher) {
            super(dispatcher);
        }

        /* access modifiers changed from: package-private */
        @Override // huawei.android.view.dynamicanimation.AnimationHandler.AnimationFrameCallbackProvider
        public void postFrameCallback() {
            long delay = AnimationHandler.FRAME_DELAY_MS - (SystemClock.uptimeMillis() - this.mLastFrameTime);
            if (delay < 0) {
                delay = 0;
            }
            this.mHandler.postDelayed(this.mRunnable, delay);
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
