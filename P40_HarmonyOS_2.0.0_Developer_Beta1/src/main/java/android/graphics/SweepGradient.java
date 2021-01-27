package android.graphics;

import android.annotation.UnsupportedAppUsage;
import android.graphics.ColorSpace;

public class SweepGradient extends Shader {
    @UnsupportedAppUsage
    private int mColor0;
    @UnsupportedAppUsage
    private int mColor1;
    private final long[] mColorLongs;
    @UnsupportedAppUsage
    private int[] mColors;
    @UnsupportedAppUsage
    private float mCx;
    @UnsupportedAppUsage
    private float mCy;
    @UnsupportedAppUsage
    private float[] mPositions;

    private static native long nativeCreate(long j, float f, float f2, long[] jArr, float[] fArr, long j2);

    public SweepGradient(float cx, float cy, int[] colors, float[] positions) {
        this(cx, cy, convertColors(colors), positions, ColorSpace.get(ColorSpace.Named.SRGB));
    }

    public SweepGradient(float cx, float cy, long[] colors, float[] positions) {
        this(cx, cy, (long[]) colors.clone(), positions, detectColorSpace(colors));
    }

    private SweepGradient(float cx, float cy, long[] colors, float[] positions, ColorSpace colorSpace) {
        super(colorSpace);
        if (positions == null || colors.length == positions.length) {
            this.mCx = cx;
            this.mCy = cy;
            this.mColorLongs = colors;
            this.mPositions = positions != null ? (float[]) positions.clone() : null;
            return;
        }
        throw new IllegalArgumentException("color and position arrays must be of equal length");
    }

    public SweepGradient(float cx, float cy, int color0, int color1) {
        this(cx, cy, Color.pack(color0), Color.pack(color1));
    }

    public SweepGradient(float cx, float cy, long color0, long color1) {
        this(cx, cy, new long[]{color0, color1}, (float[]) null);
    }

    /* access modifiers changed from: package-private */
    @Override // android.graphics.Shader
    public long createNativeInstance(long nativeMatrix) {
        return nativeCreate(nativeMatrix, this.mCx, this.mCy, this.mColorLongs, this.mPositions, colorSpace().getNativeInstance());
    }
}
