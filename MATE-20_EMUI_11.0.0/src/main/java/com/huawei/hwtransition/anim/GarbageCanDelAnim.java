package com.huawei.hwtransition.anim;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.view.View;
import android.view.animation.PathInterpolator;

public class GarbageCanDelAnim {
    private static final PathInterpolator CURRENT_PAGE_ALPHA_IP = new PathInterpolator(0.39f, 0.17f, 0.7f, 0.91f);
    private static final PathInterpolator CURRENT_PAGE_SCALE_IP = new PathInterpolator(0.33f, 0.59f, 0.84f, 0.95f);
    private static final PathInterpolator CURRENT_PAGE_TRANSLATIONX_IP = new PathInterpolator(0.89f, 0.21f, 0.9f, 0.96f);
    private static final PathInterpolator CURRENT_PAGE_TRANSLATIONY_IP = new PathInterpolator(0.99f, 0.32f, 0.88f, 0.93f);
    private static final float MIN_CURRENT_PAGE_SCLAE = 0.15f;
    private static final PathInterpolator NEXT_PAGE_ALPHA_IP = new PathInterpolator(0.99f, -0.01f, 0.9f, 1.01f);
    private AnimatorSet mCurrentPageAnims = new AnimatorSet();
    private DeleteAnimListerner mDeleteAnimListerner;
    private ObjectAnimator mNextPageAlphaAnim;
    private float mNextPageX;
    private float mNextPageY;

    public interface DeleteAnimListerner {
        void onAnimStart();

        void onDelete();
    }

    public void startDeleteAnim(View currentPage, final View nextPage, float x, float y, int duration) {
        resetAnim();
        if (currentPage != null) {
            float oldX = currentPage.getX();
            float oldY = currentPage.getY();
            int height = currentPage.getHeight();
            ObjectAnimator scaleAnim = ObjectAnimator.ofPropertyValuesHolder(currentPage, PropertyValuesHolder.ofFloat("scaleX", 0.15f), PropertyValuesHolder.ofFloat("scaleY", 0.15f));
            scaleAnim.setDuration((long) duration);
            scaleAnim.setInterpolator(CURRENT_PAGE_SCALE_IP);
            ObjectAnimator translationXAnim = ObjectAnimator.ofFloat(currentPage, "translationX", 0.0f, x - (((float) currentPage.getWidth()) / 2.0f));
            translationXAnim.setDuration((long) duration);
            translationXAnim.setInterpolator(CURRENT_PAGE_TRANSLATIONX_IP);
            ObjectAnimator translationYAnim = ObjectAnimator.ofFloat(currentPage, "translationY", 0.0f, y - (((float) height) / 2.0f));
            translationYAnim.setDuration((long) duration);
            translationYAnim.setInterpolator(CURRENT_PAGE_TRANSLATIONY_IP);
            ObjectAnimator alphaAnim = ObjectAnimator.ofFloat(currentPage, "alpha", 1.0f, 0.0f);
            alphaAnim.setDuration((long) duration);
            alphaAnim.setInterpolator(CURRENT_PAGE_ALPHA_IP);
            if (nextPage != null) {
                this.mNextPageX = nextPage.getX();
                this.mNextPageY = nextPage.getY();
                nextPage.setX(oldX);
                nextPage.setY(oldY);
                this.mNextPageAlphaAnim = ObjectAnimator.ofFloat(nextPage, "alpha", 0.0f, 1.0f);
                this.mNextPageAlphaAnim.setDuration((long) duration);
                this.mNextPageAlphaAnim.setInterpolator(NEXT_PAGE_ALPHA_IP);
                this.mNextPageAlphaAnim.start();
            }
            this.mCurrentPageAnims.play(translationXAnim).with(translationYAnim).with(scaleAnim).with(alphaAnim);
            this.mCurrentPageAnims.removeAllListeners();
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
            this.mCurrentPageAnims.start();
        }
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
