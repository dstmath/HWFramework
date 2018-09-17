package android.graphics;

public class SweepGradient extends Shader {
    private static final int TYPE_COLORS_AND_POSITIONS = 1;
    private static final int TYPE_COLOR_START_AND_COLOR_END = 2;
    private int mColor0;
    private int mColor1;
    private int[] mColors;
    private float mCx;
    private float mCy;
    private float[] mPositions;
    private int mType;

    private static native long nativeCreate1(float f, float f2, int[] iArr, float[] fArr);

    private static native long nativeCreate2(float f, float f2, int i, int i2);

    public SweepGradient(float cx, float cy, int[] colors, float[] positions) {
        if (colors.length < TYPE_COLOR_START_AND_COLOR_END) {
            throw new IllegalArgumentException("needs >= 2 number of colors");
        } else if (positions == null || colors.length == positions.length) {
            this.mType = TYPE_COLORS_AND_POSITIONS;
            this.mCx = cx;
            this.mCy = cy;
            this.mColors = colors;
            this.mPositions = positions;
            init(nativeCreate1(cx, cy, colors, positions));
        } else {
            throw new IllegalArgumentException("color and position arrays must be of equal length");
        }
    }

    public SweepGradient(float cx, float cy, int color0, int color1) {
        this.mType = TYPE_COLOR_START_AND_COLOR_END;
        this.mCx = cx;
        this.mCy = cy;
        this.mColor0 = color0;
        this.mColor1 = color1;
        init(nativeCreate2(cx, cy, color0, color1));
    }

    protected Shader copy() {
        SweepGradient copy;
        float[] fArr = null;
        switch (this.mType) {
            case TYPE_COLORS_AND_POSITIONS /*1*/:
                float f = this.mCx;
                float f2 = this.mCy;
                int[] iArr = (int[]) this.mColors.clone();
                if (this.mPositions != null) {
                    fArr = (float[]) this.mPositions.clone();
                }
                copy = new SweepGradient(f, f2, iArr, fArr);
                break;
            case TYPE_COLOR_START_AND_COLOR_END /*2*/:
                copy = new SweepGradient(this.mCx, this.mCy, this.mColor0, this.mColor1);
                break;
            default:
                throw new IllegalArgumentException("SweepGradient should be created with either colors and positions or start color and end color");
        }
        copyLocalMatrix(copy);
        return copy;
    }
}
