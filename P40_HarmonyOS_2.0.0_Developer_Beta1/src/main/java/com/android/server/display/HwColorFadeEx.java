package com.android.server.display;

import android.content.Context;
import android.opengl.GLES20;
import android.util.Slog;
import com.huawei.hiai.awareness.AwarenessInnerConstants;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import libcore.io.Streams;

public final class HwColorFadeEx implements IHwColorFadeEx {
    private static final float DAWN_ANIMATION_TOTAL_DISPLAY_WIDTH = 1.6f;
    private static final boolean DEBUG = true;
    private static final int HALF_DISPLAY_WIDTH = 2;
    private static final float START_MOVING_DISPLAY_WIDTH = 0.6f;
    private static final String TAG = "HwColorFadeEx";
    private static final float THIRTY_PERCENT_DISPLAY_WIDTH = 0.3f;
    private static final int VERTEXS_NUMBERS = 4;
    private static final int VERTEX_ATTRIBUTES_NUMBERS = 2;
    private final Context mContext;
    private float mLeftFeatherBegin;
    private float mLeftFeatherEnd;
    private int mProgram;
    private int mProjMatrixLoc;
    private float mRightFeatherBegin;
    private float mRightFeatherEnd;
    private float mThirtyPercentWidth;
    private int mUniformLeftFeatherBegin;
    private int mUniformLeftFeatherEnd;
    private int mUniformLevel;
    private int mUniformRightFeatherBegin;
    private int mUniformRightFeatherEnd;
    private int mUniformThirtyWidth;
    private final FloatBuffer mVertexBuffer = createNativeFloatBuffer(8);
    private int mVertexLoc;

    public HwColorFadeEx(Context context) {
        this.mContext = context;
        Slog.d(TAG, "mContext =" + this.mContext);
    }

    public boolean initDwanAnimationGLShaders() {
        int vertShader = loadShader(this.mContext, 17825797, 35633);
        int fragShader = loadShader(this.mContext, 17825796, 35632);
        GLES20.glReleaseShaderCompiler();
        if (vertShader == 0 || fragShader == 0) {
            return false;
        }
        this.mProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(this.mProgram, vertShader);
        GLES20.glAttachShader(this.mProgram, fragShader);
        GLES20.glDeleteShader(vertShader);
        GLES20.glDeleteShader(fragShader);
        GLES20.glLinkProgram(this.mProgram);
        this.mVertexLoc = GLES20.glGetAttribLocation(this.mProgram, "position");
        this.mProjMatrixLoc = GLES20.glGetUniformLocation(this.mProgram, "proj_matrix");
        this.mUniformThirtyWidth = GLES20.glGetUniformLocation(this.mProgram, "vThirtyWidth");
        this.mUniformLevel = GLES20.glGetUniformLocation(this.mProgram, "vLevel");
        this.mUniformLeftFeatherBegin = GLES20.glGetUniformLocation(this.mProgram, "vLeftFeatherBegin");
        this.mUniformLeftFeatherEnd = GLES20.glGetUniformLocation(this.mProgram, "vLeftFeatherEnd");
        this.mUniformRightFeatherBegin = GLES20.glGetUniformLocation(this.mProgram, "vRightFeatherBegin");
        this.mUniformRightFeatherEnd = GLES20.glGetUniformLocation(this.mProgram, "vRightFeatherEnd");
        GLES20.glUseProgram(this.mProgram);
        GLES20.glUseProgram(0);
        return true;
    }

    public boolean initDwanAnimationGLBuffers(int displayWidth, int displayHeight) {
        setDawnAnimationQuad(this.mVertexBuffer, 0.0f, 0.0f, (float) displayWidth, (float) displayHeight);
        GLES20.glBindBuffer(34962, 0);
        return true;
    }

    private void setDawnAnimationQuad(FloatBuffer vertexBuffer, float valueX, float valueY, float valueW, float valueH) {
        Slog.d(TAG, "setDawnAnimationQuad: valueX=" + valueX + ", valueY=" + valueY + ", valueW=" + valueW + ", valueH=" + valueH);
        vertexBuffer.put(0, valueX);
        vertexBuffer.put(1, valueY + valueH);
        vertexBuffer.put(2, valueX);
        vertexBuffer.put(3, valueY);
        vertexBuffer.put(4, valueX + valueW);
        vertexBuffer.put(5, valueY + valueH);
        vertexBuffer.put(6, valueX + valueW);
        vertexBuffer.put(7, valueY);
    }

    private static FloatBuffer createNativeFloatBuffer(int size) {
        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(size * 4);
        byteBuffer.order(ByteOrder.nativeOrder());
        return byteBuffer.asFloatBuffer();
    }

