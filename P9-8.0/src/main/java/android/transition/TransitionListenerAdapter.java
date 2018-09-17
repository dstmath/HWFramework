package android.transition;

import android.transition.Transition.TransitionListener;

public abstract class TransitionListenerAdapter implements TransitionListener {
    public void onTransitionStart(Transition transition) {
    }

    public void onTransitionEnd(Transition transition) {
    }

    public void onTransitionCancel(Transition transition) {
    }

    public void onTransitionPause(Transition transition) {
    }

    public void onTransitionResume(Transition transition) {
    }
}
