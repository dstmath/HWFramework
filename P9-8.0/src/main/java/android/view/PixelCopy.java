package android.view;

import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Handler;

public final class PixelCopy {
    public static final int ERROR_DESTINATION_INVALID = 5;
    public static final int ERROR_SOURCE_INVALID = 4;
    public static final int ERROR_SOURCE_NO_DATA = 3;
    public static final int ERROR_TIMEOUT = 2;
    public static final int ERROR_UNKNOWN = 1;
    public static final int SUCCESS = 0;

    public interface OnPixelCopyFinishedListener {
        void onPixelCopyFinished(int i);
    }

    public static void request(SurfaceView source, Bitmap dest, OnPixelCopyFinishedListener listener, Handler listenerThread) {
        request(source.getHolder().getSurface(), dest, listener, listenerThread);
    }

    public static void request(SurfaceView source, Rect srcRect, Bitmap dest, OnPixelCopyFinishedListener listener, Handler listenerThread) {
        request(source.getHolder().getSurface(), srcRect, dest, listener, listenerThread);
    }

    public static void request(Surface source, Bitmap dest, OnPixelCopyFinishedListener listener, Handler listenerThread) {
        request(source, null, dest, listener, listenerThread);
    }

    public static void request(Surface source, Rect srcRect, Bitmap dest, final OnPixelCopyFinishedListener listener, Handler listenerThread) {
        validateBitmapDest(dest);
        if (!source.isValid()) {
            throw new IllegalArgumentException("Surface isn't valid, source.isValid() == false");
        } else if (srcRect == null || !srcRect.isEmpty()) {
            final int result = ThreadedRenderer.copySurfaceInto(source, srcRect, dest);
            listenerThread.post(new Runnable() {
                public void run() {
                    listener.onPixelCopyFinished(result);
                }
            });
        } else {
            throw new IllegalArgumentException("sourceRect is empty");
        }
    }

    public static void request(Window source, Bitmap dest, OnPixelCopyFinishedListener listener, Handler listenerThread) {
        request(source, null, dest, listener, listenerThread);
    }

    public static void request(Window source, Rect srcRect, Bitmap dest, OnPixelCopyFinishedListener listener, Handler listenerThread) {
        validateBitmapDest(dest);
        if (source == null) {
            throw new IllegalArgumentException("source is null");
        } else if (source.peekDecorView() == null) {
            throw new IllegalArgumentException("Only able to copy windows with decor views");
        } else {
            Surface surface = null;
            if (source.peekDecorView().getViewRootImpl() != null) {
                surface = source.peekDecorView().getViewRootImpl().mSurface;
            }
            if (surface == null || (surface.isValid() ^ 1) != 0) {
                throw new IllegalArgumentException("Window doesn't have a backing surface!");
            }
            request(surface, srcRect, dest, listener, listenerThread);
        }
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
