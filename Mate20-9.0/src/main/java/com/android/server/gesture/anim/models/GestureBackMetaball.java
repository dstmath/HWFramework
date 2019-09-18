package com.android.server.gesture.anim.models;

import android.opengl.GLES30;
import com.android.server.gesture.GestureNavConst;
import com.android.server.gesture.anim.GLHelper;
import com.android.server.gesture.anim.GLLogUtils;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class GestureBackMetaball implements GLModel {
    private static final String FRAGMENT_SHADER_CODE = "#version 300 es\nprecision mediump float;\n\n// Calculating parameters\nuniform float energyR;\nuniform float wallOffset;\nuniform float energyThreshold;\nuniform float ballCenterMaxDis;\nuniform float processOffset;\nuniform float uAntiAliasingDelta;\n\nuniform bool isLeft;\nuniform float animProcess;\nuniform float uBallCenterY;\n\n// Render point\nin vec2 v_Position;\n\n// Result color of render point\nout vec4 o_FragColor;\n\n\nfloat energy_to_ball(vec2 point, vec2 ball) {\n    float denomination = distance(point, ball) * distance(point, ball);\n    float energy = energyR * energyR / denomination;\n    return energy;\n}\n\nfloat energy_to_wall(vec2 point) {\n    return point.x * point.x - wallOffset;\n}\n\nvoid main() {\n\n    vec2 point = v_Position.xy;\n    float ballCenterDistance = (ballCenterMaxDis + processOffset) * animProcess;\n\n    // Calculate side to 1(right) and -1(left).\n    float sideRatio = isLeft ? -1.0 : 1.0;\n    vec2 ballCenter = vec2(sideRatio * (1.0 + processOffset - ballCenterDistance), uBallCenterY);\n\n    // draw energy area\n    float energy = energy_to_wall(point) + energy_to_ball(point, ballCenter);\n    if (energy >= energyThreshold + uAntiAliasingDelta) {\n        o_FragColor = vec4(0, 0, 0, 0.8);\n    } else if (energy >= energyThreshold) {\n        float alphaRatio = (energy - energyThreshold) / uAntiAliasingDelta;\n        o_FragColor = vec4(0, 0, 0, 0.8 * alphaRatio);\n    } else {\n        // draw white side\n        float sideWRatio = 0.97;\n        float sideX = sideRatio * (1.0 - (1.0 - sideRatio * v_Position.x) * sideWRatio);        vec2 pointS = vec2(sideX, v_Position.y * sideWRatio);\n\n        vec2 ballCenterS = vec2(sideRatio * (1.0 + processOffset - ballCenterDistance), uBallCenterY * sideWRatio);\n\n        // draw energy area\n        float energyS = energy_to_wall(pointS) + energy_to_ball(pointS, ballCenterS);\n        float alphaRatioUx = 1.0 - pointS.x * sideRatio;        if (energyS >= energyThreshold + uAntiAliasingDelta) {\n            o_FragColor = vec4(1, 1, 1, 0.8 * alphaRatioUx);\n        } else if (energyS >= energyThreshold) {\n            float alphaRatio = (energyS - energyThreshold) / uAntiAliasingDelta;\n            o_FragColor = vec4(1, 1, 1, 0.8 * alphaRatio * alphaRatioUx);\n        } else {\n            o_FragColor = vec4(0, 0, 0, 0);\n        }\n    }\n}";
    private static final float MAX_WALL_OFFSET = 0.15f;
    private static final float MIN_WALL_OFFSET = 0.05f;
    private static final int POINT_DESCRIBE_SIZE = 2;
    private static final String TAG = "GestureBackMetaball";
    private static final String VERTEX_SHADER_CODE = "#version 300 es\n\nlayout (location = 0) in vec4 a_Position;\n\nuniform float uHWRatio;\n\nout vec2 v_Position;\n\nvoid main() {\n    gl_Position = a_Position;\n    v_Position = vec2(a_Position.x, a_Position.y * uHWRatio);\n}";
    private float mAnimProcess;
    private int mAnimProcessLoc;
    private float mAntiAliasingDelta;
    private int mAntiAliasingDeltaLoc;
    private int mAttrVertexLoc;
    private float mBallCenter;
    private float mBallCenterMaxDis;
    private int mBallCenterMaxDisLoc;
    private int mBallCenterYLoc;
    private boolean mDraw;
    private float mEnergyR;
    private int mEnergyRLoc;
    private float mEnergyThreshold;
    private int mEnergyThresholdLoc;
    private int mHWRatioLoc;
    private int mHeight;
    private boolean mIsLeft;
    private int mIsLeftLoc;
    private float mProcessOffset;
    private int mProcessOffsetLoc;
    private int mProgram;
    private FloatBuffer mRectBuffer;
    private final float[] mRectData = {-1.0f, 1.0f, 1.0f, 1.0f, -1.0f, -1.0f, 1.0f, -1.0f};
    private float mWallOffset;
    private int mWallOffsetLoc;
    private int mWidth;

    public GestureBackMetaball() {
        GLLogUtils.logD(TAG, "create");
        this.mDraw = true;
        this.mEnergyR = 0.1f;
        this.mWallOffset = MIN_WALL_OFFSET;
        this.mEnergyThreshold = 1.0f;
        this.mBallCenterMaxDis = MAX_WALL_OFFSET;
        this.mProcessOffset = 0.35f;
        this.mAntiAliasingDelta = 0.015f;
        this.mIsLeft = true;
        this.mAnimProcess = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        this.mBallCenter = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
    }

    public void prepare() {
        this.mProgram = GLHelper.buildProgram(VERTEX_SHADER_CODE, FRAGMENT_SHADER_CODE);
        this.mRectBuffer = ByteBuffer.allocateDirect(this.mRectData.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().put(this.mRectData);
        this.mAttrVertexLoc = GLES30.glGetAttribLocation(this.mProgram, "a_Position");
        this.mEnergyRLoc = GLES30.glGetUniformLocation(this.mProgram, "energyR");
        this.mWallOffsetLoc = GLES30.glGetUniformLocation(this.mProgram, "wallOffset");
        this.mEnergyThresholdLoc = GLES30.glGetUniformLocation(this.mProgram, "energyThreshold");
        this.mBallCenterMaxDisLoc = GLES30.glGetUniformLocation(this.mProgram, "ballCenterMaxDis");
        this.mProcessOffsetLoc = GLES30.glGetUniformLocation(this.mProgram, "processOffset");
        this.mAntiAliasingDeltaLoc = GLES30.glGetUniformLocation(this.mProgram, "uAntiAliasingDelta");
        this.mIsLeftLoc = GLES30.glGetUniformLocation(this.mProgram, "isLeft");
        this.mAnimProcessLoc = GLES30.glGetUniformLocation(this.mProgram, "animProcess");
        this.mBallCenterYLoc = GLES30.glGetUniformLocation(this.mProgram, "uBallCenterY");
        this.mHWRatioLoc = GLES30.glGetUniformLocation(this.mProgram, "uHWRatio");
    }

    public void onSurfaceViewChanged(int width, int height) {
        GLES30.glViewport(0, 0, width, height);
        this.mWidth = width;
        this.mHeight = height;
    }

    public void drawSelf() {
        float hwRatio;
        if (this.mDraw) {
            if (this.mWidth < this.mHeight) {
                GLES30.glViewport(0, 0, this.mWidth, this.mHeight);
                hwRatio = ((float) this.mHeight) / ((float) this.mWidth);
            } else if (this.mIsLeft) {
                GLES30.glViewport(0, 0, this.mHeight, this.mHeight);
                hwRatio = 1.0f;
            } else {
                GLES30.glViewport(this.mWidth - this.mHeight, 0, this.mHeight, this.mHeight);
                hwRatio = 1.0f;
            }
            GLES30.glUseProgram(this.mProgram);
            GLES30.glUniform1f(this.mEnergyRLoc, this.mEnergyR);
            GLES30.glUniform1f(this.mWallOffsetLoc, this.mWallOffset);
            GLES30.glUniform1f(this.mEnergyThresholdLoc, this.mEnergyThreshold);
            GLES30.glUniform1f(this.mBallCenterMaxDisLoc, this.mBallCenterMaxDis);
            GLES30.glUniform1f(this.mProcessOffsetLoc, this.mProcessOffset);
            GLES30.glUniform1f(this.mAntiAliasingDeltaLoc, this.mAntiAliasingDelta);
            GLES30.glUniform1i(this.mIsLeftLoc, this.mIsLeft ? 1 : 0);
            GLES30.glUniform1f(this.mAnimProcessLoc, this.mAnimProcess);
            GLES30.glUniform1f(this.mBallCenterYLoc, changeBallCenterToGL(this.mBallCenter, hwRatio));
            GLES30.glUniform1f(this.mHWRatioLoc, hwRatio);
            this.mRectBuffer.flip();
            GLES30.glEnableVertexAttribArray(this.mAttrVertexLoc);
            GLES30.glVertexAttribPointer(this.mAttrVertexLoc, 2, 5126, false, 8, this.mRectBuffer);
            GLES30.glDrawArrays(5, 0, this.mRectData.length / 2);
        }
    }

    private float changeBallCenterToGL(float y, float hwRatio) {
        return ((-2.0f * (y / ((float) this.mHeight))) + 1.0f) * hwRatio;
    }

    public float getProcess() {
        return this.mAnimProcess;
    }

    public void setProcess(float process) {
        this.mAnimProcess = process;
        if (this.mAnimProcess <= 0.55f) {
            this.mWallOffset = MIN_WALL_OFFSET;
        } else {
            this.mWallOffset = MIN_WALL_OFFSET + ((0.10000001f * (process - 0.55f)) / 0.45f);
        }
    }

    public void setSide(boolean isLeft) {
        this.mIsLeft = isLeft;
    }

    public void setCenter(float y) {
        this.mBallCenter = y;
    }

    public void setDraw(boolean draw) {
        this.mDraw = draw;
    }
}
