package android.transition;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.graphics.Path;
import android.transition.Transition.TransitionListener;
import android.view.View;
import com.android.internal.R;

class TranslationAnimationCreator {

    private static class TransitionPositionListener extends AnimatorListenerAdapter implements TransitionListener {
        private final View mMovingView;
        private float mPausedX;
        private float mPausedY;
        private final int mStartX;
        private final int mStartY;
        private final float mTerminalX;
        private final float mTerminalY;
        private int[] mTransitionPosition;
        private final View mViewInHierarchy;

        /* synthetic */ TransitionPositionListener(View movingView, View viewInHierarchy, int startX, int startY, float terminalX, float terminalY, TransitionPositionListener -this6) {
            this(movingView, viewInHierarchy, startX, startY, terminalX, terminalY);
        }

        private TransitionPositionListener(View movingView, View viewInHierarchy, int startX, int startY, float terminalX, float terminalY) {
            this.mMovingView = movingView;
            this.mViewInHierarchy = viewInHierarchy;
            this.mStartX = startX - Math.round(this.mMovingView.getTranslationX());
            this.mStartY = startY - Math.round(this.mMovingView.getTranslationY());
            this.mTerminalX = terminalX;
            this.mTerminalY = terminalY;
            this.mTransitionPosition = (int[]) this.mViewInHierarchy.getTag(R.id.transitionPosition);
            if (this.mTransitionPosition != null) {
                this.mViewInHierarchy.setTagInternal(R.id.transitionPosition, null);
            }
        }

        public void onAnimationCancel(Animator animation) {
            if (this.mTransitionPosition == null) {
                this.mTransitionPosition = new int[2];
            }
            this.mTransitionPosition[0] = Math.round(((float) this.mStartX) + this.mMovingView.getTranslationX());
            this.mTransitionPosition[1] = Math.round(((float) this.mStartY) + this.mMovingView.getTranslationY());
            this.mViewInHierarchy.setTagInternal(R.id.transitionPosition, this.mTransitionPosition);
        }

        public void onAnimationEnd(Animator animator) {
        }

        public void onAnimationPause(Animator animator) {
            this.mPausedX = this.mMovingView.getTranslationX();
            this.mPausedY = this.mMovingView.getTranslationY();
            this.mMovingView.setTranslationX(this.mTerminalX);
            this.mMovingView.setTranslationY(this.mTerminalY);
        }

        public void onAnimationResume(Animator animator) {
            this.mMovingView.setTranslationX(this.mPausedX);
            this.mMovingView.setTranslationY(this.mPausedY);
        }

        public void onTransitionStart(Transition transition) {
        }

        public void onTransitionEnd(Transition transition) {
            this.mMovingView.setTranslationX(this.mTerminalX);
            this.mMovingView.setTranslationY(this.mTerminalY);
            transition.removeListener(this);
        }

        public void onTransitionCancel(Transition transition) {
        }

        public void onTransitionPause(Transition transition) {
        }

        public void onTransitionResume(Transition transition) {
        }
    }

    TranslationAnimationCreator() {
    }

    static Animator createAnimation(View view, TransitionValues values, int viewPosX, int viewPosY, float startX, float startY, float endX, float endY, TimeInterpolator interpolator, Transition transition) {
        float terminalX = view.getTranslationX();
        float terminalY = view.getTranslationY();
        int[] startPosition = (int[]) values.view.getTag(R.id.transitionPosition);
        if (startPosition != null) {
            startX = ((float) (startPosition[0] - viewPosX)) + terminalX;
            startY = ((float) (startPosition[1] - viewPosY)) + terminalY;
        }
        int startPosX = viewPosX + Math.round(startX - terminalX);
        int startPosY = viewPosY + Math.round(startY - terminalY);
        view.setTranslationX(startX);
        view.setTranslationY(startY);
        if (startX == endX && startY == endY) {
            return null;
        }
        Path path = new Path();
        path.moveTo(startX, startY);
        path.lineTo(endX, endY);
        ObjectAnimator anim = ObjectAnimator.ofFloat(view, View.TRANSLATION_X, View.TRANSLATION_Y, path);
        TransitionPositionListener listener = new TransitionPositionListener(view, values.view, startPosX, startPosY, terminalX, terminalY, null);
        transition.addListener(listener);
        anim.addListener(listener);
        anim.addPauseListener(listener);
        anim.setInterpolator(interpolator);
        return anim;
    }
}
