package com.android.server.gesture.anim;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.view.animation.AnimationUtils;
import android.view.animation.Interpolator;
import android.view.animation.PathInterpolator;
import com.android.server.gesture.GestureNavConst;
import com.android.server.gesture.anim.GLGestureBackView;
import com.android.server.gesture.anim.models.GestureBackMetaball;
import com.android.server.gesture.anim.models.GestureBackTexture;
import com.huawei.utils.HwPartResourceUtils;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class GLGestureBackRender implements GLSurfaceView.Renderer {
    private static final String ANIMATION_PROCESS = "animProcess";
    private static final float DEFAULT_CONTROLX2 = 0.2f;
    private static final float DEFAULT_CONTROLY1 = 0.5f;
    private static final long DOCK_IN_ANIM_DURATION = 150;
    private static final long DOCK_OUT_ANIM_DURATION = 150;
    private static final int FAST_SLIDING_DURATION = 150;
    public static final float FAST_SLIDING_MAX_PROCESS = 0.55f;
    private static final int FAST_SLIDING_STAY_DURATION = 180;
    private static final float MAX_SCALE_VALUE = 1.0f;
    private static final float MIN_SCALE_VALUE = 0.9f;
    private static final long SCATTER_PROCESS_DURATION = 150;
    private static final int SLIDING_STAY_DURATION = 30;
    private static final int SLOW_SLIDING_DISAPPEAR_DURATION = 300;
    private static final String TAG = "GLGestureBackRender";
    private AnimatorListenerAdapter mAnimListener = new AnimatorListenerAdapter() {
        /* class com.android.server.gesture.anim.GLGestureBackRender.AnonymousClass1 */

        @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
        public void onAnimationEnd(Animator animation) {
            super.onAnimationEnd(animation);
            if (GLGestureBackRender.this.mDockAnimatorSet != null && GLGestureBackRender.this.mDockAnimatorSet.isRunning()) {
                GLGestureBackRender.this.mDockAnimatorSet.end();
            }
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
    private GLGestureBackView.GestureBackAnimListener mAnimationListener;
    private Context mContext;
    private AnimatorSet mDockAnimatorSet;
    private TimeInterpolator mDockInInterpolator;
    private TimeInterpolator mDockOutInterpolator;
    private AnimatorSet mFastSlidingAnim;
    private GestureBackMetaball mGestureBackMetaBall;
    private GestureBackTexture mGestureBackTexture;
    private GestureBackTexture mGestureBackTextureDock;
    private boolean mIsDockMode = false;
    private boolean mIsShowDockIcon = false;
    private ObjectAnimator mScatterProcessAnimator;
    private GLGestureBackView.ViewSizeChangeListener mViewSizeChangeListener;

    GLGestureBackRender(Context context) {
        GLLogUtils.logD(TAG, "GLGestureBackRender start");
        this.mContext = context;
        this.mGestureBackTexture = new GestureBackTexture(context, HwPartResourceUtils.getResourceId("gesture_nav_back_anim"));
        this.mGestureBackMetaBall = new GestureBackMetaball();
        if (GestureNavConst.SUPPORT_DOCK_TRIGGER) {
            this.mIsDockMode = true;
            this.mGestureBackTextureDock = new GestureBackTexture(context, HwPartResourceUtils.getResourceId("ic_dock_app"));
        }
        initAnimators();
    }

    private void initAnimators() {
        ObjectAnimator proAppear = ObjectAnimator.ofFloat(this, ANIMATION_PROCESS, GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO, 0.55f);
        proAppear.setDuration(150L);
        Interpolator interpolator = AnimationUtils.loadInterpolator(this.mContext, 17563661);
        proAppear.setInterpolator(interpolator);
        ObjectAnimator proDisappear = ObjectAnimator.ofFloat(this, ANIMATION_PROCESS, 0.55f, GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO);
        proDisappear.setDuration(150L);
        proDisappear.setInterpolator(interpolator);
        this.mFastSlidingAnim = new AnimatorSet();
        this.mFastSlidingAnim.play(proDisappear).after(proAppear);
        this.mFastSlidingAnim.play(proDisappear).after(180);
        this.mFastSlidingAnim.addListener(this.mAnimListener);
    }

    @Override // android.opengl.GLSurfaceView.Renderer
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES30.glClearColor(GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO, GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO, GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO, GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO);
        setAnimPosition();
        this.mGestureBackMetaBall.prepare();
        this.mGestureBackTexture.prepare();
        if (this.mIsDockMode) {
            this.mGestureBackTextureDock.prepare();
        }
    }

    @Override // android.opengl.GLSurfaceView.Renderer
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLLogUtils.logD(TAG, "onSurfaceChanged width " + width + ", height " + height);
        setAnimPosition();
        GLGestureBackView.ViewSizeChangeListener viewSizeChangeListener = this.mViewSizeChangeListener;
        if (viewSizeChangeListener != null) {
            viewSizeChangeListener.onViewSizeChanged(width, height);
        }
        this.mGestureBackMetaBall.onSurfaceViewChanged(width, height);
        this.mGestureBackTexture.onSurfaceViewChanged(width, height);
        if (this.mIsDockMode) {
            this.mGestureBackTextureDock.onSurfaceViewChanged(width, height);
        }
    }

    @Override // android.opengl.GLSurfaceView.Renderer
    public void onDrawFrame(GL10 gl) {
        GLES30.glClear(16640);
        GLES30.glEnable(3042);
        GLES30.glBlendFunc(770, 771);
        this.mGestureBackMetaBall.drawSelf();
        this.mGestureBackTexture.drawSelf();
        if (this.mIsDockMode) {
            this.mGestureBackTextureDock.drawSelf();
        }
        GLES30.glDisable(3042);
    }

    public void setAnimProcess(float process) {
        GLLogUtils.logD(TAG, "setAnimProcess with " + process);
        this.mGestureBackMetaBall.setProcess(process);
        this.mGestureBackTexture.setProcess(process);
        if (this.mIsDockMode) {
            this.mGestureBackTextureDock.setProcess(process);
        }
    }

    /* access modifiers changed from: protected */
    public boolean isScatterProcessAnimRunning() {
        ObjectAnimator objectAnimator = this.mScatterProcessAnimator;
        return objectAnimator != null && objectAnimator.isRunning();
    }

    @SuppressLint({"NewApi"})
    public void playScatterProcessAnim(float fromProcess, float toProcess) {
        ObjectAnimator objectAnimator = this.mScatterProcessAnimator;
        if (objectAnimator == null || !objectAnimator.isRunning()) {
            this.mScatterProcessAnimator = ObjectAnimator.ofFloat(this, ANIMATION_PROCESS, fromProcess, toProcess);
            this.mScatterProcessAnimator.setDuration((long) ((toProcess - fromProcess) * 150.0f));
            this.mScatterProcessAnimator.setInterpolator(new PathInterpolator(GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO, DEFAULT_CONTROLY1, DEFAULT_CONTROLX2, 1.0f));
            GLLogUtils.logD(TAG, "start scatter process: from = " + fromProcess + ", to = " + toProcess);
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.play(this.mScatterProcessAnimator);
            animatorSet.start();
            return;
        }
        GLLogUtils.logD(TAG, "scatter process animator isRunning = " + this.mScatterProcessAnimator.isRunning());
    }

    public void setSide(boolean isLeft) {
        GLLogUtils.logD(TAG, "setSide with " + isLeft);
        this.mGestureBackMetaBall.setSide(isLeft);
        this.mGestureBackTexture.setSide(isLeft);
        if (this.mIsDockMode) {
            this.mGestureBackTextureDock.setSide(isLeft);
        }
    }

    public void setNightMode(boolean isNightMode) {
        GLLogUtils.logD(TAG, "setNightMode with " + isNightMode);
        this.mGestureBackMetaBall.setNightMode(isNightMode);
    }

    public void setAnimPosition() {
        GLLogUtils.logD(TAG, "setAnimPosition");
        this.mGestureBackMetaBall.setCenter();
        this.mGestureBackTexture.setCenter();
        if (this.mIsDockMode) {
            this.mGestureBackTextureDock.setCenter();
        }
    }

    public void setDraw(boolean isDraw) {
        GLLogUtils.logD(TAG, "setDraw with " + isDraw);
        boolean z = true;
        this.mGestureBackTexture.setDraw(isDraw && !this.mIsShowDockIcon);
        if (this.mIsDockMode) {
            GestureBackTexture gestureBackTexture = this.mGestureBackTextureDock;
            if (!isDraw || !this.mIsShowDockIcon) {
                z = false;
            }
            gestureBackTexture.setDraw(z);
        }
        this.mGestureBackMetaBall.setDraw(isDraw);
    }

    public void setDockIcon(boolean isShowDockIcon) {
        this.mIsShowDockIcon = isShowDockIcon;
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
        ObjectAnimator disappearAnim = ObjectAnimator.ofFloat(this, ANIMATION_PROCESS, animProcess, 0.0f);
        disappearAnim.setDuration((long) (300.0f * animProcess));
        disappearAnim.setInterpolator(AnimationUtils.loadInterpolator(this.mContext, 17563661));
        disappearAnim.addListener(this.mAnimListener);
        AnimatorSet disappearAnimSet = new AnimatorSet();
        disappearAnimSet.play(disappearAnim).after(30);
        disappearAnimSet.start();
    }

    public void switchDockIcon(boolean isSlideIn) {
        GLLogUtils.logD(TAG, "start switchDockIcon method.isSlideIn:" + isSlideIn);
        AnimatorSet animatorSet = this.mDockAnimatorSet;
        if (animatorSet != null && animatorSet.isRunning()) {
            this.mDockAnimatorSet.end();
        }
        this.mDockAnimatorSet = new AnimatorSet();
        if (this.mGestureBackTexture != null && this.mGestureBackTextureDock != null) {
            ValueAnimator showOutAlphaAnimator = ValueAnimator.ofFloat(1.0f, GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO);
            ValueAnimator showInAlphaAnimator = ValueAnimator.ofFloat(GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO, 1.0f);
            if (isSlideIn) {
                this.mGestureBackTextureDock.setDockAlpha(GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO);
                this.mGestureBackTextureDock.setScaleRate(MIN_SCALE_VALUE);
                this.mGestureBackTextureDock.setDraw(true);
                showOutAlphaAnimator.addUpdateListener(getAlphaUpdateListener(this.mGestureBackTexture));
                showInAlphaAnimator.addUpdateListener(getAlphaUpdateListener(this.mGestureBackTextureDock));
                ValueAnimator showInScaleAnimator = ValueAnimator.ofFloat(MIN_SCALE_VALUE, 1.0f);
                showInScaleAnimator.addUpdateListener(getScaleUpdateListener(this.mGestureBackTextureDock));
                this.mDockAnimatorSet.playTogether(showInAlphaAnimator, showInScaleAnimator, showOutAlphaAnimator);
                this.mDockInInterpolator = AnimationUtils.loadInterpolator(this.mContext, HwPartResourceUtils.getResourceId("cubic_bezier_interpolator_type_33_33"));
                this.mDockAnimatorSet.setInterpolator(this.mDockInInterpolator);
                this.mDockAnimatorSet.setDuration(150L);
            } else {
                this.mGestureBackTexture.setDockAlpha(GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO);
                this.mGestureBackTexture.setScaleRate(1.0f);
                this.mGestureBackTexture.setDraw(true);
                showOutAlphaAnimator.addUpdateListener(getAlphaUpdateListener(this.mGestureBackTextureDock));
                showInAlphaAnimator.addUpdateListener(getAlphaUpdateListener(this.mGestureBackTexture));
                this.mDockAnimatorSet.playTogether(showInAlphaAnimator, showOutAlphaAnimator);
                this.mDockOutInterpolator = AnimationUtils.loadInterpolator(this.mContext, HwPartResourceUtils.getResourceId("cubic_bezier_interpolator_type_33_33"));
                this.mDockAnimatorSet.setInterpolator(this.mDockOutInterpolator);
                this.mDockAnimatorSet.setDuration(150L);
            }
            this.mDockAnimatorSet.start();
        }
    }

    private ValueAnimator.AnimatorUpdateListener getAlphaUpdateListener(final GestureBackTexture gbTexture) {
        return new ValueAnimator.AnimatorUpdateListener() {
            /* class com.android.server.gesture.anim.GLGestureBackRender.AnonymousClass2 */

            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator animation) {
                if (animation.getAnimatedValue() instanceof Float) {
                    gbTexture.setDockAlpha(((Float) animation.getAnimatedValue()).floatValue());
                }
            }
        };
    }

    private ValueAnimator.AnimatorUpdateListener getScaleUpdateListener(final GestureBackTexture gbTexture) {
        return new ValueAnimator.AnimatorUpdateListener() {
            /* class com.android.server.gesture.anim.GLGestureBackRender.AnonymousClass3 */

            @Override // android.animation.ValueAnimator.AnimatorUpdateListener
            public void onAnimationUpdate(ValueAnimator animation) {
                if (animation.getAnimatedValue() instanceof Float) {
                    gbTexture.setScaleRate(((Float) animation.getAnimatedValue()).floatValue());
                }
            }
        };
    }

    public void endAnimation() {
        GLLogUtils.logD(TAG, "start endAnimation method." + this.mIsDockMode);
        this.mGestureBackTexture.setDockAlpha(1.0f);
        this.mGestureBackTexture.setScaleRate(1.0f);
        if (this.mIsDockMode) {
            this.mGestureBackTextureDock.setDockAlpha(1.0f);
            this.mGestureBackTextureDock.setScaleRate(1.0f);
        }
    }

    public void setHwSize(int width, int height) {
        this.mGestureBackMetaBall.setHwSize(width, height);
        this.mGestureBackTexture.setHwSize(width, height);
        if (this.mIsDockMode) {
            this.mGestureBackTextureDock.setHwSize(width, height);
        }
    }

    public void setStartPosition(int startX) {
        GLLogUtils.logD(TAG, "setStartPosition" + startX);
        this.mGestureBackMetaBall.setStartPosition(startX);
        this.mGestureBackTexture.setStartPosition(startX);
        if (this.mIsDockMode) {
            this.mGestureBackTextureDock.setStartPosition(startX);
        }
    }

    public void setStartPositionOffset(float offsetRatio) {
        GLLogUtils.logD(TAG, "setStartPositionOffset" + offsetRatio);
        this.mGestureBackMetaBall.setStartPositionOffset(offsetRatio);
        this.mGestureBackTexture.setStartPositionOffset(offsetRatio);
        if (this.mIsDockMode) {
            this.mGestureBackTextureDock.setStartPositionOffset(offsetRatio);
        }
    }
}
