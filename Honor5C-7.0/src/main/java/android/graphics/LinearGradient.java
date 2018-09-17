package android.graphics;

import android.graphics.Shader.TileMode;

public class LinearGradient extends Shader {
    private static final int TYPE_COLORS_AND_POSITIONS = 1;
    private static final int TYPE_COLOR_START_AND_COLOR_END = 2;
    private int mColor0;
    private int mColor1;
    private int[] mColors;
    private float[] mPositions;
    private TileMode mTileMode;
    private int mType;
    private float mX0;
    private float mX1;
    private float mY0;
    private float mY1;

    private native long nativeCreate1(float f, float f2, float f3, float f4, int[] iArr, float[] fArr, int i);

    private native long nativeCreate2(float f, float f2, float f3, float f4, int i, int i2, int i3);

    public LinearGradient(float x0, float y0, float x1, float y1, int[] colors, float[] positions, TileMode tile) {
        if (colors.length < TYPE_COLOR_START_AND_COLOR_END) {
            throw new IllegalArgumentException("needs >= 2 number of colors");
        } else if (positions == null || colors.length == positions.length) {
            this.mType = TYPE_COLORS_AND_POSITIONS;
            this.mX0 = x0;
            this.mY0 = y0;
            this.mX1 = x1;
            this.mY1 = y1;
            this.mColors = colors;
            this.mPositions = positions;
            this.mTileMode = tile;
            init(nativeCreate1(x0, y0, x1, y1, colors, positions, tile.nativeInt));
        } else {
            throw new IllegalArgumentException("color and position arrays must be of equal length");
        }
    }

    public LinearGradient(float x0, float y0, float x1, float y1, int color0, int color1, TileMode tile) {
        this.mType = TYPE_COLOR_START_AND_COLOR_END;
        this.mX0 = x0;
        this.mY0 = y0;
        this.mX1 = x1;
        this.mY1 = y1;
        this.mColor0 = color0;
        this.mColor1 = color1;
        this.mTileMode = tile;
        init(nativeCreate2(x0, y0, x1, y1, color0, color1, tile.nativeInt));
    }

    protected Shader copy() {
        LinearGradient copy;
        float[] fArr = null;
        switch (this.mType) {
            case TYPE_COLORS_AND_POSITIONS /*1*/:
                float f = this.mX0;
                float f2 = this.mY0;
                float f3 = this.mX1;
                float f4 = this.mY1;
                int[] iArr = (int[]) this.mColors.clone();
                if (this.mPositions != null) {
                    fArr = (float[]) this.mPositions.clone();
                }
                copy = new LinearGradient(f, f2, f3, f4, iArr, fArr, this.mTileMode);
                break;
            case TYPE_COLOR_START_AND_COLOR_END /*2*/:
                copy = new LinearGradient(this.mX0, this.mY0, this.mX1, this.mY1, this.mColor0, this.mColor1, this.mTileMode);
                break;
            default:
                throw new IllegalArgumentException("LinearGradient should be created with either colors and positions or start color and end color");
        }
        copyLocalMatrix(copy);
        return copy;
    }
}
