package com.android.server.fingerprint.fingerprintAnimation;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.animation.TimeInterpolator;
import android.content.Context;
import android.opengl.GLES30;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.SystemClock;
import android.util.Log;
import android.view.animation.PathInterpolator;
import com.android.server.gesture.GestureNavConst;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class WaterEffectRender implements GLSurfaceView.Renderer {
    private static final long ANIM_DURATION = 200;
    private static final long ANIM_IN_DURATION = 200;
    private static final long ANIM_OUT_DURATION = 1000;
    private static final int A_POSITION_LOCATION = 0;
    private static final int BYTES_PER_FLOAT = 4;
    private static final boolean DEBUG = true;
    private static final boolean DEBUG_PER = false;
    private static final float DIFFUSE_FACTOR = 0.15f;
    private static final float[] LIGHT_COLOR = {1.0f, 1.0f, 1.0f, 1.0f};
    private static final float LIGHT_DEGREE = 64.0f;
    private static final int RECT_STRIDE = 8;
    private static final float SPACE_SCALE = 1.2f;
    private static final float SPECULAR_FACTOR = 0.85f;
    private static final String TAG = "WaterEffectRender";
    private static final float WAVE_CYCLE_DURATION_F = 1500.0f;
    private static final int WAVE_CYCLE_DURATION_I = 1500;
    private static final float WAVE_START_PHASE_OFFSET = 0.35f;
    private static final float WAVE_START_RADIUS = 0.2f;
    /* access modifiers changed from: private */
    public float mAlpha;
    private ObjectAnimator mAnimator;
    private float mCenterX;
    private float mCenterY;
    private Context mContext;
    private float mDiffuseFactor;
    private float mDrawRadius;
    private int mHeight;
    private float mLightDegree;
    private float[] mLightDirection;
    /* access modifiers changed from: private */
    public boolean mPaused;
    private float mPhaseOffset;
    private int mProgram;
    private float[] mProjectionMatrix = new float[16];
    private FloatBuffer mRectBuffer;
    private float[] mRectData = {-1.0f, 2.0f, -1.0f, -2.0f, 1.0f, 2.0f, 1.0f, 2.0f, -1.0f, -2.0f, 1.0f, -2.0f};
    private ReverseInterpolator mReverseInterpolator;
    /* access modifiers changed from: private */
    public boolean mShouldDrawNext;
    private float mSpecularFactor;
    private long mStartTime;
    private int mWidth;
    private float mXOffset;
    private float mYOffset;
    private int uAlpha;
    private int uDiffuseFactor;
    private int uLightColor;
    private int uLightDirection;
    private int uMvpMatrix;
    private int uPhaseOffset;
    private int uRadius;
    private int uSpecularFactor;
    private int uXOffset;
    private int uYOffset;

    private static class ReverseInterpolator implements TimeInterpolator {
        float mFirstPoint = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        float mFirstScale = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        float mSecondPoint = 1.0f;
        float mSecondScale = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        TimeInterpolator mStandardInterpolator = new PathInterpolator(0.4f, GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO, 0.2f, 1.0f);

        public float getInterpolation(float input) {
            if (input < this.mFirstPoint) {
                return this.mStandardInterpolator.getInterpolation(this.mFirstScale * input);
            }
            if (input >= this.mFirstPoint && input < this.mSecondPoint) {
                return 1.0f;
            }
            if (input >= this.mSecondPoint) {
                return 1.0f - this.mStandardInterpolator.getInterpolation((input - this.mSecondPoint) * this.mSecondScale);
            }
            return GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        }

        ReverseInterpolator(long inDuration, long animDuration, long outDuration) {
            long total = inDuration + animDuration + outDuration;
            if (inDuration >= 0 && animDuration >= 0 && outDuration >= 0 && total > 0) {
                this.mFirstPoint = (((float) inDuration) * 1.0f) / ((float) total);
                this.mSecondPoint = (((float) (inDuration + animDuration)) * 1.0f) / ((float) total);
            }
            if (this.mFirstPoint > GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO) {
                this.mFirstScale = 1.0f / this.mFirstPoint;
            }
            if (this.mSecondPoint < 1.0f) {
                this.mSecondScale = 1.0f / (1.0f - this.mSecondPoint);
            }
            Log.d(WaterEffectRender.TAG, "ReverseInterpolator: " + this.mFirstPoint + "  " + this.mFirstScale + "  " + this.mSecondPoint + "  " + this.mSecondScale);
        }
    }

    public WaterEffectRender(Context context) {
        ReverseInterpolator reverseInterpolator = new ReverseInterpolator(200, 200, 1000);
        this.mReverseInterpolator = reverseInterpolator;
        this.mContext = context;
        synchronized (this) {
            this.mPaused = true;
            this.mStartTime = 0;
            setAlpha(GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO);
            this.mCenterX = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
            this.mCenterY = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
            this.mWidth = 0;
            this.mHeight = 0;
            updateOffset();
        }
        setDiffuseFactor(DIFFUSE_FACTOR);
        setSpecularFactor(SPECULAR_FACTOR);
        setLightDegree(LIGHT_DEGREE);
    }

    public synchronized void setAlpha(float alpha) {
        if (alpha < GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO) {
            try {
                this.mAlpha = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
            } catch (Throwable th) {
                throw th;
            }
        } else if (alpha > 1.0f) {
            this.mAlpha = 1.0f;
        } else {
            this.mAlpha = alpha;
        }
    }

    public void setDiffuseFactor(float value) {
        Log.d(TAG, "setDiffuseFactor: value = " + value);
        if (value < GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO) {
            this.mDiffuseFactor = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        } else if (value > 1.0f) {
            this.mDiffuseFactor = 1.0f;
        } else {
            this.mDiffuseFactor = value;
        }
    }

    public void setSpecularFactor(float value) {
        Log.d(TAG, "setSpecularFactor: value = " + value);
        if (value < GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO) {
            this.mSpecularFactor = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        } else if (value > 1.0f) {
            this.mSpecularFactor = 1.0f;
        } else {
            this.mSpecularFactor = value;
        }
    }

    public void setLightDegree(float value) {
        Log.d(TAG, "setLightDegree: value = " + value);
        if (value < -90.0f) {
            this.mLightDegree = -90.0f;
        } else if (value > 90.0f) {
            this.mLightDegree = 90.0f;
        } else {
            this.mLightDegree = value;
        }
        float theta = (float) (((double) (this.mLightDegree / 180.0f)) * 3.141592653589793d);
        this.mLightDirection = new float[]{0.0f, (float) Math.sin((double) theta), (float) Math.cos((double) theta)};
    }

    public synchronized void setXOffset(float value) {
        this.mXOffset = value;
    }

    public synchronized void setYOffset(float value) {
        this.mYOffset = value;
    }

    public float getDiffuseFactor() {
        return this.mDiffuseFactor;
    }

    public float getSpecularFactor() {
        return this.mSpecularFactor;
    }

    public float getLightDegree() {
        return this.mLightDegree;
    }

    public synchronized float getXOffset() {
        return this.mXOffset;
    }

    public synchronized float getYOffset() {
        return this.mYOffset;
    }

    private synchronized void updateOffset() {
        if (this.mWidth <= 0 || this.mHeight <= 0) {
            this.mXOffset = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
            this.mYOffset = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        } else if (this.mWidth > this.mHeight) {
            this.mXOffset = (SPACE_SCALE * ((this.mCenterX * 2.0f) - ((float) this.mWidth))) / ((float) this.mHeight);
            this.mYOffset = (-1.2f * ((2.0f * this.mCenterY) - ((float) this.mHeight))) / ((float) this.mHeight);
        } else {
            this.mXOffset = (SPACE_SCALE * ((this.mCenterX * 2.0f) - ((float) this.mWidth))) / ((float) this.mWidth);
            this.mYOffset = (-1.2f * ((2.0f * this.mCenterY) - ((float) this.mHeight))) / ((float) this.mWidth);
        }
        Log.d(TAG, "updateOffset: xOffset = " + this.mXOffset + " yOffset = " + this.mYOffset);
    }

    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        Log.d(TAG, "onSurfaceCreated: ");
        this.mProgram = GLHelper.buildProgram(GLHelper.loadShaderSource(this.mContext, "water.vert"), GLHelper.loadShaderSource(this.mContext, "water.frag"));
        this.uMvpMatrix = GLES30.glGetUniformLocation(this.mProgram, "u_mvpMatrix");
        this.uXOffset = GLES30.glGetUniformLocation(this.mProgram, "u_xOffset");
        this.uYOffset = GLES30.glGetUniformLocation(this.mProgram, "u_yOffset");
        this.uAlpha = GLES30.glGetUniformLocation(this.mProgram, "u_alpha");
        this.uPhaseOffset = GLES30.glGetUniformLocation(this.mProgram, "u_phaseOffset");
        this.uRadius = GLES30.glGetUniformLocation(this.mProgram, "u_drawRadius");
        this.uLightDirection = GLES30.glGetUniformLocation(this.mProgram, "u_lightDirection");
        this.uLightColor = GLES30.glGetUniformLocation(this.mProgram, "u_lightColor");
        this.uDiffuseFactor = GLES30.glGetUniformLocation(this.mProgram, "u_diffuseFactor");
        this.uSpecularFactor = GLES30.glGetUniformLocation(this.mProgram, "u_specularFactor");
        Matrix.orthoM(this.mProjectionMatrix, 0, -1.0f, 1.0f, -1.0f, 1.0f, -1.0f, 1.0f);
        this.mRectBuffer = ByteBuffer.allocateDirect(this.mRectData.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().put(this.mRectData);
    }

    public void onSurfaceChanged(GL10 gl, int width, int height) {
        float f;
        float f2;
        float x;
        float x2;
        Log.d(TAG, "onSurfaceChanged: width = " + width + " height = " + height);
        if (width <= 0 || height <= 0) {
            Log.w(TAG, "onSurfaceChanged: width and height should not be zero");
            return;
        }
        if (width > height) {
            f = (float) width;
            f2 = (float) height;
        } else {
            f = (float) height;
            f2 = (float) width;
        }
        float aspectRatio = f / f2;
        if (width > height) {
            x = SPACE_SCALE * aspectRatio;
            x2 = 1.2f;
        } else {
            x2 = SPACE_SCALE * aspectRatio;
            x = 1.2f;
        }
        Matrix.orthoM(this.mProjectionMatrix, 0, -x, x, -x2, x2, -1.0f, 1.0f);
        this.mRectData = new float[]{-x, x2, -x, -x2, x, x2, x, x2, -x, -x2, x, -x2};
        this.mRectBuffer = ByteBuffer.allocateDirect(this.mRectData.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().put(this.mRectData);
        GLES30.glViewport(0, 0, width, height);
        synchronized (this) {
            this.mWidth = width;
            this.mHeight = height;
            updateOffset();
        }
    }

    /* access modifiers changed from: package-private */
    public void playAnim(float centerX, float centerY) {
        playAnim();
        synchronized (this) {
            Log.d(TAG, "playAnim: centerX = " + centerX + " centerY = " + centerY);
            this.mCenterX = centerX;
            this.mCenterY = centerY;
            updateOffset();
        }
    }

    /* access modifiers changed from: package-private */
    public void playAnim() {
        Log.d(TAG, "playAnim: ");
        if (this.mAnimator != null) {
            this.mAnimator.cancel();
        }
        this.mAnimator = ObjectAnimator.ofFloat(this, "alpha", new float[]{GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO, 1.0f});
        this.mAnimator.setDuration(1400);
        this.mAnimator.setInterpolator(this.mReverseInterpolator);
        this.mAnimator.addListener(new AnimatorListenerAdapter() {
            public void onAnimationEnd(Animator animation) {
                synchronized (WaterEffectRender.this) {
                    boolean unused = WaterEffectRender.this.mPaused = true;
                    float unused2 = WaterEffectRender.this.mAlpha = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
                    boolean unused3 = WaterEffectRender.this.mShouldDrawNext = true;
                }
            }

            public void onAnimationCancel(Animator animation) {
                animation.removeAllListeners();
            }
        });
        this.mAnimator.start();
        synchronized (this) {
            this.mAlpha = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
            this.mStartTime = SystemClock.elapsedRealtime();
            this.mPaused = false;
        }
    }

    /* access modifiers changed from: package-private */
    public void pauseAnim() {
        if (this.mAnimator != null) {
            this.mAnimator.cancel();
        }
        synchronized (this) {
            this.mPaused = true;
        }
    }

    /* access modifiers changed from: package-private */
    public void clearAnim() {
        synchronized (this) {
            pauseAnim();
            this.mAlpha = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
            this.mShouldDrawNext = true;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean shouldRequest() {
        boolean z;
        synchronized (this) {
            if (this.mPaused) {
                if (!this.mShouldDrawNext) {
                    z = false;
                }
            }
            z = true;
        }
        return z;
    }

    public void onDrawFrame(GL10 gl) {
        boolean paused;
        long startTime;
        float alpha;
        float xOffset;
        float yOffset;
        synchronized (this) {
            this.mShouldDrawNext = false;
            paused = this.mPaused;
            startTime = this.mStartTime;
            alpha = this.mAlpha;
            xOffset = this.mXOffset;
            yOffset = this.mYOffset;
        }
        GLES30.glClearColor(GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO, GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO, GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO, GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO);
        GLES30.glClear(16384);
        if (!paused) {
            long duration = SystemClock.elapsedRealtime() - startTime;
            this.mDrawRadius = ((((float) duration) / WAVE_CYCLE_DURATION_F) * 2.0f) + 0.2f + WAVE_START_PHASE_OFFSET;
            this.mPhaseOffset = (2.0f * (((float) (duration % 1500)) / WAVE_CYCLE_DURATION_F)) + WAVE_START_PHASE_OFFSET;
        }
        if (alpha >= 0.01f) {
            GLES30.glUseProgram(this.mProgram);
            GLES30.glUniformMatrix4fv(this.uMvpMatrix, 1, false, this.mProjectionMatrix, 0);
            GLES30.glUniform1f(this.uXOffset, xOffset);
            GLES30.glUniform1f(this.uYOffset, yOffset);
            GLES30.glUniform1f(this.uAlpha, alpha);
            GLES30.glUniform1f(this.uPhaseOffset, this.mPhaseOffset);
            GLES30.glUniform1f(this.uRadius, this.mDrawRadius);
            GLES30.glUniform3fv(this.uLightDirection, 1, this.mLightDirection, 0);
            GLES30.glUniform4fv(this.uLightColor, 1, LIGHT_COLOR, 0);
            GLES30.glUniform1f(this.uDiffuseFactor, this.mDiffuseFactor);
            GLES30.glUniform1f(this.uSpecularFactor, this.mSpecularFactor);
            GLHelper.setVertexAttributePointer(this.mRectBuffer, 0, 0, 2, 8);
            GLES30.glEnable(3042);
            GLES30.glBlendFunc(770, 771);
            GLES30.glDrawArrays(4, 0, 6);
            GLES30.glDisable(3042);
        }
    }

    /* access modifiers changed from: package-private */
    public void onPause() {
        clearAnim();
    }

    /* access modifiers changed from: package-private */
    public void onResume() {
        clearAnim();
    }
}