    private int loadShader(Context context, int resourceId, int type) {
        String source = readFile(context, resourceId);
        int shader = GLES20.glCreateShader(type);
        GLES20.glShaderSource(shader, source);
        GLES20.glCompileShader(shader);
        int[] compiled = new int[1];
        GLES20.glGetShaderiv(shader, 35713, compiled, 0);
        if (compiled[0] != 0) {
            return shader;
        }
        Slog.e(TAG, "Could not compile shader " + shader + ", " + type + AwarenessInnerConstants.COLON_KEY);
        Slog.e(TAG, GLES20.glGetShaderSource(shader));
        Slog.e(TAG, GLES20.glGetShaderInfoLog(shader));
        GLES20.glDeleteShader(shader);
        return 0;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x001e, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x001f, code lost:
        if (r0 != null) goto L_0x0021;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:?, code lost:
        r0.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0025, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0026, code lost:
        r1.addSuppressed(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0029, code lost:
        throw r2;
     */
    private String readFile(Context context, int resourceId) {
        try {
            InputStream stream = context.getResources().openRawResource(resourceId);
            String str = new String(Streams.readFully(new InputStreamReader(stream)));
            if (stream != null) {
                stream.close();
            }
            return str;
        } catch (IOException e) {
            return "Can not open the resource : " + resourceId;
        }
    }

    public void calculateFeatherAreas(float level, int width) {
        float tmpLevel = DAWN_ANIMATION_TOTAL_DISPLAY_WIDTH * level;
        this.mThirtyPercentWidth = ((float) width) * THIRTY_PERCENT_DISPLAY_WIDTH;
        if (Float.compare(tmpLevel, 0.6f) <= 0) {
            this.mLeftFeatherBegin = ((float) (width / 2)) - ((((float) width) * tmpLevel) / 2.0f);
            this.mLeftFeatherEnd = (float) ((width / 2) + 1);
            this.mRightFeatherBegin = (float) (width / 2);
            this.mRightFeatherEnd = ((float) (width / 2)) + ((((float) width) * tmpLevel) / 2.0f);
        } else {
            this.mLeftFeatherBegin = ((float) (width / 2)) - ((((float) width) * tmpLevel) / 2.0f);
            this.mLeftFeatherEnd = this.mLeftFeatherBegin + (((float) width) * THIRTY_PERCENT_DISPLAY_WIDTH);
            this.mRightFeatherEnd = ((float) (width / 2)) + ((((float) width) * tmpLevel) / 2.0f);
            this.mRightFeatherBegin = this.mRightFeatherEnd - (((float) width) * THIRTY_PERCENT_DISPLAY_WIDTH);
        }
        Slog.i(TAG, "calculateFeatherAreas level:" + tmpLevel + " leftBeg:" + this.mLeftFeatherBegin + " leftEnd:" + this.mLeftFeatherEnd + " rightBeg:" + this.mRightFeatherBegin + " rightEnd:" + this.mRightFeatherEnd);
    }

    public void drawDawnAnimationFaded(float level, float[] projMatrix) {
        Slog.d(TAG, "drawDawnAnimationFaded: level =" + level);
        if (projMatrix == null || projMatrix.length == 0) {
            Slog.d(TAG, "projMatrix is not vaild");
            return;
        }
        GLES20.glEnable(3042);
        GLES20.glBlendFunc(770, 771);
        GLES20.glUseProgram(this.mProgram);
        GLES20.glUniformMatrix4fv(this.mProjMatrixLoc, 1, false, projMatrix, 0);
        GLES20.glEnableVertexAttribArray(this.mVertexLoc);
        GLES20.glVertexAttribPointer(this.mVertexLoc, 2, 5126, false, 0, (Buffer) this.mVertexBuffer);
        GLES20.glUniform1f(this.mUniformThirtyWidth, this.mThirtyPercentWidth);
        GLES20.glUniform1f(this.mUniformLevel, level);
        GLES20.glUniform1f(this.mUniformLeftFeatherBegin, this.mLeftFeatherBegin);
        GLES20.glUniform1f(this.mUniformLeftFeatherEnd, this.mLeftFeatherEnd);
        GLES20.glUniform1f(this.mUniformRightFeatherBegin, this.mRightFeatherBegin);
        GLES20.glUniform1f(this.mUniformRightFeatherEnd, this.mRightFeatherEnd);
        GLES20.glDrawArrays(5, 0, 4);
        GLES20.glDisable(3042);
        GLES20.glBindBuffer(34962, 0);
        GLES20.glUseProgram(0);
    }

    public void destroyDawnAnimationGLResources() {
        Slog.i(TAG, "destroy dawn animation GL resources");
        GLES20.glDeleteProgram(this.mProgram);
        checkGlErrors("glDeleteProgram");
        this.mProgram = 0;
    }

    private static boolean checkGlErrors(String func) {
        return checkGlErrors(func, true);
    }

    private static boolean checkGlErrors(String func, boolean log) {
        boolean hadError = false;
        for (int error = GLES20.glGetError(); error != 0; error = GLES20.glGetError()) {
            if (log) {
                Slog.e(TAG, func + " failed: error " + error, new Throwable());
            }
            hadError = true;
        }
        return hadError;
    }
}
