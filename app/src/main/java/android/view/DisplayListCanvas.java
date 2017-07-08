package android.view;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.CanvasProperty;
import android.graphics.Paint;
import android.util.Pools.SynchronizedPool;

public class DisplayListCanvas extends Canvas {
    private static final int MAX_BITMAP_SIZE = 104857600;
    private static final int POOL_LIMIT = 25;
    private static boolean sIsAvailable;
    private static final SynchronizedPool<DisplayListCanvas> sPool = null;
    private int mHeight;
    RenderNode mNode;
    private int mWidth;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.view.DisplayListCanvas.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.view.DisplayListCanvas.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.view.DisplayListCanvas.<clinit>():void");
    }

    private static native void nCallDrawGLFunction(long j, long j2, Runnable runnable);

    private static native long nCreateDisplayListCanvas(int i, int i2);

    private static native void nDrawCircle(long j, long j2, long j3, long j4, long j5);

    private static native void nDrawLayer(long j, long j2);

    private static native void nDrawRenderNode(long j, long j2);

    private static native void nDrawRoundRect(long j, long j2, long j3, long j4, long j5, long j6, long j7, long j8);

    protected static native long nFinishRecording(long j);

    private static native int nGetMaximumTextureHeight();

    private static native int nGetMaximumTextureWidth();

    private static native void nInsertReorderBarrier(long j, boolean z);

    private static native boolean nIsAvailable();

    private static native void nResetDisplayListCanvas(long j, long j2, int i, int i2);

    static DisplayListCanvas obtain(RenderNode node, int width, int height) {
        if (node == null) {
            throw new IllegalArgumentException("node cannot be null");
        }
        DisplayListCanvas canvas = (DisplayListCanvas) sPool.acquire();
        if (canvas == null) {
            canvas = new DisplayListCanvas(width, height);
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

    static boolean isAvailable() {
        return sIsAvailable;
    }

    private DisplayListCanvas(int width, int height) {
        super(nCreateDisplayListCanvas(width, height));
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
