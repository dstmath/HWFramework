package ohos.agp.transition;

public class TransitionMove extends TransitionSlide {
    public static final int BOTTOM = 3;
    public static final int LEFT = 0;
    public static final int RIGHT = 1;
    public static final int TOP = 2;

    public TransitionMove() {
    }

    public TransitionMove(int i) {
        super(i);
    }

    @Override // ohos.agp.transition.TransitionSlide, ohos.agp.transition.Transition
    public long getNativeTransitionPtr() {
        return super.getNativeTransitionPtr();
    }

    @Override // ohos.agp.transition.TransitionSlide, ohos.agp.transition.Transition
    public void setTransitionDuration(float f) {
        super.setTransitionDuration(f);
    }
}
