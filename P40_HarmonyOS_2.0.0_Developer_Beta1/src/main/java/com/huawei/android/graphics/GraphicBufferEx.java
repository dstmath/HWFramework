package com.huawei.android.graphics;

import android.graphics.Bitmap;
import android.graphics.ColorSpace;
import android.graphics.GraphicBuffer;
import android.graphics.Rect;
import android.view.SurfaceControl;

public class GraphicBufferEx {
    private GraphicBuffer mGraphicBuffer;

    public GraphicBuffer getGraphicBuffer() {
        return this.mGraphicBuffer;
    }

    public void setGraphicBuffer(GraphicBuffer graphicBuffer) {
        this.mGraphicBuffer = graphicBuffer;
    }

    public static GraphicBufferEx captureLayers(SurfaceControl surfaceControl, Rect rect, float scale) {
        if (surfaceControl == null) {
            return null;
        }
        GraphicBufferEx ex = new GraphicBufferEx();
        ex.setGraphicBuffer(SurfaceControl.captureLayers(surfaceControl.getHandle(), rect, scale).getGraphicBuffer());
        return ex;
    }

    public int getWidth() {
        GraphicBuffer graphicBuffer = this.mGraphicBuffer;
        if (graphicBuffer != null) {
            return graphicBuffer.getWidth();
        }
        return 0;
    }

    public int getHeight() {
        GraphicBuffer graphicBuffer = this.mGraphicBuffer;
        if (graphicBuffer != null) {
            return graphicBuffer.getHeight();
        }
        return 0;
    }

    public boolean isDestroyed() {
        GraphicBuffer graphicBuffer = this.mGraphicBuffer;
        if (graphicBuffer != null) {
            return graphicBuffer.isDestroyed();
        }
        return false;
    }

    public void destroy() {
        GraphicBuffer graphicBuffer = this.mGraphicBuffer;
        if (graphicBuffer != null) {
            graphicBuffer.destroy();
        }
    }

    public static Bitmap wrapHardwareBuffer(GraphicBufferEx graphicBuffer, ColorSpace colorSpace) {
        if (graphicBuffer == null) {
            return null;
        }
        return Bitmap.wrapHardwareBuffer(graphicBuffer.getGraphicBuffer(), colorSpace);
    }

    public static GraphicBufferEx createGraphicBufferHandle(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }
        GraphicBufferEx ext = new GraphicBufferEx();
        ext.setGraphicBuffer(bitmap.createGraphicBufferHandle());
        return ext;
    }
}
