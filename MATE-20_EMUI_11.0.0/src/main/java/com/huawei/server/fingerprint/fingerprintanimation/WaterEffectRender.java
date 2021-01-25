package com.huawei.server.fingerprint.fingerprintanimation;

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
import com.android.server.fingerprint.fingerprintAnimation.GLHelper;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public final class WaterEffectRender implements GLSurfaceView.Renderer {
    private static final long ANIM_DURATION = 200;
    private static final long ANIM_IN_DURATION = 200;
    private static final long ANIM_OUT_DURATION = 1000;
    private static final int A_POSITION_LOCATION = 0;
    private static final int BYTES_PER_FLOAT = 4;
    private static final int COMPONENT_COUNT = 2;
    private static final float CONTROL_X_FIRST = 0.4f;
    private static final float CONTROL_X_SECOND = 0.2f;
    private static final float CONTROL_Y_FIRST = 0.0f;
    private static final float CONTROL_Y_SECOND = 1.0f;
    private static final float DIFFUSE_FACTOR = 0.15f;
    private static final float DRAW_FRAME_ALPHA = 0.01f;
    private static final float DRAW_RADIUS_DOUBLE = 2.0f;
    private static final int GL_SIZE_I = 6;
    private static final boolean IS_DEBUG = true;
    private static final boolean IS_DEBUG_PER = false;
    private static final float[] LIGHT_COLORS = {1.0f, 1.0f, 1.0f, 1.0f};
    private static final float LIGHT_DEGREE = 64.0f;
    private static final double LIGHT_DEGREE_DIVISOR = 180.0d;
    private static final float LIGHT_DEGREE_NEGATIVE_THRESHOLD = -90.0f;
    private static final float LIGHT_DEGREE_POSITIVE_THRESHOLD = 90.0f;
    private static final float MATRIXS_BOTTOM = -1.0f;
    private static final float MATRIXS_FAR = 1.0f;
    private static final float MATRIXS_LEFT = -1.0f;
    private static final float MATRIXS_NEAR = -1.0f;
    private static final float MATRIXS_RIGHT = 1.0f;
    private static final float MATRIXS_TOP = 1.0f;
    private static final int PROJECTION_MATRIX_NUM = 16;
    private static final float RECT_INIT_DATA_FIRST = -1.0f;
    private static final float RECT_INIT_DATA_FIRST_POSITIVE = 1.0f;
    private static final float RECT_INIT_DATA_SECOND = -2.0f;
    private static final float RECT_INIT_DATA_SECOND_POSITIVE = 2.0f;
    private static final int RECT_STRIDE = 8;
    private static final int ROUNDING = 2;
    private static final float SPACE_SCALE = 1.2f;
    private static final float SPACE_SCALE_MULTIPLE = 2.0f;
    private static final float SPECULAR_FACTOR = 0.85f;
    private static final String TAG = "WaterEffectRender";
    private static final float WAVE_CYCLE_DURATION_F = 1500.0f;
    private static final int WAVE_CYCLE_DURATION_I = 1500;
    private static final float WAVE_START_PHASE_OFFSET = 0.35f;
    private static final float WAVE_START_RADIUS = 0.2f;
    private boolean isPaused;
    private boolean isShouldDrawNext;
    private float mAlpha;
    private ObjectAnimator mAnimator;
    private float mCenterX;
    private float mCenterY;
    private Context mContext;
    private float mDiffuseFactor;
    private float mDrawRadius;
    private int mHeight;
    private float mLightDegree;
    private float[] mLightDirections;
    private float mPhaseOffset;
    private int mProgram;
    private float[] mProjectionMatrixs = new float[16];
    private FloatBuffer mRectBuffer;
    private float[] mRectDatas = {-1.0f, 2.0f, -1.0f, RECT_INIT_DATA_SECOND, 1.0f, 2.0f, 1.0f, 2.0f, -1.0f, RECT_INIT_DATA_SECOND, 1.0f, RECT_INIT_DATA_SECOND};
    private ReverseInterpolator mReverseInterpolator = new ReverseInterpolator(200, 200, ANIM_OUT_DURATION);
    private float mSpecularFactor;
    private long mStartTime;
    private int mUniformAlpha;
    private int mUniformDiffuseFactor;
    private int mUniformLightColor;
    private int mUniformLightDirection;
    private int mUniformMvpMatrix;
    private int mUniformPhaseOffset;
    private int mUniformRadius;
    private int mUniformSpecularFactor;
    private int mUniformxOffset;
    private int mUniformyOffset;
    private int mWidth;
    private float mXoffset;
    private float mYoffset;

    public WaterEffectRender(Context context) {
        this.mContext = context;
        synchronized (this) {
            this.isPaused = true;
            this.mStartTime = 0;
            setAlpha(0.0f);
            this.mCenterX = 0.0f;
            this.mCenterY = 0.0f;
            this.mWidth = 0;
            this.mHeight = 0;
            updateOffset();
        }
        setDiffuseFactor(DIFFUSE_FACTOR);
        setSpecularFactor(0.85f);
        setLightDegree(LIGHT_DEGREE);
    }

    private synchronized void setAlpha(float alpha) {
        if (alpha < 0.0f) {
            try {
                this.mAlpha = 0.0f;
            } catch (Throwable th) {
                throw th;
            }
        } else if (alpha > 1.0f) {
            this.mAlpha = 1.0f;
        } else {
            this.mAlpha = alpha;
        }
    }

    private void setDiffuseFactor(float value) {
        Log.d(TAG, "setDiffuseFactor: value = " + value);
        if (value < 0.0f) {
            this.mDiffuseFactor = 0.0f;
        } else if (value > 1.0f) {
            this.mDiffuseFactor = 1.0f;
        } else {
            this.mDiffuseFactor = value;
        }
    }

    private void setSpecularFactor(float value) {
        Log.d(TAG, "setSpecularFactor: value = " + value);
        if (value < 0.0f) {
            this.mSpecularFactor = 0.0f;
        } else if (value > 1.0f) {
            this.mSpecularFactor = 1.0f;
        } else {
            this.mSpecularFactor = value;
        }
    }

    private void setLightDegree(float value) {
        Log.d(TAG, "setLightDegree: value = " + value);
        if (value < LIGHT_DEGREE_NEGATIVE_THRESHOLD) {
            this.mLightDegree = LIGHT_DEGREE_NEGATIVE_THRESHOLD;
        } else if (value > LIGHT_DEGREE_POSITIVE_THRESHOLD) {
            this.mLightDegree = LIGHT_DEGREE_POSITIVE_THRESHOLD;
        } else {
            this.mLightDegree = value;
        }
        double theta = new BigDecimal(3.141592653589793d).multiply(new BigDecimal((double) this.mLightDegree).divide(new BigDecimal((double) LIGHT_DEGREE_DIVISOR), 2, RoundingMode.HALF_UP)).doubleValue();
        this.mLightDirections = new float[]{0.0f, new BigDecimal(Math.sin(theta)).floatValue(), new BigDecimal(Math.cos(theta)).floatValue()};
    }

    public synchronized void setXoffset(float value) {
        this.mXoffset = value;
    }

    public synchronized void setYoffset(float value) {
        this.mYoffset = value;
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

    public synchronized float getXoffset() {
        return this.mXoffset;
    }

    public synchronized float getYoffset() {
        return this.mYoffset;
    }

    private synchronized void updateOffset() {
        if (this.mWidth <= 0 || this.mHeight <= 0) {
            this.mXoffset = 0.0f;
            this.mYoffset = 0.0f;
        } else if (this.mWidth > this.mHeight) {
            this.mXoffset = (((this.mCenterX * 2.0f) - ((float) this.mWidth)) * SPACE_SCALE) / ((float) this.mHeight);
            this.mYoffset = (((this.mCenterY * 2.0f) - ((float) this.mHeight)) * -1.2f) / ((float) this.mHeight);
        } else {
            this.mXoffset = (((this.mCenterX * 2.0f) - ((float) this.mWidth)) * SPACE_SCALE) / ((float) this.mWidth);
            this.mYoffset = (((this.mCenterY * 2.0f) - ((float) this.mHeight)) * -1.2f) / ((float) this.mWidth);
        }
        Log.d(TAG, "updateOffset: xOffset = " + this.mXoffset + " yOffset = " + this.mYoffset);
    }

    @Override // android.opengl.GLSurfaceView.Renderer
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        Log.d(TAG, "onSurfaceCreated: ");
        this.mProgram = GLHelper.buildProgram(GLHelper.loadShaderSource(this.mContext, "water.vert"), GLHelper.loadShaderSource(this.mContext, "water.frag"));
        this.mUniformMvpMatrix = GLES30.glGetUniformLocation(this.mProgram, "u_mvpMatrix");
        this.mUniformxOffset = GLES30.glGetUniformLocation(this.mProgram, "u_xOffset");
        this.mUniformyOffset = GLES30.glGetUniformLocation(this.mProgram, "u_yOffset");
        this.mUniformAlpha = GLES30.glGetUniformLocation(this.mProgram, "u_alpha");
        this.mUniformPhaseOffset = GLES30.glGetUniformLocation(this.mProgram, "u_phaseOffset");
        this.mUniformRadius = GLES30.glGetUniformLocation(this.mProgram, "u_drawRadius");
        this.mUniformLightDirection = GLES30.glGetUniformLocation(this.mProgram, "u_lightDirection");
        this.mUniformLightColor = GLES30.glGetUniformLocation(this.mProgram, "u_lightColor");
        this.mUniformDiffuseFactor = GLES30.glGetUniformLocation(this.mProgram, "u_diffuseFactor");
        this.mUniformSpecularFactor = GLES30.glGetUniformLocation(this.mProgram, "u_specularFactor");
        Matrix.orthoM(this.mProjectionMatrixs, 0, -1.0f, 1.0f, -1.0f, 1.0f, -1.0f, 1.0f);
        this.mRectBuffer = ByteBuffer.allocateDirect(this.mRectDatas.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().put(this.mRectDatas);
    }

    @Override // android.opengl.GLSurfaceView.Renderer
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        float aspectRatio;
        float xposition;
        float xposition2;
        Log.d(TAG, "onSurfaceChanged: width = " + width + " height = " + height);
        if (width <= 0 || height <= 0) {
            Log.w(TAG, "onSurfaceChanged: width and height should not be zero");
            return;
        }
        BigDecimal widthBig = new BigDecimal(width);
        BigDecimal heightBig = new BigDecimal(height);
        if (width > height) {
            aspectRatio = widthBig.divide(heightBig, 2, RoundingMode.HALF_UP).floatValue();
        } else {
            aspectRatio = heightBig.divide(widthBig, 2, RoundingMode.HALF_UP).floatValue();
        }
        if (width > height) {
            xposition2 = SPACE_SCALE * aspectRatio;
            xposition = SPACE_SCALE;
        } else {
            xposition = SPACE_SCALE * aspectRatio;
            xposition2 = 1.2f;
        }
        Matrix.orthoM(this.mProjectionMatrixs, 0, -xposition2, xposition2, -xposition, xposition, -1.0f, 1.0f);
        this.mRectDatas = new float[]{-xposition2, xposition, -xposition2, -xposition, xposition2, xposition, xposition2, xposition, -xposition2, -xposition, xposition2, -xposition};
        this.mRectBuffer = ByteBuffer.allocateDirect(this.mRectDatas.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().put(this.mRectDatas);
        GLES30.glViewport(0, 0, width, height);
        synchronized (this) {
            this.mWidth = width;
            this.mHeight = height;
            updateOffset();
        }
    }

    /* access modifiers changed from: private */
    public static class ReverseInterpolator implements TimeInterpolator {
        float mFirstPoint = 0.0f;
        float mFirstScale = 0.0f;
        float mSecondPoint = 1.0f;
        float mSecondScale = 0.0f;
        TimeInterpolator mStandardInterpolator = new PathInterpolator(0.4f, 0.0f, 0.2f, 1.0f);

        ReverseInterpolator(long inDuration, long animDuration, long outDuration) {
            long total = inDuration + animDuration + outDuration;
            if (inDuration >= 0 && animDuration >= 0 && outDuration >= 0 && total > 0) {
                this.mFirstPoint = (((float) inDuration) * 1.0f) / ((float) total);
                this.mSecondPoint = (((float) (inDuration + animDuration)) * 1.0f) / ((float) total);
            }
            float f = this.mFirstPoint;
            if (f > 0.0f) {
                this.mFirstScale = 1.0f / f;
            }
            float f2 = this.mSecondPoint;
            if (f2 < 1.0f) {
                this.mSecondScale = 1.0f / (1.0f - f2);
            }
            Log.d(WaterEffectRender.TAG, "ReverseInterpolator: mFirstPoint:" + this.mFirstPoint + ",mFirstScale:" + this.mFirstScale + ",mSecondPoint:" + this.mSecondPoint + ",mSecondScale:" + this.mSecondScale);
        }

        @Override // android.animation.TimeInterpolator
        public float getInterpolation(float input) {
            float f = this.mFirstPoint;
            if (input < f) {
                return this.mStandardInterpolator.getInterpolation(this.mFirstScale * input);
            }
            if (input >= f && input < this.mSecondPoint) {
                return 1.0f;
            }
            if (input < this.mSecondPoint) {
                return 0.0f;
            }
            BigDecimal firstBig = new BigDecimal(1.0d);
            BigDecimal inputBig = new BigDecimal((double) input);
            BigDecimal secondPointBig = new BigDecimal((double) this.mSecondPoint);
            return firstBig.subtract(new BigDecimal((double) this.mStandardInterpolator.getInterpolation(new BigDecimal((double) this.mSecondScale).multiply(inputBig.subtract(secondPointBig)).floatValue()))).floatValue();
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
        ObjectAnimator objectAnimator = this.mAnimator;
        if (objectAnimator != null) {
            objectAnimator.cancel();
        }
        this.mAnimator = ObjectAnimator.ofFloat(this, "alpha", 0.0f, 1.0f);
        this.mAnimator.setDuration(1400L);
        this.mAnimator.setInterpolator(this.mReverseInterpolator);
        this.mAnimator.addListener(new AnimatorListenerAdapter() {
            /* class com.huawei.server.fingerprint.fingerprintanimation.WaterEffectRender.AnonymousClass1 */

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationEnd(Animator animation) {
                synchronized (WaterEffectRender.this) {
                    WaterEffectRender.this.isPaused = true;
                    WaterEffectRender.this.mAlpha = 0.0f;
                    WaterEffectRender.this.isShouldDrawNext = true;
                }
            }

            @Override // android.animation.AnimatorListenerAdapter, android.animation.Animator.AnimatorListener
            public void onAnimationCancel(Animator animation) {
                animation.removeAllListeners();
            }
        });
        this.mAnimator.start();
        synchronized (this) {
            this.mAlpha = 0.0f;
            this.mStartTime = SystemClock.elapsedRealtime();
            this.isPaused = false;
        }
    }

    /* access modifiers changed from: package-private */
    public void pauseAnim() {
        ObjectAnimator objectAnimator = this.mAnimator;
        if (objectAnimator != null) {
            objectAnimator.cancel();
        }
        synchronized (this) {
            this.isPaused = true;
        }
    }

    /* access modifiers changed from: package-private */
    public void clearAnim() {
        synchronized (this) {
            pauseAnim();
            this.mAlpha = 0.0f;
            this.isShouldDrawNext = true;
        }
    }

    /* access modifiers changed from: package-private */
    public boolean shouldRequest() {
        boolean z;
        synchronized (this) {
            if (this.isPaused) {
                if (!this.isShouldDrawNext) {
                    z = false;
                }
            }
            z = true;
        }
        return z;
    }

    @Override // android.opengl.GLSurfaceView.Renderer
    public void onDrawFrame(GL10 gl) {
        boolean isDrawPaused;
        long startTime;
        float alpha;
        float xoffset;
        float yoffset;
        synchronized (this) {
            this.isShouldDrawNext = false;
            isDrawPaused = this.isPaused;
            startTime = this.mStartTime;
            alpha = this.mAlpha;
            xoffset = this.mXoffset;
            yoffset = this.mYoffset;
        }
        GLES30.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        GLES30.glClear(16384);
        if (!isDrawPaused) {
            long duration = SystemClock.elapsedRealtime() - startTime;
            this.mDrawRadius = ((((float) duration) / WAVE_CYCLE_DURATION_F) * 2.0f) + 0.2f + WAVE_START_PHASE_OFFSET;
            this.mPhaseOffset = ((((float) (duration % 1500)) / WAVE_CYCLE_DURATION_F) * 2.0f) + WAVE_START_PHASE_OFFSET;
        }
        if (alpha >= DRAW_FRAME_ALPHA) {
            GLES30.glUseProgram(this.mProgram);
            GLES30.glUniformMatrix4fv(this.mUniformMvpMatrix, 1, false, this.mProjectionMatrixs, 0);
            GLES30.glUniform1f(this.mUniformxOffset, xoffset);
            GLES30.glUniform1f(this.mUniformyOffset, yoffset);
            GLES30.glUniform1f(this.mUniformAlpha, alpha);
            GLES30.glUniform1f(this.mUniformPhaseOffset, this.mPhaseOffset);
            GLES30.glUniform1f(this.mUniformRadius, this.mDrawRadius);
            GLES30.glUniform3fv(this.mUniformLightDirection, 1, this.mLightDirections, 0);
            GLES30.glUniform4fv(this.mUniformLightColor, 1, LIGHT_COLORS, 0);
            GLES30.glUniform1f(this.mUniformDiffuseFactor, this.mDiffuseFactor);
            GLES30.glUniform1f(this.mUniformSpecularFactor, this.mSpecularFactor);
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
