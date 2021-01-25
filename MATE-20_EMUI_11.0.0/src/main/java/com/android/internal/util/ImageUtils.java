package com.android.internal.util;

import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ImageDecoder;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Size;
import java.io.IOException;
import java.util.concurrent.Callable;

public class ImageUtils {
    private static final int ALPHA_TOLERANCE = 50;
    private static final int COMPACT_BITMAP_SIZE = 64;
    private static final int TOLERANCE = 20;
    private int[] mTempBuffer;
    private Bitmap mTempCompactBitmap;
    private Canvas mTempCompactBitmapCanvas;
    private Paint mTempCompactBitmapPaint;
    private final Matrix mTempMatrix = new Matrix();

    public boolean isGrayscale(Bitmap bitmap) {
        int height = bitmap.getHeight();
        int width = bitmap.getWidth();
        if (height > 64 || width > 64) {
            if (this.mTempCompactBitmap == null) {
                this.mTempCompactBitmap = Bitmap.createBitmap(64, 64, Bitmap.Config.ARGB_8888);
                this.mTempCompactBitmapCanvas = new Canvas(this.mTempCompactBitmap);
                this.mTempCompactBitmapPaint = new Paint(1);
                this.mTempCompactBitmapPaint.setFilterBitmap(true);
            }
            this.mTempMatrix.reset();
            this.mTempMatrix.setScale(64.0f / ((float) width), 64.0f / ((float) height), 0.0f, 0.0f);
            this.mTempCompactBitmapCanvas.drawColor(0, PorterDuff.Mode.SRC);
            this.mTempCompactBitmapCanvas.drawBitmap(bitmap, this.mTempMatrix, this.mTempCompactBitmapPaint);
            bitmap = this.mTempCompactBitmap;
            height = 64;
            width = 64;
        }
        int size = height * width;
        ensureBufferSize(size);
        bitmap.getPixels(this.mTempBuffer, 0, width, 0, 0, width, height);
        for (int i = 0; i < size; i++) {
            if (!isGrayscale(this.mTempBuffer[i])) {
                return false;
            }
        }
        return true;
    }

    private void ensureBufferSize(int size) {
        int[] iArr = this.mTempBuffer;
        if (iArr == null || iArr.length < size) {
            this.mTempBuffer = new int[size];
        }
    }

    public static boolean isGrayscale(int color) {
        if (((color >> 24) & 255) < 50) {
            return true;
        }
        int r = (color >> 16) & 255;
        int g = (color >> 8) & 255;
        int b = color & 255;
        if (Math.abs(r - g) >= 20 || Math.abs(r - b) >= 20 || Math.abs(g - b) >= 20) {
            return false;
        }
        return true;
    }

    public static Bitmap buildScaledBitmap(Drawable drawable, int maxWidth, int maxHeight) {
        if (drawable == null) {
            return null;
        }
        int originalWidth = drawable.getIntrinsicWidth();
        int originalHeight = drawable.getIntrinsicHeight();
        if (originalWidth <= maxWidth && originalHeight <= maxHeight && (drawable instanceof BitmapDrawable)) {
            return ((BitmapDrawable) drawable).getBitmap();
        }
        if (originalHeight <= 0 || originalWidth <= 0) {
            return null;
        }
        float ratio = Math.min(1.0f, Math.min(((float) maxWidth) / ((float) originalWidth), ((float) maxHeight) / ((float) originalHeight)));
        int scaledWidth = (int) (((float) originalWidth) * ratio);
        int scaledHeight = (int) (((float) originalHeight) * ratio);
        Bitmap result = Bitmap.createBitmap(scaledWidth, scaledHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);
        drawable.setBounds(0, 0, scaledWidth, scaledHeight);
        drawable.draw(canvas);
        return result;
    }

    public static int calculateSampleSize(Size currentSize, Size requestedSize) {
        int inSampleSize = 1;
        if (currentSize.getHeight() > requestedSize.getHeight() || currentSize.getWidth() > requestedSize.getWidth()) {
            int halfHeight = currentSize.getHeight() / 2;
            int halfWidth = currentSize.getWidth() / 2;
            while (halfHeight / inSampleSize >= requestedSize.getHeight() && halfWidth / inSampleSize >= requestedSize.getWidth()) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x002d, code lost:
        if (r0 != null) goto L_0x002f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:?, code lost:
        r0.close();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0033, code lost:
        r3 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:14:0x0034, code lost:
        r1.addSuppressed(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0037, code lost:
        throw r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:9:0x002c, code lost:
        r2 = move-exception;
     */
    public static Bitmap loadThumbnail(ContentResolver resolver, Uri uri, Size size) throws IOException {
        ContentProviderClient client = resolver.acquireContentProviderClient(uri);
        Bundle opts = new Bundle();
        opts.putParcelable(ContentResolver.EXTRA_SIZE, Point.convert(size));
        Bitmap decodeBitmap = ImageDecoder.decodeBitmap(ImageDecoder.createSource(new Callable(uri, opts) {
            /* class com.android.internal.util.$$Lambda$ImageUtils$UJyN8OeHYbkY_xJzm1U3D7W4PNY */
            private final /* synthetic */ Uri f$1;
            private final /* synthetic */ Bundle f$2;

            {
                this.f$1 = r2;
                this.f$2 = r3;
            }

            @Override // java.util.concurrent.Callable
            public final Object call() {
                return ContentProviderClient.this.openTypedAssetFile(this.f$1, "image/*", this.f$2, null);
            }
        }), new ImageDecoder.OnHeaderDecodedListener() {
            /* class com.android.internal.util.$$Lambda$ImageUtils$rnRZcgsdC1BtH9FpHTN2Kf_FXwE */

            @Override // android.graphics.ImageDecoder.OnHeaderDecodedListener
            public final void onHeaderDecoded(ImageDecoder imageDecoder, ImageDecoder.ImageInfo imageInfo, ImageDecoder.Source source) {
                ImageUtils.lambda$loadThumbnail$1(Size.this, imageDecoder, imageInfo, source);
            }
        });
        if (client != null) {
            client.close();
        }
        return decodeBitmap;
    }

    static /* synthetic */ void lambda$loadThumbnail$1(Size size, ImageDecoder decoder, ImageDecoder.ImageInfo info, ImageDecoder.Source source) {
        decoder.setAllocator(1);
        int sample = calculateSampleSize(info.getSize(), size);
        if (sample > 1) {
            decoder.setTargetSampleSize(sample);
        }
    }
}
