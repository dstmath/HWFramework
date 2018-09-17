package huawei.android.animation;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.res.ConstantState;
import android.util.StateSet;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class HwStateListAnimatorImpl implements HwStateListAnimator, Cloneable {
    protected static final boolean DB = false;
    public static final int NORMAL_MODE = 0;
    public static final int PENDING_MODE = 1;
    protected static final String TAG = "HwStateListAnimator";
    private boolean mAnimatorEnable = true;
    private AnimatorListenerAdapter mAnimatorListener;
    private int mChangingConfigurations;
    private StateListAnimatorConstantState mConstantState;
    private Tuple mLastMatch = null;
    private int mMode = 1;
    private WeakReference<Object> mObjectRef;
    private ArrayList<Animator> mPendingAnimators = new ArrayList();
    private Animator mRunningAnimator = null;
    private ArrayList<Tuple> mTuples = new ArrayList();

    private static class StateListAnimatorConstantState extends ConstantState<HwStateListAnimator> {
        final HwStateListAnimatorImpl mAnimator;
        int mChangingConf = this.mAnimator.getChangingConfigurations();

        public StateListAnimatorConstantState(HwStateListAnimatorImpl animator) {
            this.mAnimator = animator;
            this.mAnimator.mConstantState = this;
        }

        public int getChangingConfigurations() {
            return this.mChangingConf;
        }

        public HwStateListAnimatorImpl newInstance() {
            HwStateListAnimatorImpl clone = this.mAnimator.clone();
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

    public HwStateListAnimatorImpl() {
        initAnimatorListener();
    }

    private void initAnimatorListener() {
        this.mAnimatorListener = new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                animation.setTarget(null);
                if (HwStateListAnimatorImpl.this.mAnimatorEnable) {
                    if (HwStateListAnimatorImpl.this.mRunningAnimator == animation) {
                        HwStateListAnimatorImpl.this.mRunningAnimator = null;
                    }
                    if (HwStateListAnimatorImpl.this.mMode == 1 && !HwStateListAnimatorImpl.this.mPendingAnimators.isEmpty()) {
                        HwStateListAnimatorImpl.this.mRunningAnimator = (Animator) HwStateListAnimatorImpl.this.mPendingAnimators.remove(0);
                        if (HwStateListAnimatorImpl.this.getTarget() != null) {
                            HwStateListAnimatorImpl.this.mRunningAnimator.setTarget(HwStateListAnimatorImpl.this.getTarget());
                            HwStateListAnimatorImpl.this.mRunningAnimator.start();
                            return;
                        }
                        HwStateListAnimatorImpl.this.clearTarget();
                        HwStateListAnimatorImpl.this.clearAnimator();
                        return;
                    }
                    return;
                }
                HwStateListAnimatorImpl.this.clearAnimator();
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

    public Object getTarget() {
        return this.mObjectRef == null ? null : this.mObjectRef.get();
    }

    public void setTarget(Object object) {
        Object current = getTarget();
        if (current != object) {
            if (current != null) {
                clearTarget();
            }
            if (object != null) {
                this.mObjectRef = new WeakReference(object);
            }
        }
    }

    private void clearTarget() {
        int size = this.mTuples.size();
        for (int i = 0; i < size; i++) {
            ((Tuple) this.mTuples.get(i)).mAnimator.setTarget(null);
        }
        this.mObjectRef = null;
        this.mLastMatch = null;
        this.mRunningAnimator = null;
    }

    public HwStateListAnimatorImpl clone() {
        try {
            HwStateListAnimatorImpl clone = (HwStateListAnimatorImpl) super.clone();
            clone.mTuples = new ArrayList(this.mTuples.size());
            clone.mLastMatch = null;
            clone.mRunningAnimator = null;
            clone.mObjectRef = null;
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
        if (this.mAnimatorEnable) {
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
    }

    private void start(Tuple match) {
        match.mAnimator.setTarget(getTarget());
        if (this.mMode == 0) {
            this.mRunningAnimator = match.mAnimator;
            this.mRunningAnimator.start();
        } else if (this.mMode != 1) {
        } else {
            if (this.mRunningAnimator == null || !this.mRunningAnimator.isRunning()) {
                start(match.mAnimator);
            } else if (this.mPendingAnimators.contains(match.mAnimator)) {
                this.mPendingAnimators.clear();
                start(match.mAnimator);
            } else {
                this.mPendingAnimators.add(match.mAnimator);
            }
        }
    }

    private void start(Animator animator) {
        this.mRunningAnimator = animator;
        this.mRunningAnimator.start();
    }

    private void cancel() {
        if (this.mRunningAnimator != null && this.mMode == 0) {
            this.mRunningAnimator.cancel();
            this.mRunningAnimator = null;
        }
    }

    public ArrayList<Tuple> getTuples() {
        return this.mTuples;
    }

    public void jumpToCurrentState() {
        if (this.mRunningAnimator != null && this.mMode == 0) {
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

    public ConstantState<HwStateListAnimator> createConstantState() {
        return new StateListAnimatorConstantState(this);
    }

    public void setMode(int mode) {
        this.mMode = mode;
    }

    public void setAnimatorEnable(boolean enable) {
        this.mAnimatorEnable = enable;
        if (!enable) {
            clearAnimator();
        }
    }

    private void clearAnimator() {
        this.mPendingAnimators.clear();
        if (this.mRunningAnimator != null && this.mRunningAnimator.isRunning()) {
            this.mRunningAnimator.cancel();
        }
        this.mRunningAnimator = null;
    }
}
