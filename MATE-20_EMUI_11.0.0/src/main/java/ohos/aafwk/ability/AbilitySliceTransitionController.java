package ohos.aafwk.ability;

import ohos.agp.components.ComponentContainer;
import ohos.agp.transition.Transition;
import ohos.agp.transition.TransitionAlpha;
import ohos.agp.transition.TransitionMove;
import ohos.agp.transition.TransitionScheduler;

public final class AbilitySliceTransitionController {
    private Transition transition = null;

    /* access modifiers changed from: package-private */
    public void setTransition(Transition transition2) {
        this.transition = transition2;
    }

    /* access modifiers changed from: package-private */
    public Transition getTransition() {
        return this.transition;
    }

    private Transition createTransition() {
        Transition transition2 = this.transition;
        if (transition2 == null) {
            return null;
        }
        if (transition2 instanceof TransitionAlpha) {
            return new TransitionAlpha();
        }
        if (transition2 instanceof TransitionMove) {
            return new TransitionMove();
        }
        return new Transition();
    }

    /* access modifiers changed from: package-private */
    public boolean isTransitionEnabled() {
        return this.transition != null;
    }

    /* access modifiers changed from: package-private */
    public void startTransition(ComponentContainer componentContainer, ComponentContainer componentContainer2) {
        startTransition(componentContainer, componentContainer2, null);
    }

    /* access modifiers changed from: package-private */
    public void startTransition(ComponentContainer componentContainer, ComponentContainer componentContainer2, TransitionScheduler.ITransitionEndListener iTransitionEndListener) {
        if (isTransitionEnabled()) {
            TransitionScheduler transitionScheduler = new TransitionScheduler();
            transitionScheduler.setTransition(createTransition());
            if (iTransitionEndListener != null) {
                transitionScheduler.setTransitionEndListener(iTransitionEndListener);
            }
            transitionScheduler.startNewRootTransition(componentContainer, componentContainer2);
        }
    }
}
