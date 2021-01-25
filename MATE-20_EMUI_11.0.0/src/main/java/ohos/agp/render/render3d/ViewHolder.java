package ohos.agp.render.render3d;

import ohos.agp.components.Component;

public interface ViewHolder {

    public interface BitmapListener {
        void onBitmapLoadError();
    }

    public interface SurfaceListener {
        void onSurfaceAvailable();

        void onSurfaceDestroyed();

        void onSurfaceSizeUpdated(int i, int i2);
    }

    TargetBuffer getTargetBuffer();

    Component getView();

    void release();

    void setOpaque(boolean z);

    void setSurfaceListener(SurfaceListener surfaceListener);
}
