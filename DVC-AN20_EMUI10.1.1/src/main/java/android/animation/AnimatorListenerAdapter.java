package android.animation;

import android.animation.Animator;

public abstract class AnimatorListenerAdapter implements Animator.AnimatorListener, Animator.AnimatorPauseListener {
    @Override // android.animation.Animator.AnimatorListener
    public void onAnimationCancel(Animator animation) {
    }

    @Override // android.animation.Animator.AnimatorListener
    public void onAnimationEnd(Animator animation) {
    }

    @Override // android.animation.Animator.AnimatorListener
    public void onAnimationRepeat(Animator animation) {
    }

    @Override // android.animation.Animator.AnimatorListener
    public void onAnimationStart(Animator animation) {
    }

    @Override // android.animation.Animator.AnimatorPauseListener
    public void onAnimationPause(Animator animation) {
    }

    @Override // android.animation.Animator.AnimatorPauseListener
    public void onAnimationResume(Animator animation) {
    }
}
