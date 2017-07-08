package android.animation;

import android.animation.Animator.AnimatorListener;
import android.net.ProxyInfo;
import android.os.Looper;
import android.os.Trace;
import android.speech.tts.TextToSpeech.Engine;
import android.util.AndroidRuntimeException;
import android.util.Log;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import java.util.ArrayList;
import java.util.HashMap;

public class ValueAnimator extends Animator implements AnimationFrameCallback {
    private static final boolean DEBUG = false;
    public static final int INFINITE = -1;
    public static final int RESTART = 1;
    public static final int REVERSE = 2;
    private static final String TAG = "ValueAnimator";
    private static final TimeInterpolator sDefaultInterpolator = null;
    private static float sDurationScale;
    private boolean mAnimationEndRequested;
    private float mCurrentFraction;
    private long mDuration;
    boolean mInitialized;
    private TimeInterpolator mInterpolator;
    private long mLastFrameTime;
    private float mOverallFraction;
    private long mPauseTime;
    private int mRepeatCount;
    private int mRepeatMode;
    private boolean mResumed;
    private boolean mReversing;
    private boolean mRunning;
    float mSeekFraction;
    private long mStartDelay;
    private boolean mStartListenersCalled;
    long mStartTime;
    boolean mStartTimeCommitted;
    private boolean mStarted;
    ArrayList<AnimatorUpdateListener> mUpdateListeners;
    PropertyValuesHolder[] mValues;
    HashMap<String, PropertyValuesHolder> mValuesMap;

    public interface AnimatorUpdateListener {
        void onAnimationUpdate(ValueAnimator valueAnimator);
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.animation.ValueAnimator.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.animation.ValueAnimator.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.animation.ValueAnimator.<clinit>():void");
    }

    public static void setDurationScale(float durationScale) {
        sDurationScale = durationScale;
    }

    public static float getDurationScale() {
        return sDurationScale;
    }

