package android.graphics;

import android.annotation.UnsupportedAppUsage;
import android.graphics.ColorSpace;
import android.graphics.Shader;

public class LinearGradient extends Shader {
    @UnsupportedAppUsage
    private int mColor0;
    @UnsupportedAppUsage
    private int mColor1;
    private final long[] mColorLongs;
    @UnsupportedAppUsage
    private int[] mColors;
    @UnsupportedAppUsage
    private float[] mPositions;
    @UnsupportedAppUsage
    private Shader.TileMode mTileMode;
    @UnsupportedAppUsage
    private float mX0;
    @UnsupportedAppUsage
    private float mX1;
    @UnsupportedAppUsage
    private float mY0;
    @UnsupportedAppUsage
    private float mY1;

    private native long nativeCreate(long j, float f, float f2, float f3, float f4, long[] jArr, float[] fArr, int i, long j2);

    public LinearGradient(float x0, float y0, float x1, float y1, int[] colors, float[] positions, Shader.TileMode tile) {
        this(x0, y0, x1, y1, convertColors(colors), positions, tile, ColorSpace.get(ColorSpace.Named.SRGB));
    }

    public LinearGradient(float x0, float y0, float x1, float y1, long[] colors, float[] positions, Shader.TileMode tile) {
        this(x0, y0, x1, y1, (long[]) colors.clone(), positions, tile, detectColorSpace(colors));
    }

    private LinearGradient(float x0, float y0, float x1, float y1, long[] colors, float[] positions, Shader.TileMode tile, ColorSpace colorSpace) {
        super(colorSpace);
        if (positions == null || colors.length == positions.length) {
            this.mX0 = x0;
            this.mY0 = y0;
            this.mX1 = x1;
            this.mY1 = y1;
            this.mColorLongs = colors;
            this.mPositions = positions != null ? (float[]) positions.clone() : null;
            this.mTileMode = tile;
            return;
        }
        throw new IllegalArgumentException("color and position arrays must be of equal length");
    }

    public LinearGradient(float x0, float y0, float x1, float y1, int color0, int color1, Shader.TileMode tile) {
        this(x0, y0, x1, y1, Color.pack(color0), Color.pack(color1), tile);
    }

    public LinearGradient(float x0, float y0, float x1, float y1, long color0, long color1, Shader.TileMode tile) {
        this(x0, y0, x1, y1, new long[]{color0, color1}, (float[]) null, tile);
    }

    /* access modifiers changed from: package-private */
    @Override // android.graphics.Shader
    public long createNativeInstance(long nativeMatrix) {
        return nativeCreate(nativeMatrix, this.mX0, this.mY0, this.mX1, this.mY1, this.mColorLongs, this.mPositions, this.mTileMode.nativeInt, colorSpace().getNativeInstance());
    }
}
