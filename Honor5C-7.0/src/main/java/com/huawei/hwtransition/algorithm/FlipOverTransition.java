package com.huawei.hwtransition.algorithm;

import android.animation.TimeInterpolator;
import android.graphics.Camera;
import android.graphics.Matrix;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import com.huawei.hwtransition.AlgorithmUtil;
import com.huawei.hwtransition.interpolator.ZInterpolator;

public class FlipOverTransition extends BaseTransition {
    private static final float LEFTSCREEN_INTERPOLATOR_FACTOR = 1.5f;
    private static final int LEFT_PAGE_ANGLE = 80;
    private static final float OVERLAY_FACTOR = 0.0f;
    private static final float RIGHTSCREEN_INTERPOLATOR_FACTOR = 1.5f;
    private static final int RIGHT_PAGE_ANGLE = 45;
    private static final float SCALE_INTERPOLATOR_FACTOR = 0.5f;
    private static final float TRANSITION_SCALE_FACTOR = 0.7f;
    private TimeInterpolator mLeftScreenAlphaInterpolator;
    private TimeInterpolator mLeftScreenInterpolator;
    private TimeInterpolator mLeftScreenScaleInterpolator;
    private TimeInterpolator mRightScreenInterpolator;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.hwtransition.algorithm.FlipOverTransition.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.hwtransition.algorithm.FlipOverTransition.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.hwtransition.algorithm.FlipOverTransition.<clinit>():void");
    }

    public FlipOverTransition() {
        this.mAnimationType = "3D";
        this.mLeftScreenInterpolator = new AccelerateInterpolator(RIGHTSCREEN_INTERPOLATOR_FACTOR);
        this.mLeftScreenAlphaInterpolator = new DecelerateInterpolator();
        this.mRightScreenInterpolator = new DecelerateInterpolator(RIGHTSCREEN_INTERPOLATOR_FACTOR);
        this.mLeftScreenScaleInterpolator = new ZInterpolator(SCALE_INTERPOLATOR_FACTOR);
        if (this.mTransformationInfo.mCamera == null) {
            this.mTransformationInfo.mCamera = new Camera();
            this.mTransformationInfo.matrix3D = new Matrix();
        }
    }

    public boolean transform(int part, boolean isOverScrollFirst, boolean isOverScrollLast, float scrollProgress, View child) {
        float angle;
        float pivotX;
        float pivotY;
        float translationX;
        float alpha;
        float scale;
        float interpolatedProgress;
        if (scrollProgress < OVERLAY_FACTOR) {
            interpolatedProgress = this.mRightScreenInterpolator.getInterpolation(Math.abs(scrollProgress));
            angle = (-1.0f * interpolatedProgress) * 45.0f;
            pivotX = ((float) child.getMeasuredWidth()) / 2.0f;
            pivotY = ((float) child.getMeasuredHeight()) / 2.0f;
            translationX = (((-1.0f * interpolatedProgress) * ((float) child.getMeasuredWidth())) / 2.0f) * OVERLAY_FACTOR;
            alpha = 1.0f;
            scale = 1.0f;
        } else {
            pivotX = OVERLAY_FACTOR;
            pivotY = ((float) child.getMeasuredHeight()) / 2.0f;
            angle = this.mLeftScreenInterpolator.getInterpolation(Math.abs(scrollProgress)) * 80.0f;
            translationX = (((float) child.getMeasuredWidth()) * scrollProgress) * child.getScaleX();
            interpolatedProgress = this.mLeftScreenScaleInterpolator.getInterpolation(Math.abs(scrollProgress));
            scale = (1.0f - interpolatedProgress) + (TRANSITION_SCALE_FACTOR * interpolatedProgress);
            alpha = this.mLeftScreenAlphaInterpolator.getInterpolation(1.0f - Math.abs(scrollProgress));
        }
        this.mTransformationInfo.mPivotX = AlgorithmUtil.transformPivotX(child, pivotX);
        this.mTransformationInfo.mPivotY = AlgorithmUtil.transformPivotY(child, pivotY);
        this.mTransformationInfo.mRotationY = angle;
        this.mTransformationInfo.mScaleX = scale;
        this.mTransformationInfo.mScaleY = scale;
        this.mTransformationInfo.mMatrixDirty = true;
        this.mTransformationInfo.mAlpha = alpha;
        this.mTransformationInfo.mAlphaDirty = true;
        if (this.mLayout_type != 0) {
            translationX += ((((float) child.getMeasuredWidth()) * scrollProgress) * child.getScaleX()) * -1.0f;
        } else if (isOverScrollFirst) {
            translationX = ((-scrollProgress) * ((float) child.getMeasuredWidth())) * child.getScaleX();
        } else if (isOverScrollLast) {
            translationX = OVERLAY_FACTOR;
        }
        this.mTransformationInfo.mTranslationX = translationX;
        return true;
    }
}
