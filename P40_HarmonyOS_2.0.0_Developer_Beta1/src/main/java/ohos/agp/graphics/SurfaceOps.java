package ohos.agp.graphics;

import ohos.agp.render.Canvas;
import ohos.agp.utils.Rect;

public interface SurfaceOps {

    public interface Callback {
        void surfaceChanged(SurfaceOps surfaceOps, int i, int i2, int i3);

        void surfaceCreated(SurfaceOps surfaceOps);

        void surfaceDestroyed(SurfaceOps surfaceOps);
    }

    void addCallback(Callback callback);

    Surface getSurface();

    Rect getSurfaceDimension();

    Canvas lockCanvas();

    void removeCallback(Callback callback);

    void setFixedSize(int i, int i2);

    void setFormat(int i);

    void setKeepScreenOn(boolean z);

    void unlockCanvasAndPost(Canvas canvas);
}
