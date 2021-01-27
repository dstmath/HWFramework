package android.animation;

import android.animation.AnimationHandler;
import android.animation.Animator;
import android.annotation.UnsupportedAppUsage;
import android.os.Looper;
import android.os.Trace;
import android.util.AndroidRuntimeException;
import android.util.Log;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class ValueAnimator extends Animator implements AnimationHandler.AnimationFrameCallback {
    private static final boolean DEBUG = false;
    public static final int INFINITE = -1;
    public static final int RESTART = 1;
    public static final int REVERSE = 2;
    private static final String TAG = "ValueAnimator";
    private static final TimeInterpolator sDefaultInterpolator = new AccelerateDecelerateInterpolator();
    @UnsupportedAppUsage(maxTargetSdk = 28)
    private static float sDurationScale = 1.0f;
    private boolean mAnimationEndRequested = false;
    private float mCurrentFraction = 0.0f;
    @UnsupportedAppUsage
    private long mDuration = 300;
    private float mDurationScale = -1.0f;
    private long mFirstFrameTime = -1;
    boolean mInitialized = false;
    private TimeInterpolator mInterpolator = sDefaultInterpolator;
    private long mLastFrameTime = -1;
    private float mOverallFraction = 0.0f;
    private long mPauseTime;
    private int mRepeatCount = 0;
    private int mRepeatMode = 1;
    private boolean mResumed = false;
    private boolean mReversing;
    private boolean mRunning = false;
    float mSeekFraction = -1.0f;
    private boolean mSelfPulse = true;
    private long mStartDelay = 0;
    private boolean mStartListenersCalled = false;
    long mStartTime = -1;
    boolean mStartTimeCommitted;
    private boolean mStarted = false;
    private boolean mSuppressSelfPulseRequested = false;
    ArrayList<AnimatorUpdateListener> mUpdateListeners = null;
    PropertyValuesHolder[] mValues;
    HashMap<String, PropertyValuesHolder> mValuesMap;

    public interface AnimatorUpdateListener {
        void onAnimationUpdate(ValueAnimator valueAnimator);
    }

    @Retention(RetentionPolicy.SOURCE)
    public @interface RepeatMode {
    }

    public static void setDurationScale(float durationScale) {
        sDurationScale = durationScale;
    }

    public static float getDurationScale() {
        return sDurationScale;
    }

    public static boolean areAnimatorsEnabled() {
        return sDurationScale != 0.0f;
    }

    public static ValueAnimator ofInt(int... values) {
        ValueAnimator anim = new ValueAnimator();
        anim.setIntValues(values);
        return anim;
    }

    public static ValueAnimator ofArgb(int... values) {
        ValueAnimator anim = new ValueAnimator();
        anim.setIntValues(values);
        anim.setEvaluator(ArgbEvaluator.getInstance());
        return anim;
    }

    public static ValueAnimator ofFloat(float... values) {
        ValueAnimator anim = new ValueAnimator();
        anim.setFloatValues(values);
        return anim;
    }

    public static ValueAnimator ofPropertyValuesHolder(PropertyValuesHolder... values) {
        ValueAnimator anim = new ValueAnimator();
        anim.setValues(values);
        return anim;
    }

    public static ValueAnimator ofObject(TypeEvaluator evaluator, Object... values) {
        ValueAnimator anim = new ValueAnimator();
        anim.setObjectValues(values);
        anim.setEvaluator(evaluator);
        return anim;
    }

    public void setIntValues(int... values) {
        if (values != null && values.length != 0) {
            PropertyValuesHolder[] propertyValuesHolderArr = this.mValues;
            if (propertyValuesHolderArr == null || propertyValuesHolderArr.length == 0) {
                setValues(PropertyValuesHolder.ofInt("", values));
            } else {
                propertyValuesHolderArr[0].setIntValues(values);
            }
            this.mInitialized = false;
        }
    }

    public void setFloatValues(float... values) {
        if (values != null && values.length != 0) {
            PropertyValuesHolder[] propertyValuesHolderArr = this.mValues;
            if (propertyValuesHolderArr == null || propertyValuesHolderArr.length == 0) {
                setValues(PropertyValuesHolder.ofFloat("", values));
            } else {
                propertyValuesHolderArr[0].setFloatValues(values);
            }
            this.mInitialized = false;
        }
    }

    public void setObjectValues(Object... values) {
        if (values != null && values.length != 0) {
            PropertyValuesHolder[] propertyValuesHolderArr = this.mValues;
            if (propertyValuesHolderArr == null || propertyValuesHolderArr.length == 0) {
                setValues(PropertyValuesHolder.ofObject("", (TypeEvaluator) null, values));
            } else {
                propertyValuesHolderArr[0].setObjectValues(values);
            }
            this.mInitialized = false;
        }
    }

    public void setValues(PropertyValuesHolder... values) {
        int numValues = values.length;
        this.mValues = values;
        this.mValuesMap = new HashMap<>(numValues);
        for (PropertyValuesHolder valuesHolder : values) {
            this.mValuesMap.put(valuesHolder.getPropertyName(), valuesHolder);
        }
        this.mInitialized = false;
    }

    public PropertyValuesHolder[] getValues() {
        return this.mValues;
    }

    /* access modifiers changed from: package-private */
    public void initAnimation() {
        if (!this.mInitialized) {
            int numValues = this.mValues.length;
            for (int i = 0; i < numValues; i++) {
                this.mValues[i].init();
            }
            this.mInitialized = true;
        }
    }

    @Override // android.animation.Animator
    public ValueAnimator setDuration(long duration) {
        if (duration >= 0) {
            this.mDuration = duration;
            return this;
        }
        throw new IllegalArgumentException("Animators cannot have negative duration: " + duration);
    }

    public void overrideDurationScale(float durationScale) {
        this.mDurationScale = durationScale;
    }

    private float resolveDurationScale() {
        float f = this.mDurationScale;
        return f >= 0.0f ? f : sDurationScale;
    }

    private long getScaledDuration() {
        return (long) (((float) this.mDuration) * resolveDurationScale());
    }

    @Override // android.animation.Animator
    public long getDuration() {
        return this.mDuration;
    }

    @Override // android.animation.Animator
    public long getTotalDuration() {
        int i = this.mRepeatCount;
        if (i == -1) {
            return -1;
        }
        return this.mStartDelay + (this.mDuration * ((long) (i + 1)));
    }

    public void setCurrentPlayTime(long playTime) {
        long j = this.mDuration;
        setCurrentFraction(j > 0 ? ((float) playTime) / ((float) j) : 1.0f);
    }

    public void setCurrentFraction(float fraction) {
        initAnimation();
        float fraction2 = clampFraction(fraction);
        this.mStartTimeCommitted = true;
        if (isPulsingInternal()) {
            this.mStartTime = AnimationUtils.currentAnimationTimeMillis() - ((long) (((float) getScaledDuration()) * fraction2));
        } else {
            this.mSeekFraction = fraction2;
        }
        this.mOverallFraction = fraction2;
        animateValue(getCurrentIterationFraction(fraction2, this.mReversing));
    }

    private int getCurrentIteration(float fraction) {
        float fraction2 = clampFraction(fraction);
        double iteration = Math.floor((double) fraction2);
        if (((double) fraction2) == iteration && fraction2 > 0.0f) {
            iteration -= 1.0d;
        }
        return (int) iteration;
    }

    private float getCurrentIterationFraction(float fraction, boolean inReverse) {
        float fraction2 = clampFraction(fraction);
        int iteration = getCurrentIteration(fraction2);
        float currentFraction = fraction2 - ((float) iteration);
        return shouldPlayBackward(iteration, inReverse) ? 1.0f - currentFraction : currentFraction;
    }

    private float clampFraction(float fraction) {
        if (fraction < 0.0f) {
            return 0.0f;
        }
        int i = this.mRepeatCount;
        if (i != -1) {
            return Math.min(fraction, (float) (i + 1));
        }
        return fraction;
    }

    private boolean shouldPlayBackward(int iteration, boolean inReverse) {
        if (iteration > 0 && this.mRepeatMode == 2) {
            int i = this.mRepeatCount;
            if (iteration < i + 1 || i == -1) {
                return inReverse ? iteration % 2 == 0 : iteration % 2 != 0;
            }
        }
        return inReverse;
    }

    public long getCurrentPlayTime() {
        if (!this.mInitialized) {
            return 0;
        }
        if (!this.mStarted && this.mSeekFraction < 0.0f) {
            return 0;
        }
        float f = this.mSeekFraction;
        if (f >= 0.0f) {
            return (long) (((float) this.mDuration) * f);
        }
        float durationScale = resolveDurationScale();
        if (durationScale == 0.0f) {
            durationScale = 1.0f;
        }
        return (long) (((float) (AnimationUtils.currentAnimationTimeMillis() - this.mStartTime)) / durationScale);
    }

    @Override // android.animation.Animator
    public long getStartDelay() {
        return this.mStartDelay;
    }

    @Override // android.animation.Animator
    public void setStartDelay(long startDelay) {
        if (startDelay < 0) {
            Log.w(TAG, "Start delay should always be non-negative");
            startDelay = 0;
        }
        this.mStartDelay = startDelay;
    }

    public static long getFrameDelay() {
        AnimationHandler.getInstance();
        return AnimationHandler.getFrameDelay();
    }

    public static void setFrameDelay(long frameDelay) {
        AnimationHandler.getInstance();
        AnimationHandler.setFrameDelay(frameDelay);
    }

    public Object getAnimatedValue() {
        PropertyValuesHolder[] propertyValuesHolderArr = this.mValues;
        if (propertyValuesHolderArr == null || propertyValuesHolderArr.length <= 0) {
            return null;
        }
        return propertyValuesHolderArr[0].getAnimatedValue();
    }

    public Object getAnimatedValue(String propertyName) {
        PropertyValuesHolder valuesHolder = this.mValuesMap.get(propertyName);
        if (valuesHolder != null) {
            return valuesHolder.getAnimatedValue();
        }
        return null;
    }

    public void setRepeatCount(int value) {
        this.mRepeatCount = value;
    }

    public int getRepeatCount() {
        return this.mRepeatCount;
    }

    public void setRepeatMode(int value) {
        this.mRepeatMode = value;
    }

    public int getRepeatMode() {
        return this.mRepeatMode;
    }

    public void addUpdateListener(AnimatorUpdateListener listener) {
        if (this.mUpdateListeners == null) {
            this.mUpdateListeners = new ArrayList<>();
        }
        this.mUpdateListeners.add(listener);
    }

    public void removeAllUpdateListeners() {
        ArrayList<AnimatorUpdateListener> arrayList = this.mUpdateListeners;
        if (arrayList != null) {
            arrayList.clear();
            this.mUpdateListeners = null;
        }
    }

    public void removeUpdateListener(AnimatorUpdateListener listener) {
        ArrayList<AnimatorUpdateListener> arrayList = this.mUpdateListeners;
        if (arrayList != null) {
            arrayList.remove(listener);
            if (this.mUpdateListeners.size() == 0) {
                this.mUpdateListeners = null;
            }
        }
    }

    @Override // android.animation.Animator
    public void setInterpolator(TimeInterpolator value) {
        if (value != null) {
            this.mInterpolator = value;
        } else {
            this.mInterpolator = new LinearInterpolator();
        }
    }

    @Override // android.animation.Animator
    public TimeInterpolator getInterpolator() {
        return this.mInterpolator;
    }

    public void setEvaluator(TypeEvaluator value) {
        PropertyValuesHolder[] propertyValuesHolderArr;
        if (value != null && (propertyValuesHolderArr = this.mValues) != null && propertyValuesHolderArr.length > 0) {
            propertyValuesHolderArr[0].setEvaluator(value);
        }
    }

    private void notifyStartListeners() {
        if (this.mListeners != null && !this.mStartListenersCalled) {
            ArrayList<Animator.AnimatorListener> tmpListeners = (ArrayList) this.mListeners.clone();
            int numListeners = tmpListeners.size();
            for (int i = 0; i < numListeners; i++) {
                tmpListeners.get(i).onAnimationStart(this, this.mReversing);
            }
        }
        this.mStartListenersCalled = true;
    }

    private void start(boolean playBackwards) {
        if (Looper.myLooper() != null) {
            this.mReversing = playBackwards;
            this.mSelfPulse = !this.mSuppressSelfPulseRequested;
            if (playBackwards) {
                float f = this.mSeekFraction;
                if (!(f == -1.0f || f == 0.0f)) {
                    int i = this.mRepeatCount;
                    if (i == -1) {
                        this.mSeekFraction = 1.0f - ((float) (((double) f) - Math.floor((double) f)));
                    } else {
                        this.mSeekFraction = ((float) (i + 1)) - f;
                    }
                }
            }
            this.mStarted = true;
            this.mPaused = false;
            this.mRunning = false;
            this.mAnimationEndRequested = false;
            this.mLastFrameTime = -1;
            this.mFirstFrameTime = -1;
            this.mStartTime = -1;
            addAnimationCallback(0);
            if (this.mStartDelay == 0 || this.mSeekFraction >= 0.0f || this.mReversing) {
                startAnimation();
                float f2 = this.mSeekFraction;
                if (f2 == -1.0f) {
                    setCurrentPlayTime(0);
                } else {
                    setCurrentFraction(f2);
                }
            }
        } else {
            throw new AndroidRuntimeException("Animators may only be run on Looper threads");
        }
    }

    /* access modifiers changed from: package-private */
    @Override // android.animation.Animator
    public void startWithoutPulsing(boolean inReverse) {
        this.mSuppressSelfPulseRequested = true;
        if (inReverse) {
            reverse();
        } else {
            start();
        }
        this.mSuppressSelfPulseRequested = false;
    }

    @Override // android.animation.Animator
    public void start() {
        start(false);
    }

    @Override // android.animation.Animator
    public void cancel() {
        if (Looper.myLooper() == null) {
            throw new AndroidRuntimeException("Animators may only be run on Looper threads");
        } else if (!this.mAnimationEndRequested) {
            if ((this.mStarted || this.mRunning) && this.mListeners != null) {
                if (!this.mRunning) {
                    notifyStartListeners();
                }
                Iterator<Animator.AnimatorListener> it = ((ArrayList) this.mListeners.clone()).iterator();
                while (it.hasNext()) {
                    it.next().onAnimationCancel(this);
                }
            }
            endAnimation();
        }
    }

    @Override // android.animation.Animator
    public void end() {
        if (Looper.myLooper() != null) {
            if (!this.mRunning) {
                startAnimation();
                this.mStarted = true;
            } else if (!this.mInitialized) {
                initAnimation();
            }
            animateValue(shouldPlayBackward(this.mRepeatCount, this.mReversing) ? 0.0f : 1.0f);
            endAnimation();
            return;
        }
        throw new AndroidRuntimeException("Animators may only be run on Looper threads");
    }

    @Override // android.animation.Animator
    public void resume() {
        if (Looper.myLooper() != null) {
            if (this.mPaused && !this.mResumed) {
                this.mResumed = true;
                if (this.mPauseTime > 0) {
                    addAnimationCallback(0);
                }
            }
            super.resume();
            return;
        }
        throw new AndroidRuntimeException("Animators may only be resumed from the same thread that the animator was started on");
    }

    @Override // android.animation.Animator
    public void pause() {
        boolean previouslyPaused = this.mPaused;
        super.pause();
        if (!previouslyPaused && this.mPaused) {
            this.mPauseTime = -1;
            this.mResumed = false;
        }
    }

    @Override // android.animation.Animator
    public boolean isRunning() {
        return this.mRunning;
    }

    @Override // android.animation.Animator
    public boolean isStarted() {
        return this.mStarted;
    }

    @Override // android.animation.Animator
    public void reverse() {
        if (isPulsingInternal()) {
            long currentTime = AnimationUtils.currentAnimationTimeMillis();
            this.mStartTime = currentTime - (getScaledDuration() - (currentTime - this.mStartTime));
            this.mStartTimeCommitted = true;
            this.mReversing = !this.mReversing;
        } else if (this.mStarted) {
            this.mReversing = !this.mReversing;
            end();
        } else {
            start(true);
        }
    }

    @Override // android.animation.Animator
    public boolean canReverse() {
        return true;
    }

    private void endAnimation() {
        if (!this.mAnimationEndRequested) {
            removeAnimationCallback();
            boolean notify = true;
            this.mAnimationEndRequested = true;
            this.mPaused = false;
            if ((!this.mStarted && !this.mRunning) || this.mListeners == null) {
                notify = false;
            }
            if (notify && !this.mRunning) {
                notifyStartListeners();
            }
            this.mRunning = false;
            this.mStarted = false;
            this.mStartListenersCalled = false;
            this.mLastFrameTime = -1;
            this.mFirstFrameTime = -1;
            this.mStartTime = -1;
            if (notify && this.mListeners != null) {
                ArrayList<Animator.AnimatorListener> tmpListeners = (ArrayList) this.mListeners.clone();
                int numListeners = tmpListeners.size();
                for (int i = 0; i < numListeners; i++) {
                    tmpListeners.get(i).onAnimationEnd(this, this.mReversing);
                }
            }
            this.mReversing = false;
            if (Trace.isTagEnabled(8)) {
                Trace.asyncTraceEnd(8, getNameForTrace(), System.identityHashCode(this));
            }
        }
    }

    private void startAnimation() {
        if (Trace.isTagEnabled(8)) {
            Trace.asyncTraceBegin(8, getNameForTrace(), System.identityHashCode(this));
        }
        this.mAnimationEndRequested = false;
        initAnimation();
        this.mRunning = true;
        float f = this.mSeekFraction;
        if (f >= 0.0f) {
            this.mOverallFraction = f;
        } else {
            this.mOverallFraction = 0.0f;
        }
        if (this.mListeners != null) {
            notifyStartListeners();
        }
    }

    private boolean isPulsingInternal() {
        return this.mLastFrameTime >= 0;
    }

    /* access modifiers changed from: package-private */
    public String getNameForTrace() {
        return "animator";
    }

    @Override // android.animation.AnimationHandler.AnimationFrameCallback
    public void commitAnimationFrame(long frameTime) {
        if (!this.mStartTimeCommitted) {
            this.mStartTimeCommitted = true;
            long adjustment = frameTime - this.mLastFrameTime;
            if (adjustment > 0) {
                this.mStartTime += adjustment;
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean animateBasedOnTime(long currentTime) {
        boolean done = false;
        if (this.mRunning) {
            long scaledDuration = getScaledDuration();
            float fraction = scaledDuration > 0 ? ((float) (currentTime - this.mStartTime)) / ((float) scaledDuration) : 1.0f;
            boolean lastIterationFinished = false;
            boolean newIteration = ((int) fraction) > ((int) this.mOverallFraction);
            int i = this.mRepeatCount;
            if (fraction >= ((float) (i + 1)) && i != -1) {
                lastIterationFinished = true;
            }
            if (scaledDuration == 0) {
                done = true;
            } else if (!newIteration || lastIterationFinished) {
                if (lastIterationFinished) {
                    done = true;
                }
            } else if (this.mListeners != null) {
                int numListeners = this.mListeners.size();
                for (int i2 = 0; i2 < numListeners; i2++) {
                    ((Animator.AnimatorListener) this.mListeners.get(i2)).onAnimationRepeat(this);
                }
            }
            this.mOverallFraction = clampFraction(fraction);
            animateValue(getCurrentIterationFraction(this.mOverallFraction, this.mReversing));
        }
        return done;
    }

    /* access modifiers changed from: package-private */
    @Override // android.animation.Animator
    public void animateBasedOnPlayTime(long currentPlayTime, long lastPlayTime, boolean inReverse) {
        if (currentPlayTime < 0 || lastPlayTime < 0) {
            throw new UnsupportedOperationException("Error: Play time should never be negative.");
        }
        initAnimation();
        int i = this.mRepeatCount;
        if (i > 0) {
            long j = this.mDuration;
            if (!(Math.min((int) (currentPlayTime / j), i) == Math.min((int) (lastPlayTime / j), this.mRepeatCount) || this.mListeners == null)) {
                int numListeners = this.mListeners.size();
                for (int i2 = 0; i2 < numListeners; i2++) {
                    ((Animator.AnimatorListener) this.mListeners.get(i2)).onAnimationRepeat(this);
                }
            }
        }
        int iteration = this.mRepeatCount;
        if (iteration == -1 || currentPlayTime < ((long) (iteration + 1)) * this.mDuration) {
            animateValue(getCurrentIterationFraction(((float) currentPlayTime) / ((float) this.mDuration), inReverse));
        } else {
            skipToEndValue(inReverse);
        }
    }

    /* access modifiers changed from: package-private */
    @Override // android.animation.Animator
    public void skipToEndValue(boolean inReverse) {
        initAnimation();
        float endFraction = inReverse ? 0.0f : 1.0f;
        if (this.mRepeatCount % 2 == 1 && this.mRepeatMode == 2) {
            endFraction = 0.0f;
        }
        animateValue(endFraction);
    }

    /* access modifiers changed from: package-private */
    @Override // android.animation.Animator
    public boolean isInitialized() {
        return this.mInitialized;
    }

    @Override // android.animation.AnimationHandler.AnimationFrameCallback
    public final boolean doAnimationFrame(long frameTime) {
        long j;
        if (this.mStartTime < 0) {
            if (this.mReversing) {
                j = frameTime;
            } else {
                j = ((long) (((float) this.mStartDelay) * resolveDurationScale())) + frameTime;
            }
            this.mStartTime = j;
        }
        if (this.mPaused) {
            this.mPauseTime = frameTime;
            removeAnimationCallback();
            return false;
        }
        if (this.mResumed) {
            this.mResumed = false;
            long j2 = this.mPauseTime;
            if (j2 > 0) {
                this.mStartTime += frameTime - j2;
            }
        }
        if (!this.mRunning) {
            if (this.mStartTime > frameTime && this.mSeekFraction == -1.0f) {
                return false;
            }
            this.mRunning = true;
            startAnimation();
        }
        if (this.mLastFrameTime < 0) {
            if (this.mSeekFraction >= 0.0f) {
                this.mStartTime = frameTime - ((long) (((float) getScaledDuration()) * this.mSeekFraction));
                this.mSeekFraction = -1.0f;
            }
            this.mStartTimeCommitted = false;
        }
        this.mLastFrameTime = frameTime;
        boolean finished = animateBasedOnTime(Math.max(frameTime, this.mStartTime));
        if (finished) {
            endAnimation();
        }
        return finished;
    }

    /* access modifiers changed from: package-private */
    @Override // android.animation.Animator
    public boolean pulseAnimationFrame(long frameTime) {
        if (this.mSelfPulse) {
            return false;
        }
        return doAnimationFrame(frameTime);
    }

    private void addOneShotCommitCallback() {
        if (this.mSelfPulse) {
            getAnimationHandler().addOneShotCommitCallback(this);
        }
    }

    private void removeAnimationCallback() {
        if (this.mSelfPulse) {
            getAnimationHandler().removeCallback(this);
        }
    }

    private void addAnimationCallback(long delay) {
        if (this.mSelfPulse) {
            getAnimationHandler().addAnimationFrameCallback(this, delay);
        }
    }

    public float getAnimatedFraction() {
        return this.mCurrentFraction;
    }

    /* access modifiers changed from: package-private */
    @UnsupportedAppUsage
    public void animateValue(float fraction) {
        float fraction2 = this.mInterpolator.getInterpolation(fraction);
        this.mCurrentFraction = fraction2;
        int numValues = this.mValues.length;
        for (int i = 0; i < numValues; i++) {
            this.mValues[i].calculateValue(fraction2);
        }
        ArrayList<AnimatorUpdateListener> arrayList = this.mUpdateListeners;
        if (arrayList != null) {
            int numListeners = arrayList.size();
            for (int i2 = 0; i2 < numListeners; i2++) {
                this.mUpdateListeners.get(i2).onAnimationUpdate(this);
            }
        }
    }

    @Override // android.animation.Animator, java.lang.Object
    public ValueAnimator clone() {
        ValueAnimator anim = (ValueAnimator) super.clone();
        ArrayList<AnimatorUpdateListener> arrayList = this.mUpdateListeners;
        if (arrayList != null) {
            anim.mUpdateListeners = new ArrayList<>(arrayList);
        }
        anim.mSeekFraction = -1.0f;
        anim.mReversing = false;
        anim.mInitialized = false;
        anim.mStarted = false;
        anim.mRunning = false;
        anim.mPaused = false;
        anim.mResumed = false;
        anim.mStartListenersCalled = false;
        anim.mStartTime = -1;
        anim.mStartTimeCommitted = false;
        anim.mAnimationEndRequested = false;
        anim.mPauseTime = -1;
        anim.mLastFrameTime = -1;
        anim.mFirstFrameTime = -1;
        anim.mOverallFraction = 0.0f;
        anim.mCurrentFraction = 0.0f;
        anim.mSelfPulse = true;
        anim.mSuppressSelfPulseRequested = false;
        PropertyValuesHolder[] oldValues = this.mValues;
        if (oldValues != null) {
            int numValues = oldValues.length;
            anim.mValues = new PropertyValuesHolder[numValues];
            anim.mValuesMap = new HashMap<>(numValues);
            for (int i = 0; i < numValues; i++) {
                PropertyValuesHolder newValuesHolder = oldValues[i].clone();
                anim.mValues[i] = newValuesHolder;
                anim.mValuesMap.put(newValuesHolder.getPropertyName(), newValuesHolder);
            }
        }
        return anim;
    }

    public static int getCurrentAnimationsCount() {
        return AnimationHandler.getAnimationCount();
    }

    @Override // java.lang.Object
    public String toString() {
        String returnVal = "ValueAnimator@" + Integer.toHexString(hashCode());
        if (this.mValues != null) {
            for (int i = 0; i < this.mValues.length; i++) {
                returnVal = returnVal + "\n    " + this.mValues[i].toString();
            }
        }
        return returnVal;
    }

    @Override // android.animation.Animator
    public void setAllowRunningAsynchronously(boolean mayRunAsync) {
    }

    public AnimationHandler getAnimationHandler() {
        return AnimationHandler.getInstance();
    }
}
