package android.graphics;

import android.graphics.Shader;

public class RadialGradient extends Shader {
    private static final int TYPE_COLORS_AND_POSITIONS = 1;
    private static final int TYPE_COLOR_CENTER_AND_COLOR_EDGE = 2;
    private int mCenterColor;
    private int[] mColors;
    private int mEdgeColor;
    private float[] mPositions;
    private float mRadius;
    private Shader.TileMode mTileMode;
    private int mType;
    private float mX;
    private float mY;

    private static native long nativeCreate1(long j, float f, float f2, float f3, int[] iArr, float[] fArr, int i);

    private static native long nativeCreate2(long j, float f, float f2, float f3, int i, int i2, int i3);

    public RadialGradient(float centerX, float centerY, float radius, int[] colors, float[] stops, Shader.TileMode tileMode) {
        if (radius <= 0.0f) {
            throw new IllegalArgumentException("radius must be > 0");
        } else if (colors.length < 2) {
            throw new IllegalArgumentException("needs >= 2 number of colors");
        } else if (stops == null || colors.length == stops.length) {
            this.mType = 1;
            this.mX = centerX;
            this.mY = centerY;
            this.mRadius = radius;
            this.mColors = (int[]) colors.clone();
            this.mPositions = stops != null ? (float[]) stops.clone() : null;
            this.mTileMode = tileMode;
        } else {
            throw new IllegalArgumentException("color and position arrays must be of equal length");
        }
    }

    public RadialGradient(float centerX, float centerY, float radius, int centerColor, int edgeColor, Shader.TileMode tileMode) {
        if (radius > 0.0f) {
            this.mType = 2;
            this.mX = centerX;
            this.mY = centerY;
            this.mRadius = radius;
            this.mCenterColor = centerColor;
            this.mEdgeColor = edgeColor;
            this.mTileMode = tileMode;
            return;
        }
        throw new IllegalArgumentException("radius must be > 0");
    }

    /* access modifiers changed from: package-private */
    public long createNativeInstance(long nativeMatrix) {
        if (this.mType == 1) {
            return nativeCreate1(nativeMatrix, this.mX, this.mY, this.mRadius, this.mColors, this.mPositions, this.mTileMode.nativeInt);
        }
        return nativeCreate2(nativeMatrix, this.mX, this.mY, this.mRadius, this.mCenterColor, this.mEdgeColor, this.mTileMode.nativeInt);
    }

    /* access modifiers changed from: protected */
    public Shader copy() {
        RadialGradient copy;
        if (this.mType == 1) {
            copy = new RadialGradient(this.mX, this.mY, this.mRadius, (int[]) this.mColors.clone(), this.mPositions != null ? (float[]) this.mPositions.clone() : null, this.mTileMode);
        } else {
            copy = new RadialGradient(this.mX, this.mY, this.mRadius, this.mCenterColor, this.mEdgeColor, this.mTileMode);
        }
        copyLocalMatrix(copy);
        return copy;
    }
}
