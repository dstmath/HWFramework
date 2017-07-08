package android.view;

import android.graphics.Bitmap;
import android.os.Handler;

public final class PixelCopy {
    public static final int ERROR_DESTINATION_INVALID = 5;
    public static final int ERROR_SOURCE_INVALID = 4;
    public static final int ERROR_SOURCE_NO_DATA = 3;
    public static final int ERROR_TIMEOUT = 2;
    public static final int ERROR_UNKNOWN = 1;
    public static final int SUCCESS = 0;

    /* renamed from: android.view.PixelCopy.1 */
    static class AnonymousClass1 implements Runnable {
        final /* synthetic */ OnPixelCopyFinishedListener val$listener;
        final /* synthetic */ int val$result;

        AnonymousClass1(OnPixelCopyFinishedListener val$listener, int val$result) {
            this.val$listener = val$listener;
            this.val$result = val$result;
        }

        public void run() {
            this.val$listener.onPixelCopyFinished(this.val$result);
        }
    }

    public interface OnPixelCopyFinishedListener {
        void onPixelCopyFinished(int i);
    }

    public static void request(SurfaceView source, Bitmap dest, OnPixelCopyFinishedListener listener, Handler listenerThread) {
        request(source.getHolder().getSurface(), dest, listener, listenerThread);
    }

    public static void request(Surface source, Bitmap dest, OnPixelCopyFinishedListener listener, Handler listenerThread) {
        validateBitmapDest(dest);
        if (source.isValid()) {
            listenerThread.post(new AnonymousClass1(listener, ThreadedRenderer.copySurfaceInto(source, dest)));
            return;
        }
        throw new IllegalArgumentException("Surface isn't valid, source.isValid() == false");
    }

    private static void validateBitmapDest(Bitmap bitmap) {
        if (bitmap == null) {
            throw new IllegalArgumentException("Bitmap cannot be null");
        } else if (bitmap.isRecycled()) {
            throw new IllegalArgumentException("Bitmap is recycled");
        } else if (!bitmap.isMutable()) {
            throw new IllegalArgumentException("Bitmap is immutable");
        }
    }

    private PixelCopy() {
    }
}