    public ValueAnimator() {
        this.mSeekFraction = ScaledLayoutParams.SCALE_UNSPECIFIED;
        this.mResumed = DEBUG;
        this.mOverallFraction = 0.0f;
        this.mCurrentFraction = 0.0f;
        this.mLastFrameTime = 0;
        this.mRunning = DEBUG;
        this.mStarted = DEBUG;
        this.mStartListenersCalled = DEBUG;
        this.mInitialized = DEBUG;
        this.mAnimationEndRequested = DEBUG;
        this.mDuration = 300;
        this.mStartDelay = 0;
        this.mRepeatCount = 0;
        this.mRepeatMode = RESTART;
        this.mInterpolator = sDefaultInterpolator;
        this.mUpdateListeners = null;
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
            if (this.mValues == null || this.mValues.length == 0) {
                PropertyValuesHolder[] propertyValuesHolderArr = new PropertyValuesHolder[RESTART];
                propertyValuesHolderArr[0] = PropertyValuesHolder.ofInt(ProxyInfo.LOCAL_EXCL_LIST, values);
                setValues(propertyValuesHolderArr);
            } else {
                this.mValues[0].setIntValues(values);
            }
            this.mInitialized = DEBUG;
        }
    }

    public void setFloatValues(float... values) {
        if (values != null && values.length != 0) {
            if (this.mValues == null || this.mValues.length == 0) {
                PropertyValuesHolder[] propertyValuesHolderArr = new PropertyValuesHolder[RESTART];
                propertyValuesHolderArr[0] = PropertyValuesHolder.ofFloat(ProxyInfo.LOCAL_EXCL_LIST, values);
                setValues(propertyValuesHolderArr);
            } else {
                this.mValues[0].setFloatValues(values);
            }
            this.mInitialized = DEBUG;
        }
    }

    public void setObjectValues(Object... values) {
        if (values != null && values.length != 0) {
            if (this.mValues == null || this.mValues.length == 0) {
                PropertyValuesHolder[] propertyValuesHolderArr = new PropertyValuesHolder[RESTART];
                propertyValuesHolderArr[0] = PropertyValuesHolder.ofObject(ProxyInfo.LOCAL_EXCL_LIST, null, values);
                setValues(propertyValuesHolderArr);
            } else {
                this.mValues[0].setObjectValues(values);
            }
            this.mInitialized = DEBUG;
        }
    }

    public void setValues(PropertyValuesHolder... values) {
        int numValues = values.length;
        this.mValues = values;
        this.mValuesMap = new HashMap(numValues);
        for (int i = 0; i < numValues; i += RESTART) {
            PropertyValuesHolder valuesHolder = values[i];
            this.mValuesMap.put(valuesHolder.getPropertyName(), valuesHolder);
        }
        this.mInitialized = DEBUG;
    }

    public PropertyValuesHolder[] getValues() {
        return this.mValues;
    }

    void initAnimation() {
        if (!this.mInitialized) {
            int numValues = this.mValues.length;
            for (int i = 0; i < numValues; i += RESTART) {
                this.mValues[i].init();
            }
            this.mInitialized = true;
        }
    }

    public ValueAnimator setDuration(long duration) {
        if (duration < 0) {
            throw new IllegalArgumentException("Animators cannot have negative duration: " + duration);
        }
        this.mDuration = duration;
        return this;
    }

    private long getScaledDuration() {
        return (long) (((float) this.mDuration) * sDurationScale);
    }

    public long getDuration() {
        return this.mDuration;
    }

    public long getTotalDuration() {
        if (this.mRepeatCount == INFINITE) {
            return -1;
        }
        return this.mStartDelay + (this.mDuration * ((long) (this.mRepeatCount + RESTART)));
    }

    public void setCurrentPlayTime(long playTime) {
        setCurrentFraction(this.mDuration > 0 ? ((float) playTime) / ((float) this.mDuration) : Engine.DEFAULT_VOLUME);
    }

    public void setCurrentFraction(float fraction) {
        initAnimation();
        fraction = clampFraction(fraction);
        this.mStartTime = AnimationUtils.currentAnimationTimeMillis() - ((long) (((float) getScaledDuration()) * fraction));
        this.mStartTimeCommitted = true;
        if (!isPulsingInternal()) {
            this.mSeekFraction = fraction;
        }
        this.mOverallFraction = fraction;
        animateValue(getCurrentIterationFraction(fraction));
    }

    private int getCurrentIteration(float fraction) {
        fraction = clampFraction(fraction);
        double iteration = Math.floor((double) fraction);
        if (((double) fraction) == iteration && fraction > 0.0f) {
            iteration -= 1.0d;
        }
        return (int) iteration;
    }

    private float getCurrentIterationFraction(float fraction) {
        fraction = clampFraction(fraction);
        int iteration = getCurrentIteration(fraction);
        float currentFraction = fraction - ((float) iteration);
        return shouldPlayBackward(iteration) ? Engine.DEFAULT_VOLUME - currentFraction : currentFraction;
    }

    private float clampFraction(float fraction) {
        if (fraction < 0.0f) {
            return 0.0f;
        }
        if (this.mRepeatCount != INFINITE) {
            return Math.min(fraction, (float) (this.mRepeatCount + RESTART));
        }
        return fraction;
    }

    private boolean shouldPlayBackward(int iteration) {
        boolean z = true;
        if (iteration <= 0 || this.mRepeatMode != REVERSE || (iteration >= this.mRepeatCount + RESTART && this.mRepeatCount != INFINITE)) {
            return this.mReversing;
        }
        if (this.mReversing) {
            if (iteration % REVERSE != 0) {
                z = DEBUG;
            }
            return z;
        }
        if (iteration % REVERSE == 0) {
            z = DEBUG;
        }
        return z;
    }

    public long getCurrentPlayTime() {
        if (!this.mInitialized || (!this.mStarted && this.mSeekFraction < 0.0f)) {
            return 0;
        }
        if (this.mSeekFraction >= 0.0f) {
            return (long) (((float) this.mDuration) * this.mSeekFraction);
        }
        return (long) (((float) (AnimationUtils.currentAnimationTimeMillis() - this.mStartTime)) / (sDurationScale == 0.0f ? Engine.DEFAULT_VOLUME : sDurationScale));
    }

    public long getStartDelay() {
        return this.mStartDelay;
    }

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
        if (this.mValues == null || this.mValues.length <= 0) {
            return null;
        }
        return this.mValues[0].getAnimatedValue();
    }

    public Object getAnimatedValue(String propertyName) {
        PropertyValuesHolder valuesHolder = (PropertyValuesHolder) this.mValuesMap.get(propertyName);
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
            this.mUpdateListeners = new ArrayList();
        }
        this.mUpdateListeners.add(listener);
    }

    public void removeAllUpdateListeners() {
        if (this.mUpdateListeners != null) {
            this.mUpdateListeners.clear();
            this.mUpdateListeners = null;
        }
    }

    public void removeUpdateListener(AnimatorUpdateListener listener) {
        if (this.mUpdateListeners != null) {
            this.mUpdateListeners.remove(listener);
            if (this.mUpdateListeners.size() == 0) {
                this.mUpdateListeners = null;
            }
        }
    }

    public void setInterpolator(TimeInterpolator value) {
        if (value != null) {
            this.mInterpolator = value;
        } else {
            this.mInterpolator = new LinearInterpolator();
        }
    }

    public TimeInterpolator getInterpolator() {
        return this.mInterpolator;
    }

    public void setEvaluator(TypeEvaluator value) {
        if (value != null && this.mValues != null && this.mValues.length > 0) {
            this.mValues[0].setEvaluator(value);
        }
    }

    private void notifyStartListeners() {
        if (!(this.mListeners == null || this.mStartListenersCalled)) {
            ArrayList<AnimatorListener> tmpListeners = (ArrayList) this.mListeners.clone();
            int numListeners = tmpListeners.size();
            for (int i = 0; i < numListeners; i += RESTART) {
                ((AnimatorListener) tmpListeners.get(i)).onAnimationStart(this);
            }
        }
        this.mStartListenersCalled = true;
    }

    private void start(boolean playBackwards) {
        if (Looper.myLooper() == null) {
            throw new AndroidRuntimeException("Animators may only be run on Looper threads");
        }
        this.mReversing = playBackwards;
        if (!(!playBackwards || this.mSeekFraction == ScaledLayoutParams.SCALE_UNSPECIFIED || this.mSeekFraction == 0.0f)) {
            if (this.mRepeatCount == INFINITE) {
                this.mSeekFraction = Engine.DEFAULT_VOLUME - ((float) (((double) this.mSeekFraction) - Math.floor((double) this.mSeekFraction)));
            } else {
                this.mSeekFraction = ((float) (this.mRepeatCount + RESTART)) - this.mSeekFraction;
            }
        }
        this.mStarted = true;
        this.mPaused = DEBUG;
        this.mRunning = DEBUG;
        this.mLastFrameTime = 0;
        AnimationHandler.getInstance().addAnimationFrameCallback(this, (long) (((float) this.mStartDelay) * sDurationScale));
        if (this.mStartDelay == 0 || this.mSeekFraction >= 0.0f) {
            startAnimation();
            if (this.mSeekFraction == ScaledLayoutParams.SCALE_UNSPECIFIED) {
                setCurrentPlayTime(0);
            } else {
                setCurrentFraction(this.mSeekFraction);
            }
        }
    }

    public void start() {
        start(DEBUG);
    }

    public void cancel() {
        if (Looper.myLooper() == null) {
            throw new AndroidRuntimeException("Animators may only be run on Looper threads");
        } else if (!this.mAnimationEndRequested) {
            if ((this.mStarted || this.mRunning) && this.mListeners != null) {
                if (!this.mRunning) {
                    notifyStartListeners();
                }
                for (AnimatorListener listener : (ArrayList) this.mListeners.clone()) {
                    listener.onAnimationCancel(this);
                }
            }
            endAnimation();
        }
    }

    public void end() {
        if (Looper.myLooper() == null) {
            throw new AndroidRuntimeException("Animators may only be run on Looper threads");
        }
        if (!this.mRunning) {
            startAnimation();
            this.mStarted = true;
        } else if (!this.mInitialized) {
            initAnimation();
        }
        animateValue(shouldPlayBackward(this.mRepeatCount) ? 0.0f : Engine.DEFAULT_VOLUME);
        endAnimation();
    }

    public void resume() {
        if (Looper.myLooper() == null) {
            throw new AndroidRuntimeException("Animators may only be resumed from the same thread that the animator was started on");
        }
        if (this.mPaused && !this.mResumed) {
            this.mResumed = true;
            if (this.mPauseTime > 0) {
                AnimationHandler.getInstance().addAnimationFrameCallback(this, 0);
            }
        }
        super.resume();
    }

    public void pause() {
        boolean previouslyPaused = this.mPaused;
        super.pause();
        if (!previouslyPaused && this.mPaused) {
            this.mPauseTime = -1;
            this.mResumed = DEBUG;
        }
    }

    public boolean isRunning() {
        return this.mRunning;
    }

    public boolean isStarted() {
        return this.mStarted;
    }

    public void reverse() {
        boolean z = DEBUG;
        if (isPulsingInternal()) {
            long currentTime = AnimationUtils.currentAnimationTimeMillis();
            this.mStartTime = currentTime - (getScaledDuration() - (currentTime - this.mStartTime));
            this.mStartTimeCommitted = true;
            if (!this.mReversing) {
                z = true;
            }
            this.mReversing = z;
        } else if (this.mStarted) {
            if (!this.mReversing) {
                z = true;
            }
            this.mReversing = z;
            end();
        } else {
            start(true);
        }
    }

    public boolean canReverse() {
        return true;
    }

    private void endAnimation() {
        if (!this.mAnimationEndRequested) {
            AnimationHandler.getInstance().removeCallback(this);
            this.mAnimationEndRequested = true;
            this.mPaused = DEBUG;
            if ((this.mStarted || this.mRunning) && this.mListeners != null) {
                if (!this.mRunning) {
                    notifyStartListeners();
                }
                ArrayList<AnimatorListener> tmpListeners = (ArrayList) this.mListeners.clone();
                int numListeners = tmpListeners.size();
                for (int i = 0; i < numListeners; i += RESTART) {
                    ((AnimatorListener) tmpListeners.get(i)).onAnimationEnd(this);
                }
            }
            this.mRunning = DEBUG;
            this.mStarted = DEBUG;
            this.mStartListenersCalled = DEBUG;
            this.mReversing = DEBUG;
            this.mLastFrameTime = 0;
            if (Trace.isTagEnabled(8)) {
                Trace.asyncTraceEnd(8, getNameForTrace(), System.identityHashCode(this));
            }
        }
    }

    private void startAnimation() {
        if (Trace.isTagEnabled(8)) {
            Trace.asyncTraceBegin(8, getNameForTrace(), System.identityHashCode(this));
        }
        this.mAnimationEndRequested = DEBUG;
        initAnimation();
        this.mRunning = true;
        if (this.mSeekFraction >= 0.0f) {
            this.mOverallFraction = this.mSeekFraction;
        } else {
            this.mOverallFraction = 0.0f;
        }
        if (this.mListeners != null) {
            notifyStartListeners();
        }
    }

    private boolean isPulsingInternal() {
        return this.mLastFrameTime > 0 ? true : DEBUG;
    }

    String getNameForTrace() {
        return "animator";
    }

    public void commitAnimationFrame(long frameTime) {
        if (!this.mStartTimeCommitted) {
            this.mStartTimeCommitted = true;
            long adjustment = frameTime - this.mLastFrameTime;
            if (adjustment > 0) {
                this.mStartTime += adjustment;
            }
        }
    }

    boolean animateBasedOnTime(long currentTime) {
        boolean done = DEBUG;
        if (this.mRunning) {
            long scaledDuration = getScaledDuration();
            float fraction = scaledDuration > 0 ? ((float) (currentTime - this.mStartTime)) / ((float) scaledDuration) : Engine.DEFAULT_VOLUME;
            boolean newIteration = ((int) fraction) > ((int) this.mOverallFraction) ? true : DEBUG;
            boolean lastIterationFinished = fraction >= ((float) (this.mRepeatCount + RESTART)) ? this.mRepeatCount != INFINITE ? true : DEBUG : DEBUG;
            if (scaledDuration == 0) {
                done = true;
            } else if (!newIteration || lastIterationFinished) {
                if (lastIterationFinished) {
                    done = true;
                }
            } else if (this.mListeners != null) {
                int numListeners = this.mListeners.size();
                for (int i = 0; i < numListeners; i += RESTART) {
                    ((AnimatorListener) this.mListeners.get(i)).onAnimationRepeat(this);
                }
            }
            this.mOverallFraction = clampFraction(fraction);
            animateValue(getCurrentIterationFraction(this.mOverallFraction));
        }
        return done;
    }

    public final void doAnimationFrame(long frameTime) {
        AnimationHandler handler = AnimationHandler.getInstance();
        if (this.mLastFrameTime == 0) {
            handler.addOneShotCommitCallback(this);
            if (this.mStartDelay > 0) {
                startAnimation();
            }
            if (this.mSeekFraction < 0.0f) {
                this.mStartTime = frameTime;
            } else {
                this.mStartTime = frameTime - ((long) (((float) getScaledDuration()) * this.mSeekFraction));
                this.mSeekFraction = ScaledLayoutParams.SCALE_UNSPECIFIED;
            }
            this.mStartTimeCommitted = DEBUG;
        }
        this.mLastFrameTime = frameTime;
        if (this.mPaused) {
            this.mPauseTime = frameTime;
            handler.removeCallback(this);
            return;
        }
        if (this.mResumed) {
            this.mResumed = DEBUG;
            if (this.mPauseTime > 0) {
                this.mStartTime += frameTime - this.mPauseTime;
                this.mStartTimeCommitted = DEBUG;
            }
            handler.addOneShotCommitCallback(this);
        }
        if (animateBasedOnTime(Math.max(frameTime, this.mStartTime))) {
            endAnimation();
        }
    }

    public float getAnimatedFraction() {
        return this.mCurrentFraction;
    }

    void animateValue(float fraction) {
        int i;
        fraction = this.mInterpolator.getInterpolation(fraction);
        this.mCurrentFraction = fraction;
        int numValues = this.mValues.length;
        for (i = 0; i < numValues; i += RESTART) {
            this.mValues[i].calculateValue(fraction);
        }
        if (this.mUpdateListeners != null) {
            int numListeners = this.mUpdateListeners.size();
            for (i = 0; i < numListeners; i += RESTART) {
                ((AnimatorUpdateListener) this.mUpdateListeners.get(i)).onAnimationUpdate(this);
            }
        }
    }

    public ValueAnimator clone() {
        ValueAnimator anim = (ValueAnimator) super.clone();
        if (this.mUpdateListeners != null) {
            anim.mUpdateListeners = new ArrayList(this.mUpdateListeners);
        }
        anim.mSeekFraction = ScaledLayoutParams.SCALE_UNSPECIFIED;
        anim.mReversing = DEBUG;
        anim.mInitialized = DEBUG;
        anim.mStarted = DEBUG;
        anim.mRunning = DEBUG;
        anim.mPaused = DEBUG;
        anim.mResumed = DEBUG;
        anim.mStartListenersCalled = DEBUG;
        anim.mStartTime = 0;
        anim.mStartTimeCommitted = DEBUG;
        anim.mAnimationEndRequested = DEBUG;
        anim.mPauseTime = 0;
        anim.mLastFrameTime = 0;
        anim.mOverallFraction = 0.0f;
        anim.mCurrentFraction = 0.0f;
        PropertyValuesHolder[] oldValues = this.mValues;
        if (oldValues != null) {
            int numValues = oldValues.length;
            anim.mValues = new PropertyValuesHolder[numValues];
            anim.mValuesMap = new HashMap(numValues);
            for (int i = 0; i < numValues; i += RESTART) {
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

    public String toString() {
        String returnVal = "ValueAnimator@" + Integer.toHexString(hashCode());
        if (this.mValues != null) {
            for (int i = 0; i < this.mValues.length; i += RESTART) {
                returnVal = returnVal + "\n    " + this.mValues[i].toString();
            }
        }
        return returnVal;
    }

    public void setAllowRunningAsynchronously(boolean mayRunAsync) {
    }
}
