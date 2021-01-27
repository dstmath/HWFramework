package com.huawei.android.view;

import android.graphics.Bitmap;
import android.graphics.GraphicBuffer;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.view.Surface;
import android.view.SurfaceControl;
import com.huawei.android.graphics.GraphicBufferEx;

public class SurfaceEx {
    public static void copyFrom(Surface surface, SurfaceControl sc) {
        surface.copyFrom(sc);
    }

    public static void attachAndQueueBuffer(Surface surface, GraphicBufferEx bufferEx) {
        surface.attachAndQueueBuffer(bufferEx.getGraphicBuffer());
    }

    public static void setPosition(SurfaceControl.Transaction transaction, SurfaceControl sc, float x, float y) {
        transaction.setPosition(sc, x, y);
    }

    public static void show(SurfaceControl.Transaction transaction, SurfaceControl sc) {
        transaction.show(sc);
    }

    public static void hide(SurfaceControl.Transaction transaction, SurfaceControl sc) {
        transaction.hide(sc);
    }

    public static void remove(SurfaceControl.Transaction transaction, SurfaceControl sc) {
        transaction.remove(sc);
    }

    public static GraphicBufferEx captureLayers(SurfaceControl surfaceControl, Rect sourceCrop, float frameScale) {
        if (surfaceControl == null) {
            return null;
        }
        GraphicBuffer buffer = SurfaceControl.captureLayers(surfaceControl.getHandle(), sourceCrop, frameScale).getGraphicBuffer();
        GraphicBufferEx ex = new GraphicBufferEx();
        ex.setGraphicBuffer(buffer);
        return ex;
    }

    public static Bitmap screenshot(Rect sourceCrop, int width, int height, int rotation) {
        return SurfaceControl.screenshot(sourceCrop, width, height, rotation);
    }

    public static void setMatrix(SurfaceControl.Transaction transaction, SurfaceControl sc, Matrix matrix, float[] float9) {
        transaction.setMatrix(sc, matrix, float9);
    }

    public static void setWindowCrop(SurfaceControl.Transaction transaction, SurfaceControl sc, Rect crop) {
        transaction.setWindowCrop(sc, crop);
    }
}
