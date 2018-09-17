package android.graphics;

import android.graphics.Shader.TileMode;

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

    private static native long nativeCreate1(float f, float f2, float f3, int[] iArr, float[] fArr, int i);

    private static native long nativeCreate2(float f, float f2, float f3, int i, int i2, int i3);

    public RadialGradient(float centerX, float centerY, float radius, int[] colors, float[] stops, TileMode tileMode) {
        if (radius <= 0.0f) {
            throw new IllegalArgumentException("radius must be > 0");
        } else if (colors.length < TYPE_COLOR_CENTER_AND_COLOR_EDGE) {
            throw new IllegalArgumentException("needs >= 2 number of colors");
        } else if (stops == null || colors.length == stops.length) {
            this.mType = TYPE_COLORS_AND_POSITIONS;
            this.mX = centerX;
            this.mY = centerY;
            this.mRadius = radius;
            this.mColors = colors;
            this.mPositions = stops;
            this.mTileMode = tileMode;
            init(nativeCreate1(centerX, centerY, radius, colors, stops, tileMode.nativeInt));
        } else {
            throw new IllegalArgumentException("color and position arrays must be of equal length");
        }
    }

    public RadialGradient(float centerX, float centerY, float radius, int centerColor, int edgeColor, TileMode tileMode) {
        if (radius <= 0.0f) {
            throw new IllegalArgumentException("radius must be > 0");
        }
        this.mType = TYPE_COLOR_CENTER_AND_COLOR_EDGE;
        this.mX = centerX;
        this.mY = centerY;
        this.mRadius = radius;
        this.mCenterColor = centerColor;
        this.mEdgeColor = edgeColor;
        this.mTileMode = tileMode;
        init(nativeCreate2(centerX, centerY, radius, centerColor, edgeColor, tileMode.nativeInt));
    }

    protected Shader copy() {
        RadialGradient copy;
        float[] fArr = null;
        switch (this.mType) {
            case TYPE_COLORS_AND_POSITIONS /*1*/:
                float f = this.mX;
                float f2 = this.mY;
                float f3 = this.mRadius;
                int[] iArr = (int[]) this.mColors.clone();
                if (this.mPositions != null) {
                    fArr = (float[]) this.mPositions.clone();
                }
                copy = new RadialGradient(f, f2, f3, iArr, fArr, this.mTileMode);
                break;
            case TYPE_COLOR_CENTER_AND_COLOR_EDGE /*2*/:
                copy = new RadialGradient(this.mX, this.mY, this.mRadius, this.mCenterColor, this.mEdgeColor, this.mTileMode);
                break;
            default:
                throw new IllegalArgumentException("RadialGradient should be created with either colors and positions or center color and edge color");
        }
        copyLocalMatrix(copy);
        return copy;
    }
}
