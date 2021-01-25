package android.graphics;

import android.graphics.BitmapFactory;

public final class LargeBitmap {
    private long mNativeLargeBitmap;
    private boolean mRecycled = false;

    private static native void nativeClean(long j);

    private static native Bitmap nativeDecodeRegion(long j, int i, int i2, int i3, int i4, BitmapFactory.Options options);

    private static native int nativeGetHeight(long j);

    private static native int nativeGetWidth(long j);

    private LargeBitmap(long nativeLbm) {
        this.mNativeLargeBitmap = nativeLbm;
    }

    public Bitmap decodeRegion(Rect rect, BitmapFactory.Options options) {
        checkRecycled("decodeRegion called on recycled large bitmap");
        if (rect.left >= 0 && rect.top >= 0 && rect.right <= getWidth() && rect.bottom <= getHeight()) {
            return nativeDecodeRegion(this.mNativeLargeBitmap, rect.left, rect.top, rect.right - rect.left, rect.bottom - rect.top, options);
        }
        throw new IllegalArgumentException("rectangle is not inside the image");
    }

    public int getWidth() {
        checkRecycled("getWidth called on recycled large bitmap");
        return nativeGetWidth(this.mNativeLargeBitmap);
    }

    public int getHeight() {
        checkRecycled("getHeight called on recycled large bitmap");
        return nativeGetHeight(this.mNativeLargeBitmap);
    }

    public void recycle() {
        if (!this.mRecycled) {
            nativeClean(this.mNativeLargeBitmap);
            this.mRecycled = true;
        }
    }

    public final boolean isRecycled() {
        return this.mRecycled;
    }

    private void checkRecycled(String errorMessage) {
        if (this.mRecycled) {
            throw new IllegalStateException(errorMessage);
        }
    }

    /* access modifiers changed from: protected */
    public void finalize() {
        recycle();
    }
}
