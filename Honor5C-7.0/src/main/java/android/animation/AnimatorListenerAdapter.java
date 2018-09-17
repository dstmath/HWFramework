package android.animation;

import android.animation.Animator.AnimatorListener;
import android.animation.Animator.AnimatorPauseListener;

public abstract class AnimatorListenerAdapter implements AnimatorListener, AnimatorPauseListener {
    public void onAnimationCancel(Animator animation) {
    }

    public void onAnimationEnd(Animator animation) {
    }

    public void onAnimationRepeat(Animator animation) {
    }

    public void onAnimationStart(Animator animation) {
    }

    public void onAnimationPause(Animator animation) {
    }

    public void onAnimationResume(Animator animation) {
    }
}
