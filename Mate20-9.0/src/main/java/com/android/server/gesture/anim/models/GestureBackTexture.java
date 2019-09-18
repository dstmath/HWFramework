package com.android.server.gesture.anim.models;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.opengl.GLES30;
import android.opengl.GLUtils;
import com.android.server.gesture.GestureNavConst;
import com.android.server.gesture.anim.GLHelper;
import com.android.server.gesture.anim.GLLogUtils;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class GestureBackTexture implements GLModel {
    private static final int BASE_VIEW_HEIGHT = 2340;
    private static final int BASE_VIEW_WIDTH = 1080;
    private static final String FRAGMENT_SHADER_CODE = "#version 300 es\nprecision mediump float;\n\nuniform float uAlpha;\nuniform sampler2D uTexture;\n\nin vec2 vTexCoord;\n\nout vec4 oFragColor;\n\nvoid main() {\n    vec4 vt = texture(uTexture, vTexCoord);\n    oFragColor = vec4(vt.rgb, vt.a * uAlpha);\n}";
    private static final int POINT_DESCRIBE_SIZE = 2;
    private static final String TAG = "GestureBackTexture";
    private static final int TEXTURE_H = 62;
    private static final int TEXTURE_W = 38;
    private static final String VERTEX_SHADER_CODE = "#version 300 es\n\nlayout (location = 0) in vec4 aPosition;\nlayout (location = 1) in vec2 aTexCoordinate;\n\nout vec2 vTexCoord;\n\nvoid main() {\n    gl_Position = aPosition;\n    vTexCoord = aTexCoordinate;\n}";
    private int mAlphaLoc;
    private float mAnimProcess;
    private int mAttrTextureLoc;
    private int mAttrVertexLoc;
    private int mCenterY;
    private Context mContext;
    private boolean mDraw;
    private int mHeight;
    private boolean mIsLeft;
    private float mMaxTextureOffset;
    private int mProgram;
    private FloatBuffer mRectBuffer;
    private float[] mRectData = {-1.0f, 1.0f, 1.0f, 1.0f, -1.0f, -1.0f, 1.0f, -1.0f};
    private float mTextureAlpha;
    private FloatBuffer mTextureBuffer;
    private final float[] mTextureData = {GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO, GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO, 1.0f, GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO, GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO, 1.0f, 1.0f, 1.0f};
    private int mTextureLoc;
    private int mWidth;

    public GestureBackTexture(Context context) {
        this.mContext = context;
        this.mDraw = true;
        this.mMaxTextureOffset = 0.15f;
        this.mAnimProcess = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        this.mIsLeft = true;
        this.mTextureAlpha = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        this.mCenterY = 0;
    }

    public void prepare() {
        this.mProgram = GLHelper.buildProgram(VERTEX_SHADER_CODE, FRAGMENT_SHADER_CODE);
        this.mRectBuffer = ByteBuffer.allocateDirect(this.mRectData.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().put(this.mRectData);
        this.mTextureBuffer = ByteBuffer.allocateDirect(this.mTextureData.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().put(this.mTextureData);
        createTexture(this.mContext, 33751967);
        this.mAttrVertexLoc = GLES30.glGetAttribLocation(this.mProgram, "aPosition");
        this.mAttrTextureLoc = GLES30.glGetAttribLocation(this.mProgram, "aTexCoordinate");
        this.mAlphaLoc = GLES30.glGetUniformLocation(this.mProgram, "uAlpha");
        this.mTextureLoc = GLES30.glGetUniformLocation(this.mProgram, "uTexture");
    }

    public void onSurfaceViewChanged(int width, int height) {
        GLES30.glViewport(0, 0, width, height);
        this.mWidth = width;
        this.mHeight = height;
        if (this.mCenterY == 0) {
            this.mCenterY = height / 2;
        }
    }

    public void drawSelf() {
        if (this.mDraw) {
            int centerY = changeCenterToGL(this.mCenterY);
            if (this.mWidth < this.mHeight) {
                float viewRatio = ((float) this.mWidth) / 1080.0f;
                GLES30.glViewport(0, centerY - ((int) ((62.0f * viewRatio) / 2.0f)), this.mWidth, (int) (62.0f * viewRatio));
                refreshVertexLoc((float) this.mWidth, viewRatio);
            } else if (this.mIsLeft) {
                float viewRatio2 = ((float) this.mHeight) / 1080.0f;
                GLES30.glViewport(0, centerY - ((int) ((62.0f * viewRatio2) / 2.0f)), this.mHeight, (int) (62.0f * viewRatio2));
                refreshVertexLoc((float) this.mHeight, viewRatio2);
            } else {
                float viewRatio3 = ((float) this.mHeight) / 1080.0f;
                GLES30.glViewport(this.mWidth - this.mHeight, centerY - ((int) ((62.0f * viewRatio3) / 2.0f)), this.mHeight, (int) (62.0f * viewRatio3));
                refreshVertexLoc((float) this.mHeight, viewRatio3);
            }
            GLES30.glUseProgram(this.mProgram);
            GLES30.glUniform1f(this.mAlphaLoc, this.mTextureAlpha);
            GLES30.glUniform1i(this.mTextureLoc, 0);
            this.mRectBuffer.flip();
            GLES30.glEnableVertexAttribArray(this.mAttrVertexLoc);
            GLES30.glVertexAttribPointer(this.mAttrVertexLoc, 2, 5126, false, 8, this.mRectBuffer);
            this.mTextureBuffer.flip();
            GLES30.glEnableVertexAttribArray(this.mAttrTextureLoc);
            GLES30.glVertexAttribPointer(this.mAttrTextureLoc, 2, 5126, false, 8, this.mTextureBuffer);
            GLES30.glDrawArrays(5, 0, this.mRectData.length / 2);
        }
    }

    private int changeCenterToGL(int y) {
        return this.mHeight - y;
    }

    private void refreshVertexLoc(float drawWidth, float viewRatio) {
        float textureW = (38.0f * viewRatio) / drawWidth;
        float startOffset = 2.0f * textureW;
        float textureCenterX = (-1.0f - startOffset) + ((this.mMaxTextureOffset + startOffset) * this.mAnimProcess);
        if (!this.mIsLeft) {
            textureCenterX *= -1.0f;
        }
        this.mRectData = new float[]{textureCenterX - textureW, 1.0f, textureCenterX + textureW, 1.0f, textureCenterX - textureW, -1.0f, textureCenterX + textureW, -1.0f};
        this.mRectBuffer = ByteBuffer.allocateDirect(this.mRectData.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().put(this.mRectData);
    }

    private static void createTexture(Context context, int resourceId) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId, options);
        if (bitmap == null) {
            GLLogUtils.logD(TAG, ">> load bitmap fail");
            return;
        }
        int[] texture = new int[1];
        GLES30.glGenTextures(1, texture, 0);
        if (texture[0] == 0) {
            GLLogUtils.logD(TAG, ">> create texture fail");
            return;
        }
        GLES30.glActiveTexture(33984);
        GLES30.glBindTexture(3553, texture[0]);
        GLES30.glTexParameterf(3553, 10241, 9728.0f);
        GLES30.glTexParameterf(3553, 10240, 9729.0f);
        GLUtils.texImage2D(3553, 0, 6408, bitmap, 5121, 0);
        bitmap.recycle();
        GLES30.glGenerateMipmap(3553);
    }

    private void setAlpha(float alpha) {
        this.mTextureAlpha = alpha;
    }

    public void setProcess(float process) {
        this.mAnimProcess = process;
        if (this.mAnimProcess >= 0.55f) {
            setAlpha(1.0f);
        } else if (this.mAnimProcess < 0.275f) {
            setAlpha(GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO);
        } else {
            setAlpha((this.mAnimProcess - 0.275f) / (0.55f - 0.275f));
        }
    }

    public void setSide(boolean isLeft) {
        this.mIsLeft = isLeft;
    }

    public void setCenter(int y) {
        this.mCenterY = y;
    }

    public void setDraw(boolean draw) {
        this.mDraw = draw;
    }
}
