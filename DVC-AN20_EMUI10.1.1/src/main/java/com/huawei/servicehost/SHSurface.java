package com.huawei.servicehost;

import android.util.Size;
import android.view.Surface;

public class SHSurface {
    public int cameraId;
    public Size size;
    public Surface surface;
    public SurfaceType type;

    public int getCameraId() {
        return this.cameraId;
    }

    public void setCameraId(int cameraId2) {
        this.cameraId = cameraId2;
    }

    public Size getSize() {
        return this.size;
    }

    public void setSize(Size size2) {
        this.size = size2;
    }

    public Surface getSurface() {
        return this.surface;
    }

    public void setSurface(Surface surface2) {
        this.surface = surface2;
    }

    public SurfaceType getType() {
        return this.type;
    }

    public void setType(SurfaceType type2) {
        this.type = type2;
    }
}
