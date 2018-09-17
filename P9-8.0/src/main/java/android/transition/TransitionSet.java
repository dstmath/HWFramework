package android.transition;

import android.animation.TimeInterpolator;
import android.content.Context;
import android.content.res.TypedArray;
import android.transition.Transition.EpicenterCallback;
import android.transition.Transition.TransitionListener;
import android.util.AndroidRuntimeException;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import com.android.internal.R;
import java.util.ArrayList;

public class TransitionSet extends Transition {
    public static final int ORDERING_SEQUENTIAL = 1;
    public static final int ORDERING_TOGETHER = 0;
    int mCurrentListeners;
    private boolean mPlayTogether = true;
    boolean mStarted = false;
    ArrayList<Transition> mTransitions = new ArrayList();

    static class TransitionSetListener extends TransitionListenerAdapter {
        TransitionSet mTransitionSet;

        TransitionSetListener(TransitionSet transitionSet) {
            this.mTransitionSet = transitionSet;
        }

        public void onTransitionStart(Transition transition) {
            if (!this.mTransitionSet.mStarted) {
                this.mTransitionSet.start();
                this.mTransitionSet.mStarted = true;
            }
        }

        public void onTransitionEnd(Transition transition) {
            TransitionSet transitionSet = this.mTransitionSet;
            transitionSet.mCurrentListeners--;
            if (this.mTransitionSet.mCurrentListeners == 0) {
                this.mTransitionSet.mStarted = false;
                this.mTransitionSet.end();
            }
            transition.removeListener(this);
        }
    }

