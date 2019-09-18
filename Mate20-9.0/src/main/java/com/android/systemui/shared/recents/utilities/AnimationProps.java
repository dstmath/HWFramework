package com.android.systemui.shared.recents.utilities;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ValueAnimator;
import android.util.SparseArray;
import android.util.SparseLongArray;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

public class AnimationProps {
    public static final int ALL = 0;
    public static final int ALPHA = 4;
    public static final int BOUNDS = 6;
    public static final int DIM_ALPHA = 7;
    public static final AnimationProps IMMEDIATE = new AnimationProps(0, LINEAR_INTERPOLATOR);
    private static final Interpolator LINEAR_INTERPOLATOR = new LinearInterpolator();
    public static final int SCALE = 5;
    public static final int TRANSLATION_X = 1;
    public static final int TRANSLATION_Y = 2;
    public static final int TRANSLATION_Z = 3;
    private Animator.AnimatorListener mListener;
    private SparseLongArray mPropDuration;
    private SparseArray<Interpolator> mPropInterpolators;
    private SparseLongArray mPropStartDelay;

    @Retention(RetentionPolicy.SOURCE)
    public @interface PropType {
    }

    public AnimationProps() {
    }

    public AnimationProps(int duration, Interpolator interpolator) {
        this(0, duration, interpolator, null);
    }

    public AnimationProps(int duration, Interpolator interpolator, Animator.AnimatorListener listener) {
        this(0, duration, interpolator, listener);
    }

    public AnimationProps(int startDelay, int duration, Interpolator interpolator) {
        this(startDelay, duration, interpolator, null);
    }

    public AnimationProps(int startDelay, int duration, Interpolator interpolator, Animator.AnimatorListener listener) {
        setStartDelay(0, startDelay);
        setDuration(0, duration);
        setInterpolator(0, interpolator);
        setListener(listener);
    }

    public AnimatorSet createAnimator(List<Animator> animators) {
        AnimatorSet anim = new AnimatorSet();
        if (this.mListener != null) {
            anim.addListener(this.mListener);
        }
        anim.playTogether(animators);
        return anim;
    }

    public <T extends ValueAnimator> T apply(int propertyType, T animator) {
        animator.setStartDelay(getStartDelay(propertyType));
        animator.setDuration(getDuration(propertyType));
        animator.setInterpolator(getInterpolator(propertyType));
        return animator;
    }

    public AnimationProps setStartDelay(int propertyType, int startDelay) {
        if (this.mPropStartDelay == null) {
            this.mPropStartDelay = new SparseLongArray();
        }
        this.mPropStartDelay.append(propertyType, (long) startDelay);
        return this;
    }

    public long getStartDelay(int propertyType) {
        if (this.mPropStartDelay == null) {
            return 0;
        }
        long startDelay = this.mPropStartDelay.get(propertyType, -1);
        if (startDelay != -1) {
            return startDelay;
        }
        return this.mPropStartDelay.get(0, 0);
    }

    public AnimationProps setDuration(int propertyType, int duration) {
        if (this.mPropDuration == null) {
            this.mPropDuration = new SparseLongArray();
        }
        this.mPropDuration.append(propertyType, (long) duration);
        return this;
    }

    public long getDuration(int propertyType) {
        if (this.mPropDuration == null) {
            return 0;
        }
        long duration = this.mPropDuration.get(propertyType, -1);
        if (duration != -1) {
            return duration;
        }
        return this.mPropDuration.get(0, 0);
    }

    public AnimationProps setInterpolator(int propertyType, Interpolator interpolator) {
        if (this.mPropInterpolators == null) {
            this.mPropInterpolators = new SparseArray<>();
        }
        this.mPropInterpolators.append(propertyType, interpolator);
        return this;
    }

    public Interpolator getInterpolator(int propertyType) {
        if (this.mPropInterpolators == null) {
            return LINEAR_INTERPOLATOR;
        }
        Interpolator interp = this.mPropInterpolators.get(propertyType);
        if (interp != null) {
            return interp;
        }
        return this.mPropInterpolators.get(0, LINEAR_INTERPOLATOR);
    }

    public AnimationProps setListener(Animator.AnimatorListener listener) {
        this.mListener = listener;
        return this;
    }

    public Animator.AnimatorListener getListener() {
        return this.mListener;
    }

    public boolean isImmediate() {
        int count = this.mPropDuration.size();
        for (int i = 0; i < count; i++) {
            if (this.mPropDuration.valueAt(i) > 0) {
                return false;
            }
        }
        return true;
    }
}
