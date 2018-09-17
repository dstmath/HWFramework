package android.view;

import android.graphics.Bitmap;
import android.graphics.CanvasProperty;
import android.graphics.Paint;
import android.util.Pools.SynchronizedPool;

public final class DisplayListCanvas extends RecordingCanvas {
    private static final int MAX_BITMAP_SIZE = 104857600;
    private static final int POOL_LIMIT = 25;
    private static final SynchronizedPool<DisplayListCanvas> sPool = new SynchronizedPool(25);
    private int mHeight;
    RenderNode mNode;
    private int mWidth;

    private static native void nCallDrawGLFunction(long j, long j2, Runnable runnable);

    private static native long nCreateDisplayListCanvas(long j, int i, int i2);

    private static native void nDrawCircle(long j, long j2, long j3, long j4, long j5);

    private static native void nDrawLayer(long j, long j2);

    private static native void nDrawRenderNode(long j, long j2);

    private static native void nDrawRoundRect(long j, long j2, long j3, long j4, long j5, long j6, long j7, long j8);

    private static native long nFinishRecording(long j);

    private static native int nGetMaximumTextureHeight();

    private static native int nGetMaximumTextureWidth();

    private static native void nInsertReorderBarrier(long j, boolean z);

    private static native void nResetDisplayListCanvas(long j, long j2, int i, int i2);

    static DisplayListCanvas obtain(RenderNode node, int width, int height) {
        if (node == null) {
            throw new IllegalArgumentException("node cannot be null");
        }
        DisplayListCanvas canvas = (DisplayListCanvas) sPool.acquire();
        if (canvas == null) {
            canvas = new DisplayListCanvas(node, width, height);
        } else {
            nResetDisplayListCanvas(canvas.mNativeCanvasWrapper, node.mNativeRenderNode, width, height);
        }
        canvas.mNode = node;
        canvas.mWidth = width;
        canvas.mHeight = height;
        return canvas;
    }

    void recycle() {
        this.mNode = null;
        sPool.release(this);
    }

    long finishRecording() {
        return nFinishRecording(this.mNativeCanvasWrapper);
    }

    public boolean isRecordingFor(Object o) {
        return o == this.mNode;
    }

    private DisplayListCanvas(RenderNode node, int width, int height) {
        super(nCreateDisplayListCanvas(node.mNativeRenderNode, width, height));
        this.mDensity = 0;
    }

    public void setDensity(int density) {
    }

    public boolean isHardwareAccelerated() {
        return true;
    }

    public void setBitmap(Bitmap bitmap) {
        throw new UnsupportedOperationException();
    }

    public boolean isOpaque() {
        return false;
    }

    public int getWidth() {
        return this.mWidth;
    }

    public int getHeight() {
        return this.mHeight;
    }

    public int getMaximumBitmapWidth() {
        return nGetMaximumTextureWidth();
    }

    public int getMaximumBitmapHeight() {
        return nGetMaximumTextureHeight();
    }

    public void insertReorderBarrier() {
        nInsertReorderBarrier(this.mNativeCanvasWrapper, true);
    }

    public void insertInorderBarrier() {
        nInsertReorderBarrier(this.mNativeCanvasWrapper, false);
    }

    public void callDrawGLFunction2(long drawGLFunction) {
        nCallDrawGLFunction(this.mNativeCanvasWrapper, drawGLFunction, null);
    }

    public void drawGLFunctor2(long drawGLFunctor, Runnable releasedCallback) {
        nCallDrawGLFunction(this.mNativeCanvasWrapper, drawGLFunctor, releasedCallback);
    }

    public void drawRenderNode(RenderNode renderNode) {
        nDrawRenderNode(this.mNativeCanvasWrapper, renderNode.getNativeDisplayList());
    }

    void drawHardwareLayer(HardwareLayer layer) {
        nDrawLayer(this.mNativeCanvasWrapper, layer.getLayerHandle());
    }

    public void drawCircle(CanvasProperty<Float> cx, CanvasProperty<Float> cy, CanvasProperty<Float> radius, CanvasProperty<Paint> paint) {
        nDrawCircle(this.mNativeCanvasWrapper, cx.getNativeContainer(), cy.getNativeContainer(), radius.getNativeContainer(), paint.getNativeContainer());
    }

    public void drawRoundRect(CanvasProperty<Float> left, CanvasProperty<Float> top, CanvasProperty<Float> right, CanvasProperty<Float> bottom, CanvasProperty<Float> rx, CanvasProperty<Float> ry, CanvasProperty<Paint> paint) {
        nDrawRoundRect(this.mNativeCanvasWrapper, left.getNativeContainer(), top.getNativeContainer(), right.getNativeContainer(), bottom.getNativeContainer(), rx.getNativeContainer(), ry.getNativeContainer(), paint.getNativeContainer());
    }

    protected void throwIfCannotDraw(Bitmap bitmap) {
        super.throwIfCannotDraw(bitmap);
        int bitmapSize = bitmap.getByteCount();
        if (bitmapSize > MAX_BITMAP_SIZE) {
            throw new RuntimeException("Canvas: trying to draw too large(" + bitmapSize + "bytes) bitmap.");
        }
    }
}
