package ohos.agp.transition;

public class TransitionAlpha extends TransitionFade {
    @Override // ohos.agp.transition.TransitionFade, ohos.agp.transition.Transition
    public long getNativeTransitionPtr() {
        return super.getNativeTransitionPtr();
    }

    @Override // ohos.agp.transition.TransitionFade, ohos.agp.transition.Transition
    public void setDuration(float f) {
        super.setDuration(f);
    }
}
