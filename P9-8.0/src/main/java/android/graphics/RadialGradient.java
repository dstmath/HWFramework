package android.graphics;

import android.graphics.Shader.TileMode;
import android.hardware.camera2.params.TonemapCurve;

public class RadialGradient extends Shader {
    private static final int TYPE_COLORS_AND_POSITIONS = 1;
    private static final int TYPE_COLOR_CENTER_AND_COLOR_EDGE = 2;
    private int mCenterColor;
    private int[] mColors;
    private int mEdgeColor;
    private float[] mPositions;
    private float mRadius;
    private TileMode mTileMode;
    private int mType;
    private float mX;
    private float mY;

    private static native long nativeCreate1(long j, float f, float f2, float f3, int[] iArr, float[] fArr, int i);

    private static native long nativeCreate2(long j, float f, float f2, float f3, int i, int i2, int i3);

    public RadialGradient(float centerX, float centerY, float radius, int[] colors, float[] stops, TileMode tileMode) {
        if (radius <= TonemapCurve.LEVEL_BLACK) {
            throw new IllegalArgumentException("radius must be > 0");
        } else if (colors.length < 2) {
            throw new IllegalArgumentException("needs >= 2 number of colors");
        } else if (stops == null || colors.length == stops.length) {
            float[] fArr;
            this.mType = 1;
            this.mX = centerX;
            this.mY = centerY;
            this.mRadius = radius;
            this.mColors = (int[]) colors.clone();
            if (stops != null) {
                fArr = (float[]) stops.clone();
            } else {
                fArr = null;
            }
            this.mPositions = fArr;
            this.mTileMode = tileMode;
        } else {
            throw new IllegalArgumentException("color and position arrays must be of equal length");
        }
    }

    public RadialGradient(float centerX, float centerY, float radius, int centerColor, int edgeColor, TileMode tileMode) {
        if (radius <= TonemapCurve.LEVEL_BLACK) {
            throw new IllegalArgumentException("radius must be > 0");
        }
        this.mType = 2;
        this.mX = centerX;
        this.mY = centerY;
        this.mRadius = radius;
        this.mCenterColor = centerColor;
        this.mEdgeColor = edgeColor;
        this.mTileMode = tileMode;
    }

    long createNativeInstance(long nativeMatrix) {
        if (this.mType == 1) {
            return nativeCreate1(nativeMatrix, this.mX, this.mY, this.mRadius, this.mColors, this.mPositions, this.mTileMode.nativeInt);
        }
        return nativeCreate2(nativeMatrix, this.mX, this.mY, this.mRadius, this.mCenterColor, this.mEdgeColor, this.mTileMode.nativeInt);
    }

    protected Shader copy() {
        RadialGradient copy;
        float[] fArr = null;
        if (this.mType == 1) {
            float f = this.mX;
            float f2 = this.mY;
            float f3 = this.mRadius;
            int[] iArr = (int[]) this.mColors.clone();
            if (this.mPositions != null) {
                fArr = (float[]) this.mPositions.clone();
            }
            copy = new RadialGradient(f, f2, f3, iArr, fArr, this.mTileMode);
        } else {
            copy = new RadialGradient(this.mX, this.mY, this.mRadius, this.mCenterColor, this.mEdgeColor, this.mTileMode);
        }
        copyLocalMatrix(copy);
        return copy;
    }
}
