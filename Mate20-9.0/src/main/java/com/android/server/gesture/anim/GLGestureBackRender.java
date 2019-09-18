package com.android.server.gesture.anim;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import com.android.server.gesture.GestureNavConst;
import com.android.server.gesture.anim.GLGestureBackView;
import com.android.server.gesture.anim.models.GestureBackMetaball;
import com.android.server.gesture.anim.models.GestureBackTexture;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class GLGestureBackRender implements GLSurfaceView.Renderer {
    private static final int FAST_SLIDING_DURATION = 150;
    public static final float FAST_SLIDING_MAX_PROCESS = 0.55f;
    private static final int FAST_SLIDING_STAY_DURATION = 180;
    private static final int SLIDING_STAY_DURATION = 30;
    private static final int SLOW_SLIDING_DISAPPEAR_DURATION = 300;
    private static final String TAG = "GLGestureBackRender";
    private AnimatorListenerAdapter mAnimListener = new AnimatorListenerAdapter() {
        public void onAnimationEnd(Animator animation) {
            super.onAnimationEnd(animation);
            if (GLGestureBackRender.this.mAnimationListener == null) {
                return;
            }
            if (animation == GLGestureBackRender.this.mFastSlidingAnim) {
                GLLogUtils.logD(GLGestureBackRender.TAG, "fast sliding animation end.");
                GLGestureBackRender.this.mAnimationListener.onAnimationEnd(1);
                return;
            }
            GLLogUtils.logD(GLGestureBackRender.TAG, "slow sliding disappear animation end.");
            GLGestureBackRender.this.mAnimationListener.onAnimationEnd(2);
        }
    };
    /* access modifiers changed from: private */
    public GLGestureBackView.GestureBackAnimListener mAnimationListener;
    private Context mContext;
    /* access modifiers changed from: private */
    public AnimatorSet mFastSlidingAnim;
    private GestureBackMetaball mGestureBackMetaBall;
    private GestureBackTexture mGestureBackTexture;
    private GLGestureBackView.ViewSizeChangeListener mViewSizeChangeListener;

    GLGestureBackRender(Context context) {
        this.mContext = context;
        this.mGestureBackTexture = new GestureBackTexture(context);
        this.mGestureBackMetaBall = new GestureBackMetaball();
        initAnimators();
    }

    private void initAnimators() {
        ObjectAnimator proAppear = ObjectAnimator.ofFloat(this, "animProcess", new float[]{GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO, 0.55f});
        proAppear.setDuration(150);
        Interpolator interpolator = AnimationUtils.loadInterpolator(this.mContext, 17563661);
        proAppear.setInterpolator(interpolator);
        ObjectAnimator proDisappear = ObjectAnimator.ofFloat(this, "animProcess", new float[]{0.55f, GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO});
        proDisappear.setDuration(150);
        proDisappear.setInterpolator(interpolator);
        this.mFastSlidingAnim = new AnimatorSet();
        this.mFastSlidingAnim.play(proDisappear).after(proAppear);
        this.mFastSlidingAnim.play(proDisappear).after(180);
        this.mFastSlidingAnim.addListener(this.mAnimListener);
    }

    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES30.glClearColor(GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO, GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO, GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO, GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO);
        this.mGestureBackMetaBall.prepare();
        this.mGestureBackTexture.prepare();
    }

    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLLogUtils.logD(TAG, "onSurfaceChanged width " + width + ", height " + height);
        if (this.mViewSizeChangeListener != null) {
            this.mViewSizeChangeListener.onViewSizeChanged(width, height);
        }
        this.mGestureBackMetaBall.onSurfaceViewChanged(width, height);
        this.mGestureBackTexture.onSurfaceViewChanged(width, height);
    }

    public void onDrawFrame(GL10 gl) {
        GLES30.glClear(16640);
        GLES30.glEnable(3042);
        GLES30.glBlendFunc(770, 771);
        this.mGestureBackMetaBall.drawSelf();
        this.mGestureBackTexture.drawSelf();
        GLES30.glDisable(3042);
    }

    public void setAnimProcess(float process) {
        GLLogUtils.logD(TAG, "setAnimProcess with " + process);
        this.mGestureBackMetaBall.setProcess(process);
        this.mGestureBackTexture.setProcess(process);
    }

    public void setSide(boolean isLeft) {
        GLLogUtils.logD(TAG, "setSide with " + isLeft);
        this.mGestureBackMetaBall.setSide(isLeft);
        this.mGestureBackTexture.setSide(isLeft);
    }

    public void setAnimPosition(float y) {
        GLLogUtils.logD(TAG, "setAnimPosition with " + y);
        this.mGestureBackMetaBall.setCenter(y);
        this.mGestureBackTexture.setCenter((int) y);
    }

    public void setDraw(boolean draw) {
        GLLogUtils.logD(TAG, "setDraw with " + draw);
        this.mGestureBackTexture.setDraw(draw);
        this.mGestureBackMetaBall.setDraw(draw);
    }

    public void addAnimationListener(GLGestureBackView.GestureBackAnimListener listener) {
        this.mAnimationListener = listener;
    }

    public void addViewSizeChangeListener(GLGestureBackView.ViewSizeChangeListener listener) {
        this.mViewSizeChangeListener = listener;
    }

    public void playFastSlidingAnim() {
        GLLogUtils.logD(TAG, "playFastSlidingAnim");
        this.mFastSlidingAnim.start();
    }

    public void playDisappearAnim() {
        GLLogUtils.logD(TAG, "playDisappearAnim");
        float animProcess = this.mGestureBackMetaBall.getProcess();
        AnimatorSet disappearAnimSet = new AnimatorSet();
        ObjectAnimator disappearAnim = ObjectAnimator.ofFloat(this, "animProcess", new float[]{animProcess, 0.0f});
        disappearAnim.setDuration((long) (300.0f * animProcess));
        disappearAnim.setInterpolator(AnimationUtils.loadInterpolator(this.mContext, 17563661));
        disappearAnim.addListener(this.mAnimListener);
        disappearAnimSet.play(disappearAnim).after(30);
        disappearAnimSet.start();
    }
}
