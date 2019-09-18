package android.animation;

import android.os.SystemClock;
import android.util.ArrayMap;
import android.view.Choreographer;
import java.util.ArrayList;

public class AnimationHandler {
    public static final ThreadLocal<AnimationHandler> sAnimatorHandler = new ThreadLocal<>();
    /* access modifiers changed from: private */
    public final ArrayList<AnimationFrameCallback> mAnimationCallbacks = new ArrayList<>();
    private final ArrayList<AnimationFrameCallback> mCommitCallbacks = new ArrayList<>();
    private final ArrayMap<AnimationFrameCallback, Long> mDelayedCallbackStartTime = new ArrayMap<>();
    private final Choreographer.FrameCallback mFrameCallback = new Choreographer.FrameCallback() {
        public void doFrame(long frameTimeNanos) {
            AnimationHandler.this.doAnimationFrame(AnimationHandler.this.getProvider().getFrameTime());
            if (AnimationHandler.this.mAnimationCallbacks.size() > 0) {
                AnimationHandler.this.getProvider().postFrameCallback(this);
            }
        }
    };
    private boolean mListDirty = false;
    private AnimationFrameCallbackProvider mProvider;

    interface AnimationFrameCallback {
        void commitAnimationFrame(long j);

        boolean doAnimationFrame(long j);
    }

    public interface AnimationFrameCallbackProvider {
        long getFrameDelay();

        long getFrameTime();

        void postCommitCallback(Runnable runnable);

        void postFrameCallback(Choreographer.FrameCallback frameCallback);

        void setFrameDelay(long j);
    }

    private class MyFrameCallbackProvider implements AnimationFrameCallbackProvider {
        final Choreographer mChoreographer;

        private MyFrameCallbackProvider() {
            this.mChoreographer = Choreographer.getInstance();
        }

        public void postFrameCallback(Choreographer.FrameCallback callback) {
            this.mChoreographer.postFrameCallback(callback);
        }

        public void postCommitCallback(Runnable runnable) {
            this.mChoreographer.postCallback(3, runnable, null);
        }

        public long getFrameTime() {
            return this.mChoreographer.getFrameTime();
        }

        public long getFrameDelay() {
            return Choreographer.getFrameDelay();
        }

        public void setFrameDelay(long delay) {
            Choreographer.setFrameDelay(delay);
        }
    }

    public static AnimationHandler getInstance() {
        if (sAnimatorHandler.get() == null) {
            sAnimatorHandler.set(new AnimationHandler());
        }
        return sAnimatorHandler.get();
    }

    public void setProvider(AnimationFrameCallbackProvider provider) {
        if (provider == null) {
            this.mProvider = new MyFrameCallbackProvider();
        } else {
            this.mProvider = provider;
        }
    }

    /* access modifiers changed from: private */
    public AnimationFrameCallbackProvider getProvider() {
        if (this.mProvider == null) {
            this.mProvider = new MyFrameCallbackProvider();
        }
        return this.mProvider;
    }

    public void addAnimationFrameCallback(AnimationFrameCallback callback, long delay) {
        if (this.mAnimationCallbacks.size() == 0) {
            getProvider().postFrameCallback(this.mFrameCallback);
        }
        if (!this.mAnimationCallbacks.contains(callback)) {
            this.mAnimationCallbacks.add(callback);
        }
        if (delay > 0) {
            this.mDelayedCallbackStartTime.put(callback, Long.valueOf(SystemClock.uptimeMillis() + delay));
        }
    }

    public void addOneShotCommitCallback(AnimationFrameCallback callback) {
        if (!this.mCommitCallbacks.contains(callback)) {
            this.mCommitCallbacks.add(callback);
        }
    }

    public void removeCallback(AnimationFrameCallback callback) {
        this.mCommitCallbacks.remove(callback);
        this.mDelayedCallbackStartTime.remove(callback);
        int id = this.mAnimationCallbacks.indexOf(callback);
        if (id >= 0) {
            this.mAnimationCallbacks.set(id, null);
            this.mListDirty = true;
        }
    }

    /* access modifiers changed from: private */
    public void doAnimationFrame(long frameTime) {
        long currentTime = SystemClock.uptimeMillis();
        int size = this.mAnimationCallbacks.size();
        for (int i = 0; i < size; i++) {
            final AnimationFrameCallback callback = this.mAnimationCallbacks.get(i);
            if (callback != null && isCallbackDue(callback, currentTime)) {
                callback.doAnimationFrame(frameTime);
                if (this.mCommitCallbacks.contains(callback)) {
                    getProvider().postCommitCallback(new Runnable() {
                        public void run() {
                            AnimationHandler.this.commitAnimationFrame(callback, AnimationHandler.this.getProvider().getFrameTime());
                        }
                    });
                }
            }
        }
        cleanUpList();
    }

    /* access modifiers changed from: private */
    public void commitAnimationFrame(AnimationFrameCallback callback, long frameTime) {
        if (!this.mDelayedCallbackStartTime.containsKey(callback) && this.mCommitCallbacks.contains(callback)) {
            callback.commitAnimationFrame(frameTime);
            this.mCommitCallbacks.remove(callback);
        }
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

    public static int getAnimationCount() {
        AnimationHandler handler = sAnimatorHandler.get();
        if (handler == null) {
            return 0;
        }
        return handler.getCallbackSize();
    }

    public static void setFrameDelay(long delay) {
        getInstance().getProvider().setFrameDelay(delay);
    }

    public static long getFrameDelay() {
        return getInstance().getProvider().getFrameDelay();
    }

    /* access modifiers changed from: package-private */
    public void autoCancelBasedOn(ObjectAnimator objectAnimator) {
        for (int i = this.mAnimationCallbacks.size() - 1; i >= 0; i--) {
            AnimationFrameCallback cb = this.mAnimationCallbacks.get(i);
            if (cb != null && objectAnimator.shouldAutoCancel(cb)) {
                ((Animator) this.mAnimationCallbacks.get(i)).cancel();
            }
        }
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

    private int getCallbackSize() {
        int count = 0;
        for (int i = this.mAnimationCallbacks.size() - 1; i >= 0; i--) {
            if (this.mAnimationCallbacks.get(i) != null) {
                count++;
            }
        }
        return count;
    }
}
