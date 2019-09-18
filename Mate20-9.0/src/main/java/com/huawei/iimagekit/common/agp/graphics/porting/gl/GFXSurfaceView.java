package com.huawei.iimagekit.common.agp.graphics.porting.gl;

import android.content.Context;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;

public class GFXSurfaceView extends GLSurfaceView {
    public GFXSurfaceView(Context context) {
        super(context);
        doInit();
    }

    public GFXSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        doInit();
    }

    private void doInit() {
        setEGLContextClientVersion(3);
        setEGLConfigChooser(8, 8, 8, 8, 0, 0);
        getHolder().setFixedSize(1080, 1920);
    }

    public void invalidate() {
        requestRender();
    }
}
