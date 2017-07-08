package android.graphics.drawable;

public interface Animatable2 extends Animatable {

    public static abstract class AnimationCallback {
        public void onAnimationStart(Drawable drawable) {
        }

        public void onAnimationEnd(Drawable drawable) {
        }
    }

    void clearAnimationCallbacks();

    void registerAnimationCallback(AnimationCallback animationCallback);

    boolean unregisterAnimationCallback(AnimationCallback animationCallback);
}
