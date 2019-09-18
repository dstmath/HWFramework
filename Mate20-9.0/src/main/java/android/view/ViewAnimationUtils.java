package android.view;

import android.animation.Animator;
import android.animation.RevealAnimator;

public final class ViewAnimationUtils {
    private ViewAnimationUtils() {
    }

    public static Animator createCircularReveal(View view, int centerX, int centerY, float startRadius, float endRadius) {
        RevealAnimator revealAnimator = new RevealAnimator(view, centerX, centerY, startRadius, endRadius);
        return revealAnimator;
    }
}
