package android.view;

import android.graphics.Canvas;
import android.graphics.Rect;

public interface SurfaceHolder {
    @Deprecated
    public static final int SURFACE_TYPE_GPU = 2;
    @Deprecated
    public static final int SURFACE_TYPE_HARDWARE = 1;
    @Deprecated
    public static final int SURFACE_TYPE_NORMAL = 0;
    @Deprecated
    public static final int SURFACE_TYPE_PUSH_BUFFERS = 3;

    public interface Callback {
        void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i2, int i3);

        void surfaceCreated(SurfaceHolder surfaceHolder);

        void surfaceDestroyed(SurfaceHolder surfaceHolder);
    }

    public static class BadSurfaceTypeException extends RuntimeException {
        public BadSurfaceTypeException(String name) {
            super(name);
        }
    }

    public interface Callback2 extends Callback {
        void surfaceRedrawNeeded(SurfaceHolder surfaceHolder);

        void surfaceRedrawNeededAsync(SurfaceHolder holder, Runnable drawingFinished) {
            surfaceRedrawNeeded(holder);
            drawingFinished.run();
        }
    }

    void addCallback(Callback callback);

    Surface getSurface();

    Rect getSurfaceFrame();

    boolean isCreating();

    Canvas lockCanvas();

    Canvas lockCanvas(Rect rect);

    void removeCallback(Callback callback);

    void setFixedSize(int i, int i2);

    void setFormat(int i);

    void setKeepScreenOn(boolean z);

    void setSizeFromLayout();

    @Deprecated
    void setType(int i);

    void unlockCanvasAndPost(Canvas canvas);

    Canvas lockHardwareCanvas() {
        throw new IllegalStateException("This SurfaceHolder doesn't support lockHardwareCanvas");
    }
}
