package android.graphics;

import android.annotation.UnsupportedAppUsage;
import android.content.res.AssetManager;
import android.graphics.BitmapFactory;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public final class BitmapRegionDecoder {
    private long mNativeBitmapRegionDecoder;
    private final Object mNativeLock = new Object();
    private boolean mRecycled;

    private static native void nativeClean(long j);

    private static native Bitmap nativeDecodeRegion(long j, int i, int i2, int i3, int i4, BitmapFactory.Options options, long j2, long j3);

    private static native int nativeGetHeight(long j);

    private static native int nativeGetWidth(long j);

    private static native BitmapRegionDecoder nativeNewInstance(long j, boolean z);

    private static native BitmapRegionDecoder nativeNewInstance(FileDescriptor fileDescriptor, boolean z);

    private static native BitmapRegionDecoder nativeNewInstance(InputStream inputStream, byte[] bArr, boolean z);

    @UnsupportedAppUsage
    private static native BitmapRegionDecoder nativeNewInstance(byte[] bArr, int i, int i2, boolean z);

    public static BitmapRegionDecoder newInstance(byte[] data, int offset, int length, boolean isShareable) throws IOException {
        if ((offset | length) >= 0 && data.length >= offset + length) {
            return nativeNewInstance(data, offset, length, isShareable);
        }
        throw new ArrayIndexOutOfBoundsException();
    }

    public static BitmapRegionDecoder newInstance(FileDescriptor fd, boolean isShareable) throws IOException {
        return nativeNewInstance(fd, isShareable);
    }

    public static BitmapRegionDecoder newInstance(InputStream is, boolean isShareable) throws IOException {
        if (is instanceof AssetManager.AssetInputStream) {
            return nativeNewInstance(((AssetManager.AssetInputStream) is).getNativeAsset(), isShareable);
        }
        return nativeNewInstance(is, new byte[16384], isShareable);
    }

    public static BitmapRegionDecoder newInstance(String pathName, boolean isShareable) throws IOException {
        InputStream stream = null;
        try {
            stream = new FileInputStream(pathName);
            BitmapRegionDecoder decoder = newInstance(stream, isShareable);
            try {
                stream.close();
            } catch (IOException e) {
            }
            return decoder;
        } catch (Throwable th) {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e2) {
                }
            }
            throw th;
        }
    }

    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    private BitmapRegionDecoder(long decoder) {
        this.mNativeBitmapRegionDecoder = decoder;
        this.mRecycled = false;
    }

    public Bitmap decodeRegion(Rect rect, BitmapFactory.Options options) {
        Bitmap nativeDecodeRegion;
        BitmapFactory.Options.validate(options);
        synchronized (this.mNativeLock) {
            checkRecycled("decodeRegion called on recycled region decoder");
            if (rect.right <= 0 || rect.bottom <= 0 || rect.left >= getWidth() || rect.top >= getHeight()) {
                throw new IllegalArgumentException("rectangle is outside the image");
            }
            nativeDecodeRegion = nativeDecodeRegion(this.mNativeBitmapRegionDecoder, rect.left, rect.top, rect.right - rect.left, rect.bottom - rect.top, options, BitmapFactory.Options.nativeInBitmap(options), BitmapFactory.Options.nativeColorSpace(options));
        }
        return nativeDecodeRegion;
    }

    public int getWidth() {
        int nativeGetWidth;
        synchronized (this.mNativeLock) {
            checkRecycled("getWidth called on recycled region decoder");
            nativeGetWidth = nativeGetWidth(this.mNativeBitmapRegionDecoder);
        }
        return nativeGetWidth;
    }

    public int getHeight() {
        int nativeGetHeight;
        synchronized (this.mNativeLock) {
            checkRecycled("getHeight called on recycled region decoder");
            nativeGetHeight = nativeGetHeight(this.mNativeBitmapRegionDecoder);
        }
        return nativeGetHeight;
    }

    public void recycle() {
        synchronized (this.mNativeLock) {
            if (!this.mRecycled) {
                nativeClean(this.mNativeBitmapRegionDecoder);
                this.mRecycled = true;
            }
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
    public void finalize() throws Throwable {
        try {
            recycle();
        } finally {
            super.finalize();
        }
    }
}
