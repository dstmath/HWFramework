package android.graphics;

import android.graphics.Shader;

public class LinearGradient extends Shader {
    private static final int TYPE_COLORS_AND_POSITIONS = 1;
    private static final int TYPE_COLOR_START_AND_COLOR_END = 2;
    private int mColor0;
    private int mColor1;
    private int[] mColors;
    private float[] mPositions;
    private Shader.TileMode mTileMode;
    private int mType;
    private float mX0;
    private float mX1;
    private float mY0;
    private float mY1;

    private native long nativeCreate1(long j, float f, float f2, float f3, float f4, int[] iArr, float[] fArr, int i);

    private native long nativeCreate2(long j, float f, float f2, float f3, float f4, int i, int i2, int i3);

    public LinearGradient(float x0, float y0, float x1, float y1, int[] colors, float[] positions, Shader.TileMode tile) {
        if (colors.length < 2) {
            throw new IllegalArgumentException("needs >= 2 number of colors");
        } else if (positions == null || colors.length == positions.length) {
            this.mType = 1;
            this.mX0 = x0;
            this.mY0 = y0;
            this.mX1 = x1;
            this.mY1 = y1;
            this.mColors = (int[]) colors.clone();
            this.mPositions = positions != null ? (float[]) positions.clone() : null;
            this.mTileMode = tile;
        } else {
            throw new IllegalArgumentException("color and position arrays must be of equal length");
        }
    }

    public LinearGradient(float x0, float y0, float x1, float y1, int color0, int color1, Shader.TileMode tile) {
        this.mType = 2;
        this.mX0 = x0;
        this.mY0 = y0;
        this.mX1 = x1;
        this.mY1 = y1;
        this.mColor0 = color0;
        this.mColor1 = color1;
        this.mColors = null;
        this.mPositions = null;
        this.mTileMode = tile;
    }

    /* access modifiers changed from: package-private */
    public long createNativeInstance(long nativeMatrix) {
        if (this.mType == 1) {
            return nativeCreate1(nativeMatrix, this.mX0, this.mY0, this.mX1, this.mY1, this.mColors, this.mPositions, this.mTileMode.nativeInt);
        }
        return nativeCreate2(nativeMatrix, this.mX0, this.mY0, this.mX1, this.mY1, this.mColor0, this.mColor1, this.mTileMode.nativeInt);
    }

    /* access modifiers changed from: protected */
    public Shader copy() {
        LinearGradient copy;
        if (this.mType == 1) {
            copy = new LinearGradient(this.mX0, this.mY0, this.mX1, this.mY1, (int[]) this.mColors.clone(), this.mPositions != null ? (float[]) this.mPositions.clone() : null, this.mTileMode);
        } else {
            copy = new LinearGradient(this.mX0, this.mY0, this.mX1, this.mY1, this.mColor0, this.mColor1, this.mTileMode);
        }
        copyLocalMatrix(copy);
        return copy;
    }
}
