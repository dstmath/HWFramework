package com.huawei.agpengine;

import android.graphics.Bitmap;
import android.view.View;

public interface ViewHolder {

    public interface BitmapListener {
        void onBitmapLoadDone(Bitmap bitmap);

        void onBitmapLoadError();
    }

    public interface SurfaceListener {
        void onSurfaceAvailable();

        void onSurfaceDestroyed();

        void onSurfaceSizeUpdated(int i, int i2);
    }

    TargetBuffer getTargetBuffer();

    View getView();

    void release();

    void requestViewAsBitmap(Bitmap bitmap, BitmapListener bitmapListener);

    void setOpaque(boolean z);

    void setSurfaceListener(SurfaceListener surfaceListener);
}
