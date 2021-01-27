package android.graphics;

import android.annotation.UnsupportedAppUsage;
import android.content.res.AssetManager;
import android.graphics.BitmapFactory;
import android.os.SystemProperties;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Semaphore;

public final class BitmapRegionDecoder {
    private static final boolean IS_M_PLATFORM = SystemProperties.get("ro.board.platform", "unknow").startsWith("mt6853");
    private Semaphore mDecodeRegionSem = new Semaphore(4);
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
        if (IS_M_PLATFORM) {
            return decodeRegionOverride(rect, options);
        }
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

    public Bitmap decodeRegionOverride(Rect rect, BitmapFactory.Options options) {
        BitmapFactory.Options.validate(options);
        checkRecycled("decodeRegion called on recycled region decoder");
        if (rect.right <= 0 || rect.bottom <= 0 || rect.left >= getWidth() || rect.top >= getHeight()) {
            throw new IllegalArgumentException("rectangle is outside the image");
        }
        Bitmap bitmap = null;
        try {
            this.mDecodeRegionSem.acquire();
            bitmap = nativeDecodeRegion(this.mNativeBitmapRegionDecoder, rect.left, rect.top, rect.right - rect.left, rect.bottom - rect.top, options, BitmapFactory.Options.nativeInBitmap(options), BitmapFactory.Options.nativeColorSpace(options));
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Throwable th) {
            this.mDecodeRegionSem.release();
            throw th;
        }
        this.mDecodeRegionSem.release();
        return bitmap;
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
            if (IS_M_PLATFORM) {
                int mSleepCount = 0;
                while (this.mDecodeRegionSem.availablePermits() < 4 && mSleepCount < 450) {
                    try {
                        Thread.sleep(10);
                        mSleepCount++;
                    } catch (Exception e) {
                    }
                }
            }
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
