package android.graphics;

import android.annotation.UnsupportedAppUsage;

public class NinePatch {
    @UnsupportedAppUsage
    private final Bitmap mBitmap;
    @UnsupportedAppUsage
    public long mNativeChunk;
    private Paint mPaint;
    private String mSrcName;

    public static native boolean isNinePatchChunk(byte[] bArr);

    private static native void nativeFinalize(long j);

    private static native long nativeGetTransparentRegion(long j, long j2, Rect rect);

    private static native long validateNinePatchChunk(byte[] bArr);

    public static class InsetStruct {
        public final Rect opticalRect;
        public final float outlineAlpha;
        public final float outlineRadius;
        public final Rect outlineRect;

        @UnsupportedAppUsage
        InsetStruct(int opticalLeft, int opticalTop, int opticalRight, int opticalBottom, int outlineLeft, int outlineTop, int outlineRight, int outlineBottom, float outlineRadius2, int outlineAlpha2, float decodeScale) {
            this.opticalRect = new Rect(opticalLeft, opticalTop, opticalRight, opticalBottom);
            this.opticalRect.scale(decodeScale);
            this.outlineRect = scaleInsets(outlineLeft, outlineTop, outlineRight, outlineBottom, decodeScale);
            this.outlineRadius = outlineRadius2 * decodeScale;
            this.outlineAlpha = ((float) outlineAlpha2) / 255.0f;
        }

        public static Rect scaleInsets(int left, int top, int right, int bottom, float scale) {
            if (scale == 1.0f) {
                return new Rect(left, top, right, bottom);
            }
            Rect result = new Rect();
            result.left = (int) Math.ceil((double) (((float) left) * scale));
            result.top = (int) Math.ceil((double) (((float) top) * scale));
            result.right = (int) Math.ceil((double) (((float) right) * scale));
            result.bottom = (int) Math.ceil((double) (((float) bottom) * scale));
            return result;
        }
    }

    public NinePatch(Bitmap bitmap, byte[] chunk) {
        this(bitmap, chunk, null);
    }

    public NinePatch(Bitmap bitmap, byte[] chunk, String srcName) {
        this.mBitmap = bitmap;
        this.mSrcName = srcName;
        this.mNativeChunk = validateNinePatchChunk(chunk);
    }

    public NinePatch(NinePatch patch) {
        this.mBitmap = patch.mBitmap;
        this.mSrcName = patch.mSrcName;
        Paint paint = patch.mPaint;
        if (paint != null) {
            this.mPaint = new Paint(paint);
        }
        this.mNativeChunk = patch.mNativeChunk;
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        try {
            if (this.mNativeChunk != 0) {
                nativeFinalize(this.mNativeChunk);
                this.mNativeChunk = 0;
            }
        } finally {
            super.finalize();
        }
    }

    public String getName() {
        return this.mSrcName;
    }

    public Paint getPaint() {
        return this.mPaint;
    }

    public void setPaint(Paint p) {
        this.mPaint = p;
    }

    public Bitmap getBitmap() {
        return this.mBitmap;
    }

    public void draw(Canvas canvas, RectF location) {
        canvas.drawPatch(this, location, this.mPaint);
    }

    public void draw(Canvas canvas, Rect location) {
        canvas.drawPatch(this, location, this.mPaint);
    }

    public void draw(Canvas canvas, Rect location, Paint paint) {
        canvas.drawPatch(this, location, paint);
    }

    public int getDensity() {
        return this.mBitmap.mDensity;
    }

    public int getWidth() {
        return this.mBitmap.getWidth();
    }

    public int getHeight() {
        return this.mBitmap.getHeight();
    }

    public final boolean hasAlpha() {
        return this.mBitmap.hasAlpha();
    }

    public final Region getTransparentRegion(Rect bounds) {
        long r = nativeGetTransparentRegion(this.mBitmap.getNativeInstance(), this.mNativeChunk, bounds);
        if (r != 0) {
            return new Region(r);
        }
        return null;
    }
}
