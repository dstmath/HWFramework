package com.huawei.hwtransition.anim;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.view.View;
import android.view.animation.PathInterpolator;

public class GarbageCanDelAnim {
    private static final PathInterpolator CURRENT_PAGE_ALPHA_IP = new PathInterpolator(CURRENT_PAGE_ALPHA_IP_CONTROL_X_FIRST, CURRENT_PAGE_ALPHA_IP_CONTROL_Y_FIRST, CURRENT_PAGE_ALPHA_IP_CONTROL_X_SECOND, CURRENT_PAGE_ALPHA_IP_CONTROL_Y_SECOND);
    private static final float CURRENT_PAGE_ALPHA_IP_CONTROL_X_FIRST = 0.39f;
    private static final float CURRENT_PAGE_ALPHA_IP_CONTROL_X_SECOND = 0.7f;
    private static final float CURRENT_PAGE_ALPHA_IP_CONTROL_Y_FIRST = 0.17f;
    private static final float CURRENT_PAGE_ALPHA_IP_CONTROL_Y_SECOND = 0.91f;
    private static final PathInterpolator CURRENT_PAGE_SCALE_IP = new PathInterpolator(CURRENT_PAGE_SCALE_IP_CONTROL_X_FIRST, CURRENT_PAGE_SCALE_IP_CONTROL_Y_FIRST, CURRENT_PAGE_SCALE_IP_CONTROL_X_SECOND, CURRENT_PAGE_SCALE_IP_CONTROL_Y_SECOND);
    private static final float CURRENT_PAGE_SCALE_IP_CONTROL_X_FIRST = 0.33f;
    private static final float CURRENT_PAGE_SCALE_IP_CONTROL_X_SECOND = 0.84f;
    private static final float CURRENT_PAGE_SCALE_IP_CONTROL_Y_FIRST = 0.59f;
    private static final float CURRENT_PAGE_SCALE_IP_CONTROL_Y_SECOND = 0.95f;
    private static final PathInterpolator CURRENT_PAGE_TRANSLATIONX_IP = new PathInterpolator(CURRENT_PAGE_TRANSLATIONX_IP_CONTROL_X_FIRST, CURRENT_PAGE_TRANSLATIONX_IP_CONTROL_Y_FIRST, 0.9f, CURRENT_PAGE_TRANSLATIONX_IP_CONTROL_Y_SECOND);
    private static final float CURRENT_PAGE_TRANSLATIONX_IP_CONTROL_X_FIRST = 0.89f;
    private static final float CURRENT_PAGE_TRANSLATIONX_IP_CONTROL_X_SECOND = 0.9f;
    private static final float CURRENT_PAGE_TRANSLATIONX_IP_CONTROL_Y_FIRST = 0.21f;
    private static final float CURRENT_PAGE_TRANSLATIONX_IP_CONTROL_Y_SECOND = 0.96f;
    private static final PathInterpolator CURRENT_PAGE_TRANSLATIONY_IP = new PathInterpolator(0.99f, CURRENT_PAGE_TRANSLATIONY_IP_CONTROL_Y_FIRST, CURRENT_PAGE_TRANSLATIONY_IP_CONTROL_X_SECOND, CURRENT_PAGE_TRANSLATIONY_IP_CONTROL_Y_SECOND);
    private static final float CURRENT_PAGE_TRANSLATIONY_IP_CONTROL_X_FIRST = 0.99f;
    private static final float CURRENT_PAGE_TRANSLATIONY_IP_CONTROL_X_SECOND = 0.88f;
    private static final float CURRENT_PAGE_TRANSLATIONY_IP_CONTROL_Y_FIRST = 0.32f;
    private static final float CURRENT_PAGE_TRANSLATIONY_IP_CONTROL_Y_SECOND = 0.93f;
    private static final float MIN_CURRENT_PAGE_SCLAE = 0.15f;
    private static final PathInterpolator NEXT_PAGE_ALPHA_IP = new PathInterpolator(0.99f, NEXT_PAGE_ALPHA_IP_CONTROL_Y_FIRST, 0.9f, NEXT_PAGE_ALPHA_IP_CONTROL_Y_SECOND);
    private static final float NEXT_PAGE_ALPHA_IP_CONTROL_X_FIRST = 0.99f;
    private static final float NEXT_PAGE_ALPHA_IP_CONTROL_X_SECOND = 0.9f;
    private static final float NEXT_PAGE_ALPHA_IP_CONTROL_Y_FIRST = -0.01f;
    private static final float NEXT_PAGE_ALPHA_IP_CONTROL_Y_SECOND = 1.01f;
    private AnimatorSet mCurrentPageAnims = new AnimatorSet();
    private DeleteAnimListerner mDeleteAnimListerner;
    private ObjectAnimator mNextPageAlphaAnim;
    private float mNextPageX;
    private float mNextPageY;

