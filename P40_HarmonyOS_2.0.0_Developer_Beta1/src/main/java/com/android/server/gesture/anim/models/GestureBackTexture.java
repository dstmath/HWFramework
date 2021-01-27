package com.android.server.gesture.anim.models;

import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.hardware.display.HwFoldScreenState;
import android.opengl.GLES30;
import android.opengl.GLUtils;
import com.android.server.gesture.GestureNavConst;
import com.android.server.gesture.anim.GLHelper;
import com.android.server.gesture.anim.GLLogUtils;
import com.huawei.android.fsm.HwFoldScreenManagerEx;
import com.huawei.utils.HwPartResourceUtils;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class GestureBackTexture implements GLModel {
    private static final int BASE_VIEW_HEIGHT = 2340;
    private static final int BASE_VIEW_WIDTH = 1080;
    private static final int CALCULATION_ACCURACY_COMPENSATION = 2;
    private static final float DEFAULT_VALUE = -1.0f;
    private static final int DEVIDE_NUMBER = 2;
    private static final int DOCK_TEXTURE_H = 62;
    private static final int DOCK_TEXTURE_W = 62;
    private static final float EPSLON = 1.0E-6f;
    private static final String FRAGMENT_SHADER_CODE = "#version 300 es\nprecision mediump float;\n\nuniform float uAlpha;\nuniform sampler2D uTexture;\n\nin vec2 vTexCoord;\n\nout vec4 oFragColor;\n\nvoid main() {\n    vec4 vt = texture(uTexture, vTexCoord);\n    oFragColor = vec4(vt.rgb, vt.a * uAlpha);\n}";
    private static final float MAX_TEXTURE_OFFSET = 0.15f;
    private static final float PAD_DEFAULT_OFFSET_RATIO = 1.0f;
    private static final int POINT_DESCRIBE_SIZE = 2;
    private static final String TAG = "GestureBackTexture";
    private static final int TEXTURE_H = 62;
    private static final int TEXTURE_W = 38;
    private static final String VERTEX_SHADER_CODE = "#version 300 es\n\nlayout (location = 0) in vec4 aPosition;\nlayout (location = 1) in vec2 aTexCoordinate;\n\nout vec2 vTexCoord;\n\nvoid main() {\n    gl_Position = aPosition;\n    vTexCoord = aTexCoordinate;\n}";
    private int mAlphaLoc;
    private float mAnimProcess;
    private int mAnimWindowsH;
    private int mAnimWindowsW;
    private int mAttrTextureLoc;
    private int mAttrVertexLoc;
    private int mCenterY;
    private Context mContext;
    private float mDockAlpha;
    private float mFoldMainScreenRatio = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
    private int mHeight;
    private int mIcon;
    private boolean mIsDraw;
    private boolean mIsFoldScreenDevice = false;
    private boolean mIsLeft;
    private float mMaxTextureOffset;
    private float mPadStartOffsetRatio = 0.43f;
    private int mProgram;
    private FloatBuffer mRectBuffer;
    private float[] mRectDatas = {DEFAULT_VALUE, 1.0f, 1.0f, 1.0f, DEFAULT_VALUE, DEFAULT_VALUE, 1.0f, DEFAULT_VALUE};
    private int mStartX;
    private float mTextureAlpha;
    private FloatBuffer mTextureBuffer;
    private final float[] mTextureDatas = {GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO, GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO, 1.0f, GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO, GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO, 1.0f, 1.0f, 1.0f};
    private int mTextureH;
    private int mTextureLoc;
    private int mTextureW;
    private int mWidth;

    public GestureBackTexture(Context context, int icon) {
        this.mContext = context;
        this.mIsDraw = true;
        this.mMaxTextureOffset = MAX_TEXTURE_OFFSET;
        this.mAnimProcess = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        this.mIsLeft = true;
        this.mTextureAlpha = GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO;
        this.mCenterY = 0;
        this.mIcon = icon;
        this.mDockAlpha = 1.0f;
        if (this.mIcon == HwPartResourceUtils.getResourceId("gesture_nav_back_anim")) {
            this.mTextureW = TEXTURE_W;
            this.mTextureH = 62;
        } else {
            this.mTextureW = 62;
            this.mTextureH = 62;
        }
        if (HwFoldScreenState.isFoldScreenDevice()) {
            this.mIsFoldScreenDevice = true;
            Rect foldScreenSize = HwFoldScreenState.getScreenPhysicalRect(2);
            if (foldScreenSize == null || foldScreenSize.height() == 0) {
                GLLogUtils.logE(TAG, "get foldScreenSize failed");
            } else {
                this.mFoldMainScreenRatio = ((float) foldScreenSize.width()) / ((float) foldScreenSize.height());
            }
        }
    }

    @Override // com.android.server.gesture.anim.models.GLModel
    public void prepare() {
        this.mProgram = GLHelper.buildProgram(VERTEX_SHADER_CODE, FRAGMENT_SHADER_CODE);
        this.mRectBuffer = ByteBuffer.allocateDirect(this.mRectDatas.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().put(this.mRectDatas);
        createTexture(this.mContext, this.mIcon);
        this.mAttrVertexLoc = GLES30.glGetAttribLocation(this.mProgram, "aPosition");
        this.mAttrTextureLoc = GLES30.glGetAttribLocation(this.mProgram, "aTexCoordinate");
        this.mAlphaLoc = GLES30.glGetUniformLocation(this.mProgram, "uAlpha");
        this.mTextureLoc = GLES30.glGetUniformLocation(this.mProgram, "uTexture");
    }

    @Override // com.android.server.gesture.anim.models.GLModel
    public void onSurfaceViewChanged(int width, int height) {
        this.mAnimWindowsW = width;
        this.mAnimWindowsH = height;
        setCenter();
        GLES30.glViewport(-this.mStartX, 0, width, height);
    }

    @Override // com.android.server.gesture.anim.models.GLModel
    public void drawSelf() {
        if (this.mIsDraw) {
            adapterDevices();
            GLES30.glUseProgram(this.mProgram);
            GLES30.glUniform1f(this.mAlphaLoc, this.mTextureAlpha * this.mDockAlpha);
            int mTextureValue = 0;
            if (this.mIcon == HwPartResourceUtils.getResourceId("ic_dock_app")) {
                mTextureValue = 1;
            }
            GLES30.glUniform1i(this.mTextureLoc, mTextureValue);
            this.mTextureBuffer = ByteBuffer.allocateDirect(this.mTextureDatas.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().put(this.mTextureDatas);
            this.mRectBuffer.flip();
            GLES30.glEnableVertexAttribArray(this.mAttrVertexLoc);
            GLES30.glVertexAttribPointer(this.mAttrVertexLoc, 2, 5126, false, 8, (Buffer) this.mRectBuffer);
            this.mTextureBuffer.flip();
            GLES30.glEnableVertexAttribArray(this.mAttrTextureLoc);
            GLES30.glVertexAttribPointer(this.mAttrTextureLoc, 2, 5126, false, 8, (Buffer) this.mTextureBuffer);
            GLES30.glDrawArrays(5, 0, this.mRectDatas.length / 2);
        }
    }

    private int changeCenterToGl(int positionY) {
        return this.mHeight - positionY;
    }

    private void refreshVertexLoc(float drawWidth, float viewRatio) {
        if (Float.compare(drawWidth, GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO) == 0) {
            GLLogUtils.logW(TAG, "drawWidth is 0");
            return;
        }
        float textureW = (((float) this.mTextureW) * viewRatio) / drawWidth;
        float startOffset = ((38.0f * viewRatio) / drawWidth) * 2.0f;
        float textureCenterX = (DEFAULT_VALUE - startOffset) + ((this.mMaxTextureOffset + startOffset) * this.mAnimProcess);
        if (!this.mIsLeft) {
            textureCenterX *= DEFAULT_VALUE;
        }
        if (!isRtlLanguage()) {
            this.mRectDatas = new float[]{textureCenterX - textureW, 1.0f, textureCenterX + textureW, 1.0f, textureCenterX - textureW, -1.0f, textureCenterX + textureW, -1.0f};
        } else {
            this.mRectDatas = new float[]{textureCenterX + textureW, 1.0f, textureCenterX - textureW, 1.0f, textureCenterX + textureW, -1.0f, textureCenterX - textureW, -1.0f};
        }
        this.mRectBuffer = ByteBuffer.allocateDirect(this.mRectDatas.length * 4).order(ByteOrder.nativeOrder()).asFloatBuffer().put(this.mRectDatas);
    }

    private void adapterDevices() {
        int centerY = changeCenterToGl(this.mCenterY);
        if (!this.mIsFoldScreenDevice || HwFoldScreenManagerEx.getFoldableState() == 2) {
            int i = this.mWidth;
            int i2 = this.mHeight;
            if (i < i2) {
                float viewRatio = ((float) i) / 1080.0f;
                if (!GestureNavConst.IS_TABLET || this.mIsLeft) {
                    int i3 = this.mTextureH;
                    GLES30.glViewport(-this.mStartX, centerY - ((int) ((((float) i3) * viewRatio) / 2.0f)), this.mWidth, (int) (((float) i3) * viewRatio));
                } else {
                    int i4 = this.mWidth;
                    int i5 = this.mTextureH;
                    GLES30.glViewport((-this.mStartX) + ((int) (((float) i4) * this.mPadStartOffsetRatio)) + 2, centerY - ((int) ((((float) i5) * viewRatio) / 2.0f)), i4, (int) (((float) i5) * viewRatio));
                }
                refreshVertexLoc((float) this.mWidth, viewRatio);
            } else if (this.mIsLeft) {
                float viewRatio2 = ((float) i2) / 1080.0f;
                int i6 = this.mTextureH;
                GLES30.glViewport(0, centerY - ((int) ((((float) i6) * viewRatio2) / 2.0f)), i2, (int) (((float) i6) * viewRatio2));
                refreshVertexLoc((float) this.mHeight, viewRatio2);
            } else {
                float viewRatio3 = ((float) i2) / 1080.0f;
                int i7 = this.mTextureH;
                GLES30.glViewport(this.mAnimWindowsW - i2, centerY - ((int) ((((float) i7) * viewRatio3) / 2.0f)), i2, (int) (((float) i7) * viewRatio3));
                refreshVertexLoc((float) this.mHeight, viewRatio3);
            }
        } else {
            int longSide = this.mWidth;
            int i8 = this.mHeight;
            if (longSide <= i8) {
                longSide = i8;
            }
            int viewportWidth = (int) (this.mFoldMainScreenRatio * ((float) longSide));
            float viewRatio4 = ((float) viewportWidth) / 1080.0f;
            if (this.mIsLeft) {
                int i9 = this.mTextureH;
                GLES30.glViewport(-this.mStartX, centerY - ((int) ((((float) i9) * viewRatio4) / 2.0f)), viewportWidth, (int) (((float) i9) * viewRatio4));
            } else {
                int i10 = this.mTextureH;
                GLES30.glViewport(this.mAnimWindowsW - viewportWidth, centerY - ((int) ((((float) i10) * viewRatio4) / 2.0f)), viewportWidth, (int) (((float) i10) * viewRatio4));
            }
            refreshVertexLoc((float) viewportWidth, viewRatio4);
        }
    }

    private void createTexture(Context context, int resourceId) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inScaled = false;
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), resourceId, options);
        if (bitmap == null) {
            GLLogUtils.logD(TAG, ">> load bitmap fail");
            return;
        }
        int[] textures = new int[1];
        GLES30.glGenTextures(1, textures, 0);
        if (textures[0] == 0) {
            GLLogUtils.logD(TAG, ">> create texture fail");
            return;
        }
        if (resourceId == HwPartResourceUtils.getResourceId("gesture_nav_back_anim")) {
            GLES30.glActiveTexture(33984);
            GLES30.glBindTexture(3553, textures[0]);
        } else {
            GLES30.glActiveTexture(33985);
            GLES30.glBindTexture(3553, textures[0]);
        }
        GLES30.glTexParameterf(3553, 10241, 9728.0f);
        GLES30.glTexParameterf(3553, 10240, 9729.0f);
        GLES30.glTexParameterf(3553, 10243, 33071.0f);
        GLES30.glTexParameterf(3553, 10242, 33071.0f);
        GLUtils.texImage2D(3553, 0, 6408, bitmap, 5121, 0);
        bitmap.recycle();
        GLES30.glGenerateMipmap(3553);
    }

    private void setAlpha(float alpha) {
        this.mTextureAlpha = alpha;
    }

    public void setDockAlpha(float alpha) {
        this.mDockAlpha = alpha;
    }

    public void setScaleRate(float scaleRate) {
        float scaleValue = 1.0f / scaleRate;
        float[] fArr = this.mTextureDatas;
        fArr[0] = (1.0f - scaleValue) / 2.0f;
        fArr[1] = (1.0f - scaleValue) / 2.0f;
        fArr[2] = (scaleValue + 1.0f) / 2.0f;
        fArr[3] = (1.0f - scaleValue) / 2.0f;
        fArr[4] = (1.0f - scaleValue) / 2.0f;
        fArr[5] = (scaleValue + 1.0f) / 2.0f;
        fArr[6] = (scaleValue + 1.0f) / 2.0f;
        fArr[7] = (1.0f + scaleValue) / 2.0f;
    }

    public void setProcess(float process) {
        this.mAnimProcess = process;
        float f = this.mAnimProcess;
        if (f >= 0.55f) {
            setAlpha(1.0f);
        } else if (f < 0.275f) {
            setAlpha(GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO);
        } else {
            setAlpha((f - 0.275f) / (0.55f - 0.275f));
        }
    }

    public void setSide(boolean isLeft) {
        this.mIsLeft = isLeft;
    }

    public void setCenter() {
        this.mCenterY = this.mHeight - (this.mAnimWindowsH / 2);
    }

    public void setDraw(boolean isDraw) {
        this.mIsDraw = isDraw;
    }

    public void setHwSize(int width, int height) {
        GLLogUtils.logD(TAG, "setHwSize" + width + "::" + height);
        this.mWidth = width;
        this.mHeight = height;
    }

    public void setStartPosition(int startX) {
        GLLogUtils.logD(TAG, "setStartPosition" + startX);
        this.mStartX = startX;
    }

    public void setStartPositionOffset(float offsetRatio) {
        GLLogUtils.logD(TAG, "setStartPositionOffset" + offsetRatio);
        if (Math.abs(offsetRatio - GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO) >= EPSLON) {
            this.mPadStartOffsetRatio = (1.0f - offsetRatio) / offsetRatio;
        }
    }

    private boolean isRtlLanguage() {
        Configuration configuration;
        Resources resource = this.mContext.getResources();
        if (resource == null || (configuration = resource.getConfiguration()) == null || (configuration.screenLayout & 192) != 128) {
            return false;
        }
        return true;
    }
}