    public TransitionSet(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.TransitionSet);
        setOrdering(a.getInt(0, 0));
        a.recycle();
    }

    public TransitionSet setOrdering(int ordering) {
        switch (ordering) {
            case 0:
                this.mPlayTogether = true;
                break;
            case 1:
                this.mPlayTogether = false;
                break;
            default:
                throw new AndroidRuntimeException("Invalid parameter for TransitionSet ordering: " + ordering);
        }
        return this;
    }

    public int getOrdering() {
        return this.mPlayTogether ? 0 : 1;
    }

    public TransitionSet addTransition(Transition transition) {
        if (transition != null) {
            this.mTransitions.add(transition);
            transition.mParent = this;
            if (this.mDuration >= 0) {
                transition.setDuration(this.mDuration);
            }
        }
        return this;
    }

    public int getTransitionCount() {
        return this.mTransitions.size();
    }

    public Transition getTransitionAt(int index) {
        if (index < 0 || index >= this.mTransitions.size()) {
            return null;
        }
        return (Transition) this.mTransitions.get(index);
    }

    public TransitionSet setDuration(long duration) {
        super.setDuration(duration);
        if (this.mDuration >= 0 && this.mTransitions != null) {
            int numTransitions = this.mTransitions.size();
            for (int i = 0; i < numTransitions; i++) {
                ((Transition) this.mTransitions.get(i)).setDuration(duration);
            }
        }
        return this;
    }

    public TransitionSet setStartDelay(long startDelay) {
        return (TransitionSet) super.setStartDelay(startDelay);
    }

    public TransitionSet setInterpolator(TimeInterpolator interpolator) {
        return (TransitionSet) super.setInterpolator(interpolator);
    }

    public TransitionSet addTarget(View target) {
        for (int i = 0; i < this.mTransitions.size(); i++) {
            ((Transition) this.mTransitions.get(i)).addTarget(target);
        }
        return (TransitionSet) super.addTarget(target);
    }

    public TransitionSet addTarget(int targetId) {
        for (int i = 0; i < this.mTransitions.size(); i++) {
            ((Transition) this.mTransitions.get(i)).addTarget(targetId);
        }
        return (TransitionSet) super.addTarget(targetId);
    }

    public TransitionSet addTarget(String targetName) {
        for (int i = 0; i < this.mTransitions.size(); i++) {
            ((Transition) this.mTransitions.get(i)).addTarget(targetName);
        }
        return (TransitionSet) super.addTarget(targetName);
    }

    public TransitionSet addTarget(Class targetType) {
        for (int i = 0; i < this.mTransitions.size(); i++) {
            ((Transition) this.mTransitions.get(i)).addTarget(targetType);
        }
        return (TransitionSet) super.addTarget(targetType);
    }

    public TransitionSet addListener(TransitionListener listener) {
        return (TransitionSet) super.addListener(listener);
    }

    public TransitionSet removeTarget(int targetId) {
        for (int i = 0; i < this.mTransitions.size(); i++) {
            ((Transition) this.mTransitions.get(i)).removeTarget(targetId);
        }
        return (TransitionSet) super.removeTarget(targetId);
    }

    public TransitionSet removeTarget(View target) {
        for (int i = 0; i < this.mTransitions.size(); i++) {
            ((Transition) this.mTransitions.get(i)).removeTarget(target);
        }
        return (TransitionSet) super.removeTarget(target);
    }

    public TransitionSet removeTarget(Class target) {
        for (int i = 0; i < this.mTransitions.size(); i++) {
            ((Transition) this.mTransitions.get(i)).removeTarget(target);
        }
        return (TransitionSet) super.removeTarget(target);
    }

    public TransitionSet removeTarget(String target) {
        for (int i = 0; i < this.mTransitions.size(); i++) {
            ((Transition) this.mTransitions.get(i)).removeTarget(target);
        }
        return (TransitionSet) super.removeTarget(target);
    }

    public Transition excludeTarget(View target, boolean exclude) {
        for (int i = 0; i < this.mTransitions.size(); i++) {
            ((Transition) this.mTransitions.get(i)).excludeTarget(target, exclude);
        }
        return super.excludeTarget(target, exclude);
    }

    public Transition excludeTarget(String targetName, boolean exclude) {
        for (int i = 0; i < this.mTransitions.size(); i++) {
            ((Transition) this.mTransitions.get(i)).excludeTarget(targetName, exclude);
        }
        return super.excludeTarget(targetName, exclude);
    }

    public Transition excludeTarget(int targetId, boolean exclude) {
        for (int i = 0; i < this.mTransitions.size(); i++) {
            ((Transition) this.mTransitions.get(i)).excludeTarget(targetId, exclude);
        }
        return super.excludeTarget(targetId, exclude);
    }

    public Transition excludeTarget(Class type, boolean exclude) {
        for (int i = 0; i < this.mTransitions.size(); i++) {
            ((Transition) this.mTransitions.get(i)).excludeTarget(type, exclude);
        }
        return super.excludeTarget(type, exclude);
    }

    public TransitionSet removeListener(TransitionListener listener) {
        return (TransitionSet) super.removeListener(listener);
    }

    public void setPathMotion(PathMotion pathMotion) {
        super.setPathMotion(pathMotion);
        for (int i = 0; i < this.mTransitions.size(); i++) {
            ((Transition) this.mTransitions.get(i)).setPathMotion(pathMotion);
        }
    }

    public TransitionSet removeTransition(Transition transition) {
        this.mTransitions.remove(transition);
        transition.mParent = null;
        return this;
    }

    private void setupStartEndListeners() {
        TransitionSetListener listener = new TransitionSetListener(this);
        for (Transition childTransition : this.mTransitions) {
            childTransition.addListener(listener);
        }
        this.mCurrentListeners = this.mTransitions.size();
    }

    protected void createAnimators(ViewGroup sceneRoot, TransitionValuesMaps startValues, TransitionValuesMaps endValues, ArrayList<TransitionValues> startValuesList, ArrayList<TransitionValues> endValuesList) {
        long startDelay = getStartDelay();
        int numTransitions = this.mTransitions.size();
        int i = 0;
        while (i < numTransitions) {
            Transition childTransition = (Transition) this.mTransitions.get(i);
            if (startDelay > 0 && (this.mPlayTogether || i == 0)) {
                long childStartDelay = childTransition.getStartDelay();
                if (childStartDelay > 0) {
                    childTransition.setStartDelay(startDelay + childStartDelay);
                } else {
                    childTransition.setStartDelay(startDelay);
                }
            }
            childTransition.createAnimators(sceneRoot, startValues, endValues, startValuesList, endValuesList);
            i++;
        }
    }

    protected void runAnimators() {
        if (this.mTransitions.isEmpty()) {
            start();
            end();
            return;
        }
        setupStartEndListeners();
        int numTransitions = this.mTransitions.size();
        int i;
        if (this.mPlayTogether) {
            for (i = 0; i < numTransitions; i++) {
                ((Transition) this.mTransitions.get(i)).runAnimators();
            }
        } else {
            for (i = 1; i < numTransitions; i++) {
                final Transition nextTransition = (Transition) this.mTransitions.get(i);
                ((Transition) this.mTransitions.get(i - 1)).addListener(new TransitionListenerAdapter() {
                    public void onTransitionEnd(Transition transition) {
                        nextTransition.runAnimators();
                        transition.removeListener(this);
                    }
                });
            }
            Transition firstTransition = (Transition) this.mTransitions.get(0);
            if (firstTransition != null) {
                firstTransition.runAnimators();
            }
        }
    }

    public void captureStartValues(TransitionValues transitionValues) {
        if (isValidTarget(transitionValues.view)) {
            for (Transition childTransition : this.mTransitions) {
                if (childTransition.isValidTarget(transitionValues.view)) {
                    childTransition.captureStartValues(transitionValues);
                    transitionValues.targetedTransitions.add(childTransition);
                }
            }
        }
    }

    public void captureEndValues(TransitionValues transitionValues) {
        if (isValidTarget(transitionValues.view)) {
            for (Transition childTransition : this.mTransitions) {
                if (childTransition.isValidTarget(transitionValues.view)) {
                    childTransition.captureEndValues(transitionValues);
                    transitionValues.targetedTransitions.add(childTransition);
                }
            }
        }
    }

    void capturePropagationValues(TransitionValues transitionValues) {
        super.capturePropagationValues(transitionValues);
        int numTransitions = this.mTransitions.size();
        for (int i = 0; i < numTransitions; i++) {
            ((Transition) this.mTransitions.get(i)).capturePropagationValues(transitionValues);
        }
    }

    public void pause(View sceneRoot) {
        super.pause(sceneRoot);
        int numTransitions = this.mTransitions.size();
        for (int i = 0; i < numTransitions; i++) {
            ((Transition) this.mTransitions.get(i)).pause(sceneRoot);
        }
    }

    public void resume(View sceneRoot) {
        super.resume(sceneRoot);
        int numTransitions = this.mTransitions.size();
        for (int i = 0; i < numTransitions; i++) {
            ((Transition) this.mTransitions.get(i)).resume(sceneRoot);
        }
    }

    protected void cancel() {
        super.cancel();
        int numTransitions = this.mTransitions.size();
        for (int i = 0; i < numTransitions; i++) {
            ((Transition) this.mTransitions.get(i)).cancel();
        }
    }

    void forceToEnd(ViewGroup sceneRoot) {
        super.forceToEnd(sceneRoot);
        int numTransitions = this.mTransitions.size();
        for (int i = 0; i < numTransitions; i++) {
            ((Transition) this.mTransitions.get(i)).forceToEnd(sceneRoot);
        }
    }

    TransitionSet setSceneRoot(ViewGroup sceneRoot) {
        super.setSceneRoot(sceneRoot);
        int numTransitions = this.mTransitions.size();
        for (int i = 0; i < numTransitions; i++) {
            ((Transition) this.mTransitions.get(i)).setSceneRoot(sceneRoot);
        }
        return this;
    }

    void setCanRemoveViews(boolean canRemoveViews) {
        super.setCanRemoveViews(canRemoveViews);
        int numTransitions = this.mTransitions.size();
        for (int i = 0; i < numTransitions; i++) {
            ((Transition) this.mTransitions.get(i)).setCanRemoveViews(canRemoveViews);
        }
    }

    public void setPropagation(TransitionPropagation propagation) {
        super.setPropagation(propagation);
        int numTransitions = this.mTransitions.size();
        for (int i = 0; i < numTransitions; i++) {
            ((Transition) this.mTransitions.get(i)).setPropagation(propagation);
        }
    }

    public void setEpicenterCallback(EpicenterCallback epicenterCallback) {
        super.setEpicenterCallback(epicenterCallback);
        int numTransitions = this.mTransitions.size();
        for (int i = 0; i < numTransitions; i++) {
            ((Transition) this.mTransitions.get(i)).setEpicenterCallback(epicenterCallback);
        }
    }

    String toString(String indent) {
        String result = super.toString(indent);
        for (int i = 0; i < this.mTransitions.size(); i++) {
            result = result + "\n" + ((Transition) this.mTransitions.get(i)).toString(indent + "  ");
        }
        return result;
    }

    public TransitionSet clone() {
        TransitionSet clone = (TransitionSet) super.clone();
        clone.mTransitions = new ArrayList();
        int numTransitions = this.mTransitions.size();
        for (int i = 0; i < numTransitions; i++) {
            clone.addTransition(((Transition) this.mTransitions.get(i)).clone());
        }
        return clone;
    }
}
