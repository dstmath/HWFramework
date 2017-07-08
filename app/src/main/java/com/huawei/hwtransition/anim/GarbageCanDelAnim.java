package com.huawei.hwtransition.anim;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.view.View;
import android.view.animation.PathInterpolator;

public class GarbageCanDelAnim {
    private static PathInterpolator CURRENT_PAGE_ALPHA_IP;
    private static PathInterpolator CURRENT_PAGE_SCALE_IP;
    private static PathInterpolator CURRENT_PAGE_TRANSLATIONX_IP;
    private static PathInterpolator CURRENT_PAGE_TRANSLATIONY_IP;
    private static float MIN_CURRENT_PAGE_SCLAE;
    private static PathInterpolator NEXT_PAGE_ALPHA_IP;
    private AnimatorSet mCurrentPageAnim;
    private DeleteAnimListerner mDeleteAnimListerner;
    private ObjectAnimator mNextPageAlphaAnim;
    private float mNextPageX;
    private float mNextPageY;

    /* renamed from: com.huawei.hwtransition.anim.GarbageCanDelAnim.1 */
    class AnonymousClass1 extends AnimatorListenerAdapter {
        final /* synthetic */ View val$nextPage;

        AnonymousClass1(View val$nextPage) {
            this.val$nextPage = val$nextPage;
        }

        public void onAnimationStart(Animator animation) {
            super.onAnimationStart(animation);
            if (GarbageCanDelAnim.this.mDeleteAnimListerner != null) {
                GarbageCanDelAnim.this.mDeleteAnimListerner.onAnimStart();
            }
        }

        public void onAnimationEnd(Animator animation) {
            super.onAnimationEnd(animation);
            if (this.val$nextPage != null) {
                this.val$nextPage.setX(GarbageCanDelAnim.this.mNextPageX);
                this.val$nextPage.setY(GarbageCanDelAnim.this.mNextPageY);
            }
            if (GarbageCanDelAnim.this.mDeleteAnimListerner != null) {
                GarbageCanDelAnim.this.mDeleteAnimListerner.onDelete();
            }
        }
    }

    public interface DeleteAnimListerner {
        void onAnimStart();

        void onDelete();
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.hwtransition.anim.GarbageCanDelAnim.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.hwtransition.anim.GarbageCanDelAnim.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.hwtransition.anim.GarbageCanDelAnim.<clinit>():void");
    }

    public GarbageCanDelAnim() {
        this.mCurrentPageAnim = new AnimatorSet();
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
            String str = "translationX";
            ObjectAnimator translationXAnim = ObjectAnimator.ofFloat(currentPage, r18, new float[]{0.0f, translationX});
            translationXAnim.setDuration((long) duration);
            translationXAnim.setInterpolator(CURRENT_PAGE_TRANSLATIONX_IP);
            str = "translationY";
            Animator translationYAnim = ObjectAnimator.ofFloat(currentPage, r18, new float[]{0.0f, translationY});
            translationYAnim.setDuration((long) duration);
            translationYAnim.setInterpolator(CURRENT_PAGE_TRANSLATIONY_IP);
            str = "alpha";
            ObjectAnimator alphaAnim = ObjectAnimator.ofFloat(currentPage, r18, new float[]{1.0f, 0.0f});
            alphaAnim.setDuration((long) duration);
            alphaAnim.setInterpolator(CURRENT_PAGE_ALPHA_IP);
            if (nextPage != null) {
                this.mNextPageX = nextPage.getX();
                this.mNextPageY = nextPage.getY();
                nextPage.setX(oldX);
                nextPage.setY(oldY);
                str = "alpha";
                this.mNextPageAlphaAnim = ObjectAnimator.ofFloat(nextPage, r18, new float[]{0.0f, 1.0f});
                this.mNextPageAlphaAnim.setDuration((long) duration);
                this.mNextPageAlphaAnim.setInterpolator(NEXT_PAGE_ALPHA_IP);
                this.mNextPageAlphaAnim.start();
            }
            this.mCurrentPageAnim.play(translationXAnim).with(translationYAnim).with(scaleAnim).with(alphaAnim);
            this.mCurrentPageAnim.removeAllListeners();
            this.mCurrentPageAnim.addListener(new AnonymousClass1(nextPage));
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