    public interface DeleteAnimListerner {
        void onAnimStart();

        void onDelete();
    }

    public void startDeleteAnim(View currentPage, View nextPage, float valueX, float valueY, int duration) {
        resetAnim();
        if (currentPage != null) {
            ObjectAnimator scaleAnim = ObjectAnimator.ofPropertyValuesHolder(currentPage, PropertyValuesHolder.ofFloat("scaleX", 0.15f), PropertyValuesHolder.ofFloat("scaleY", 0.15f));
            scaleAnim.setDuration((long) duration);
            scaleAnim.setInterpolator(CURRENT_PAGE_SCALE_IP);
            ObjectAnimator translationAnimatorX = ObjectAnimator.ofFloat(currentPage, "translationX", 0.0f, valueX - (((float) currentPage.getWidth()) / 2.0f));
            translationAnimatorX.setDuration((long) duration);
            translationAnimatorX.setInterpolator(CURRENT_PAGE_TRANSLATIONX_IP);
            ObjectAnimator translationAnimatorY = ObjectAnimator.ofFloat(currentPage, "translationY", 0.0f, valueY - (((float) currentPage.getHeight()) / 2.0f));
            translationAnimatorY.setDuration((long) duration);
            translationAnimatorY.setInterpolator(CURRENT_PAGE_TRANSLATIONY_IP);
            ObjectAnimator alphaAnim = ObjectAnimator.ofFloat(currentPage, "alpha", 1.0f, 0.0f);
            alphaAnim.setDuration((long) duration);
            alphaAnim.setInterpolator(CURRENT_PAGE_ALPHA_IP);
            if (nextPage != null) {
                this.mNextPageX = nextPage.getX();
                this.mNextPageY = nextPage.getY();
                float oldX = currentPage.getX();
                float oldY = currentPage.getY();
                nextPage.setX(oldX);
                nextPage.setY(oldY);
                this.mNextPageAlphaAnim = ObjectAnimator.ofFloat(nextPage, "alpha", 0.0f, 1.0f);
                this.mNextPageAlphaAnim.setDuration((long) duration);
                this.mNextPageAlphaAnim.setInterpolator(NEXT_PAGE_ALPHA_IP);
                this.mNextPageAlphaAnim.start();
            }
            this.mCurrentPageAnims.play(translationAnimatorX).with(translationAnimatorY).with(scaleAnim).with(alphaAnim);
            this.mCurrentPageAnims.removeAllListeners();
            addListener(nextPage);
            this.mCurrentPageAnims.start();
        }
    }

    private void addListener(final View nextPage) {
        this.mCurrentPageAnims.addListener(new AnimatorListenerAdapter() {
            /* class com.huawei.hwtransition.anim.GarbageCanDelAnim.AnonymousClass1 */

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationStart(Animator animation) {
                super.onAnimationStart(animation);
                if (GarbageCanDelAnim.this.mDeleteAnimListerner != null) {
                    GarbageCanDelAnim.this.mDeleteAnimListerner.onAnimStart();
                }
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                if (nextPage != null) {
                    nextPage.setX(GarbageCanDelAnim.this.mNextPageX);
                    nextPage.setY(GarbageCanDelAnim.this.mNextPageY);
                }
                if (GarbageCanDelAnim.this.mDeleteAnimListerner != null) {
                    GarbageCanDelAnim.this.mDeleteAnimListerner.onDelete();
                }
            }
        });
    }

    private void resetAnim() {
        cancleAnim(this.mCurrentPageAnims);
        cancleAnim(this.mNextPageAlphaAnim);
        this.mNextPageX = 0.0f;
        this.mNextPageY = 0.0f;
    }

    private void cancleAnim(Animator animator) {
        if (animator != null) {
            animator.cancel();
        }
    }

    public void setDeleteAnimListerner(DeleteAnimListerner deleteAnimListerner) {
        this.mDeleteAnimListerner = deleteAnimListerner;
    }
}
