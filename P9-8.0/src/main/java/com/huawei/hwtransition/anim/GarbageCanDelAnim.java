package com.huawei.hwtransition.anim;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.view.View;
import android.view.animation.PathInterpolator;

public class GarbageCanDelAnim {
    private static PathInterpolator CURRENT_PAGE_ALPHA_IP = new PathInterpolator(0.39f, 0.17f, 0.7f, 0.91f);
    private static PathInterpolator CURRENT_PAGE_SCALE_IP = new PathInterpolator(0.33f, 0.59f, 0.84f, 0.95f);
    private static PathInterpolator CURRENT_PAGE_TRANSLATIONX_IP = new PathInterpolator(0.89f, 0.21f, 0.9f, 0.96f);
    private static PathInterpolator CURRENT_PAGE_TRANSLATIONY_IP = new PathInterpolator(0.99f, 0.32f, 0.88f, 0.93f);
    private static float MIN_CURRENT_PAGE_SCLAE = 0.15f;
    private static PathInterpolator NEXT_PAGE_ALPHA_IP = new PathInterpolator(0.99f, -0.01f, 0.9f, 1.01f);
    private AnimatorSet mCurrentPageAnim = new AnimatorSet();
    private DeleteAnimListerner mDeleteAnimListerner;
    private ObjectAnimator mNextPageAlphaAnim;
    private float mNextPageX;
    private float mNextPageY;

    public interface DeleteAnimListerner {
        void onAnimStart();

        void onDelete();
    }

    public void startDeleteAnim(View currentPage, View nextPage, float x, float y, int duration) {
        resetAnim();
        if (currentPage != null) {
            float oldX = currentPage.getX();
            float oldY = currentPage.getY();
            float translationY = y - (((float) currentPage.getHeight()) / 2.0f);
            float translationX = x - (((float) currentPage.getWidth()) / 2.0f);
            PropertyValuesHolder scaleXPvh = PropertyValuesHolder.ofFloat("scaleX", new float[]{MIN_CURRENT_PAGE_SCLAE});
            PropertyValuesHolder scaleYPvh = PropertyValuesHolder.ofFloat("scaleY", new float[]{MIN_CURRENT_PAGE_SCLAE});
            ObjectAnimator scaleAnim = ObjectAnimator.ofPropertyValuesHolder(currentPage, new PropertyValuesHolder[]{scaleXPvh, scaleYPvh});
            scaleAnim.setDuration((long) duration);
            scaleAnim.setInterpolator(CURRENT_PAGE_SCALE_IP);
            ObjectAnimator translationXAnim = ObjectAnimator.ofFloat(currentPage, "translationX", new float[]{0.0f, translationX});
            translationXAnim.setDuration((long) duration);
            translationXAnim.setInterpolator(CURRENT_PAGE_TRANSLATIONX_IP);
            Animator translationYAnim = ObjectAnimator.ofFloat(currentPage, "translationY", new float[]{0.0f, translationY});
            translationYAnim.setDuration((long) duration);
            translationYAnim.setInterpolator(CURRENT_PAGE_TRANSLATIONY_IP);
            ObjectAnimator alphaAnim = ObjectAnimator.ofFloat(currentPage, "alpha", new float[]{1.0f, 0.0f});
            alphaAnim.setDuration((long) duration);
            alphaAnim.setInterpolator(CURRENT_PAGE_ALPHA_IP);
            if (nextPage != null) {
                this.mNextPageX = nextPage.getX();
                this.mNextPageY = nextPage.getY();
                nextPage.setX(oldX);
                nextPage.setY(oldY);
                this.mNextPageAlphaAnim = ObjectAnimator.ofFloat(nextPage, "alpha", new float[]{0.0f, 1.0f});
                this.mNextPageAlphaAnim.setDuration((long) duration);
                this.mNextPageAlphaAnim.setInterpolator(NEXT_PAGE_ALPHA_IP);
                this.mNextPageAlphaAnim.start();
            }
            this.mCurrentPageAnim.play(translationXAnim).with(translationYAnim).with(scaleAnim).with(alphaAnim);
            this.mCurrentPageAnim.removeAllListeners();
            final View view = nextPage;
            this.mCurrentPageAnim.addListener(new AnimatorListenerAdapter() {
                public void onAnimationStart(Animator animation) {
                    super.onAnimationStart(animation);
                    if (GarbageCanDelAnim.this.mDeleteAnimListerner != null) {
                        GarbageCanDelAnim.this.mDeleteAnimListerner.onAnimStart();
                    }
                }

                public void onAnimationEnd(Animator animation) {
                    super.onAnimationEnd(animation);
                    if (view != null) {
                        view.setX(GarbageCanDelAnim.this.mNextPageX);
                        view.setY(GarbageCanDelAnim.this.mNextPageY);
                    }
                    if (GarbageCanDelAnim.this.mDeleteAnimListerner != null) {
                        GarbageCanDelAnim.this.mDeleteAnimListerner.onDelete();
                    }
                }
            });
            this.mCurrentPageAnim.start();
        }
    }

    private void resetAnim() {
        cancleAnim(this.mCurrentPageAnim);
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
