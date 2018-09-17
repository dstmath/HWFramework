package android.animation;

import android.content.res.ConstantState;
import android.util.StateSet;
import android.view.View;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class StateListAnimator implements Cloneable {
    private AnimatorListenerAdapter mAnimatorListener;
    private int mChangingConfigurations;
    private StateListAnimatorConstantState mConstantState;
    private Tuple mLastMatch = null;
    private Animator mRunningAnimator = null;
    private ArrayList<Tuple> mTuples = new ArrayList();
    private WeakReference<View> mViewRef;

    private static class StateListAnimatorConstantState extends ConstantState<StateListAnimator> {
        final StateListAnimator mAnimator;
        int mChangingConf = this.mAnimator.getChangingConfigurations();

        public StateListAnimatorConstantState(StateListAnimator animator) {
            this.mAnimator = animator;
            this.mAnimator.mConstantState = this;
        }

        public int getChangingConfigurations() {
            return this.mChangingConf;
        }

        public StateListAnimator newInstance() {
            StateListAnimator clone = this.mAnimator.clone();
            clone.mConstantState = this;
            return clone;
        }
    }

    public static class Tuple {
        final Animator mAnimator;
        final int[] mSpecs;

        /* synthetic */ Tuple(int[] specs, Animator animator, Tuple -this2) {
            this(specs, animator);
        }

        private Tuple(int[] specs, Animator animator) {
            this.mSpecs = specs;
            this.mAnimator = animator;
        }

        public int[] getSpecs() {
            return this.mSpecs;
        }

        public Animator getAnimator() {
            return this.mAnimator;
        }
    }

    public StateListAnimator() {
        initAnimatorListener();
    }

    private void initAnimatorListener() {
        this.mAnimatorListener = new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                animation.setTarget(null);
                if (StateListAnimator.this.mRunningAnimator == animation) {
                    StateListAnimator.this.mRunningAnimator = null;
                }
            }
        };
    }

    public void addState(int[] specs, Animator animator) {
        Tuple tuple = new Tuple(specs, animator, null);
        tuple.mAnimator.addListener(this.mAnimatorListener);
        this.mTuples.add(tuple);
        this.mChangingConfigurations |= animator.getChangingConfigurations();
    }

    public Animator getRunningAnimator() {
        return this.mRunningAnimator;
    }

    public View getTarget() {
        return this.mViewRef == null ? null : (View) this.mViewRef.get();
    }

    public void setTarget(View view) {
        View current = getTarget();
        if (current != view) {
            if (current != null) {
                clearTarget();
            }
            if (view != null) {
                this.mViewRef = new WeakReference(view);
            }
        }
    }

    private void clearTarget() {
        int size = this.mTuples.size();
        for (int i = 0; i < size; i++) {
            ((Tuple) this.mTuples.get(i)).mAnimator.setTarget(null);
        }
        this.mViewRef = null;
        this.mLastMatch = null;
        this.mRunningAnimator = null;
    }

    public StateListAnimator clone() {
        try {
            StateListAnimator clone = (StateListAnimator) super.clone();
            clone.mTuples = new ArrayList(this.mTuples.size());
            clone.mLastMatch = null;
            clone.mRunningAnimator = null;
            clone.mViewRef = null;
            clone.mAnimatorListener = null;
            clone.initAnimatorListener();
            int tupleSize = this.mTuples.size();
            for (int i = 0; i < tupleSize; i++) {
                Tuple tuple = (Tuple) this.mTuples.get(i);
                Animator animatorClone = tuple.mAnimator.clone();
                animatorClone.removeListener(this.mAnimatorListener);
                clone.addState(tuple.mSpecs, animatorClone);
            }
            clone.setChangingConfigurations(getChangingConfigurations());
            return clone;
        } catch (CloneNotSupportedException e) {
            throw new AssertionError("cannot clone state list animator", e);
        }
    }

    public void setState(int[] state) {
        Tuple match = null;
        int count = this.mTuples.size();
        for (int i = 0; i < count; i++) {
            Tuple tuple = (Tuple) this.mTuples.get(i);
            if (StateSet.stateSetMatches(tuple.mSpecs, state)) {
                match = tuple;
                break;
            }
        }
        if (match != this.mLastMatch) {
            if (this.mLastMatch != null) {
                cancel();
            }
            this.mLastMatch = match;
            if (match != null) {
                start(match);
            }
        }
    }

    private void start(Tuple match) {
        match.mAnimator.setTarget(getTarget());
        this.mRunningAnimator = match.mAnimator;
        this.mRunningAnimator.start();
    }

    private void cancel() {
        if (this.mRunningAnimator != null) {
            this.mRunningAnimator.cancel();
            this.mRunningAnimator = null;
        }
    }

    public ArrayList<Tuple> getTuples() {
        return this.mTuples;
    }

    public void jumpToCurrentState() {
        if (this.mRunningAnimator != null) {
            this.mRunningAnimator.end();
        }
    }

    public int getChangingConfigurations() {
        return this.mChangingConfigurations;
    }

    public void setChangingConfigurations(int configs) {
        this.mChangingConfigurations = configs;
    }

    public void appendChangingConfigurations(int configs) {
        this.mChangingConfigurations |= configs;
    }

    public ConstantState<StateListAnimator> createConstantState() {
        return new StateListAnimatorConstantState(this);
    }
}
