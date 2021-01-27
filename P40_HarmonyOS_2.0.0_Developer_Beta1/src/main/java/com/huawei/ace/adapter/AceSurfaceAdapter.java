package com.huawei.ace.adapter;

import android.view.Surface;
import ohos.agp.components.surfaceview.adapter.SurfaceUtils;

public class AceSurfaceAdapter {
    private final Surface surface;

    public AceSurfaceAdapter(Surface surface2) {
        this.surface = surface2;
    }

    public Surface getASurface() {
        return this.surface;
    }

    public ohos.agp.graphics.Surface getHarmonySurface() {
        return SurfaceUtils.getSurface(this.surface);
    }
}
