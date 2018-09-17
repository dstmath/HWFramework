package android.graphics;

import android.graphics.Shader.TileMode;

public class BitmapShader extends Shader {
    public Bitmap mBitmap;
    private int mTileX;
    private int mTileY;

    private static native long nativeCreate(long j, Bitmap bitmap, int i, int i2);

    public BitmapShader(Bitmap bitmap, TileMode tileX, TileMode tileY) {
        this(bitmap, tileX.nativeInt, tileY.nativeInt);
    }

    private BitmapShader(Bitmap bitmap, int tileX, int tileY) {
        if (bitmap == null) {
            throw new IllegalArgumentException("Bitmap must be non-null");
        } else if (bitmap != this.mBitmap || tileX != this.mTileX || tileY != this.mTileY) {
            this.mBitmap = bitmap;
            this.mTileX = tileX;
            this.mTileY = tileY;
        }
    }

    long createNativeInstance(long nativeMatrix) {
        return nativeCreate(nativeMatrix, this.mBitmap, this.mTileX, this.mTileY);
    }

    protected Shader copy() {
        BitmapShader copy = new BitmapShader(this.mBitmap, this.mTileX, this.mTileY);
        copyLocalMatrix(copy);
        return copy;
    }
}
