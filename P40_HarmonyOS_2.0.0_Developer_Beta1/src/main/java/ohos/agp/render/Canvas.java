package ohos.agp.render;

import ohos.aafwk.utils.log.LogDomain;
import ohos.agp.components.element.Element;
import ohos.agp.utils.Matrix;
import ohos.agp.utils.MemoryCleanerRegistry;
import ohos.agp.utils.NativeMemoryCleanerHelper;
import ohos.agp.utils.Point;
import ohos.agp.utils.Rect;
import ohos.agp.utils.RectFloat;
import ohos.app.Context;
import ohos.global.configuration.DeviceCapability;
import ohos.global.resource.ResourceManager;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;

public class Canvas {
    private static final float EPSILON = 1.0E-6f;
    private static final int MAX_PIXELMAP_SIZE = 32766;
    private static final int POINTS_COORDINATE_MULTIPLE = 2;
    private static final int POINTS_MULTIPLE = 4;
    private static final HiLogLabel TAG = new HiLogLabel(3, (int) LogDomain.END, "AGP_RENDER_CANVAS");
    protected boolean mDrawIntoBitmap = false;
    protected long mNativeCanvasHandle = 0;
    private Paint mPaintForDrawText;
    private Paint mPaintForDrawTextOnPath;
    protected Path mPath;
    protected Path mPathForText;
    protected Texture mTexture = null;

    private boolean isArrayIllegal(int i, int i2, int i3) {
        return i3 < 0 || i2 < 0 || i2 + i3 > i;
    }

    private native void nativeClipPath(long j, long j2, int i);

    private native void nativeClipRect(long j, RectFloat rectFloat, int i);

    private native void nativeConcat(long j, long j2);

    private native void nativeDrawArc(long j, RectFloat rectFloat, Arc arc, long j2);

    private native void nativeDrawChangelessTexture(long j, float f, float f2, long j2);

    private native void nativeDrawCircle(long j, float f, float f2, float f3, long j2);

    private native void nativeDrawColor(long j, int i, int i2);

    private native void nativeDrawDeformedPixelMap(long j, PixelMapHolder pixelMapHolder, int i, int i2, float[] fArr, int i3, int[] iArr, int i4, long j2);

    private native void nativeDrawElement(long j, long j2);

    private native void nativeDrawLine(long j, Point point, Point point2, long j2);

    private native void nativeDrawLines(long j, float[] fArr, int i, int i2, int i3, long j2);

    private native void nativeDrawOval(long j, RectFloat rectFloat, long j2);

    private native void nativeDrawPaint(long j, long j2);

    private native void nativeDrawPath(long j, long j2, long j3);

    private native void nativeDrawPicture(long j, long j2);

    private native void nativeDrawPixelMapHolder(long j, PixelMapHolder pixelMapHolder, float f, float f2, long j2);

    private native void nativeDrawPixelMapHolderCircleShape(long j, PixelMapHolder pixelMapHolder, RectFloat rectFloat, float f, float f2, float f3);

    private native void nativeDrawPixelMapHolderRect(long j, PixelMapHolder pixelMapHolder, RectFloat rectFloat, long j2);

    private native void nativeDrawPixelMapHolderRectShape(long j, PixelMapHolder pixelMapHolder, RectFloat rectFloat, RectFloat rectFloat2, long j2);

    private native void nativeDrawPixelMapHolderRoundRectShape(long j, PixelMapHolder pixelMapHolder, RectFloat rectFloat, RectFloat rectFloat2, float f, float f2);

    private native void nativeDrawPoint(long j, float f, float f2, long j2);

    private native void nativeDrawPoints(long j, float[] fArr, int i, int i2, long j2);

    private native void nativeDrawPolylineShadow(long j, float[] fArr, Point point, Point point2, int i, long j2);

    private native void nativeDrawRect(long j, RectFloat rectFloat, long j2);

    private native void nativeDrawRoundRect(long j, RectFloat rectFloat, float f, float f2, long j2);

    private native void nativeDrawText(long j, long j2, String str, float f, float f2);

    private native void nativeDrawTextOnPath(long j, long j2, String str, long j3, float f, float f2);

    private native void nativeDrawTexture(long j, float f, float f2, long j2);

    private native void nativeDrawVertices(long j, int i, int i2, float[] fArr, int i3, float[] fArr2, int i4, int[] iArr, int i5, short[] sArr, int i6, int i7, long j2);

    private native long nativeGetCanvasHandle();

    private native Rect nativeGetLocalClipBounds(long j);

    private native void nativeGetMatrix(long j, long j2);

    private native int nativeGetSaveCount(long j);

    private native long nativeGetTextureCanvasHandle(long j);

    private native boolean nativeQuickReject(long j, float f, float f2, float f3, float f4);

    private native void nativeRestore(long j);

    private native void nativeRestoreToCount(long j, int i);

    private native void nativeRotate(long j, float f, float f2, float f3);

    private native int nativeSave(long j);

    private native int nativeSaveLayer(long j, float f, float f2, float f3, float f4, long j2);

    private native int nativeSaveLayerAlpha(long j, float f, float f2, float f3, float f4, int i);

    private native void nativeScale(long j, float f, float f2);

    private native void nativeSetMatrix(long j, long j2);

    private native void nativeSetTexture(long j, long j2);

    private native void nativeSkew(long j, float f, float f2);

    private native void nativeTranslate(long j, float f, float f2);

    public enum ClipOp {
        INTERSECT(0),
        DIFFERENCE(1);
        
        final int enumInt;

        private ClipOp(int i) {
            this.enumInt = i;
        }

        public int value() {
            return this.enumInt;
        }
    }

    public enum PorterDuffMode {
        CLEAR(0),
        SRC(1),
        DST(2),
        SRC_OVER(3),
        DST_OVER(4),
        SRC_IN(5),
        DST_IN(6),
        SRC_OUT(7),
        DST_OUT(8),
        SRC_ATOP(9),
        DST_ATOP(10),
        XOR(11),
        PLUS(12),
        MULTIPLY(13),
        SCREEN(14),
        OVERLAY(15),
        DARKEN(16),
        LIGHTEN(17);
        
        final int enumInt;

        private PorterDuffMode(int i) {
            this.enumInt = i;
        }

        public int value() {
            return this.enumInt;
        }
    }

    public enum VertexMode {
        TRIANGLES(0),
        TRIANGLE_STRIP(1),
        TRIANGLE_FAN(2);
        
        final int enumInt;

        private VertexMode(int i) {
            this.enumInt = i;
        }

        public int value() {
            return this.enumInt;
        }
    }

    private void init(long j) {
        this.mNativeCanvasHandle = j;
        MemoryCleanerRegistry.getInstance().registerWithNativeBind(this, new CanvasCleaner(this.mNativeCanvasHandle), this.mNativeCanvasHandle);
    }

    public Canvas() {
        init(nativeGetCanvasHandle());
    }

    public Canvas(long j) {
        init(j);
    }

    public Canvas(Texture texture) {
        this.mTexture = texture;
        Texture texture2 = this.mTexture;
        init(texture2 == null ? nativeGetCanvasHandle() : nativeGetTextureCanvasHandle(texture2.getNativeHandle()));
    }

    /* access modifiers changed from: protected */
    public static class CanvasCleaner extends NativeMemoryCleanerHelper {
        private native void nativeCanvasRelease(long j);

        public CanvasCleaner(long j) {
            super(j);
        }

        /* access modifiers changed from: protected */
        @Override // ohos.agp.utils.NativeMemoryCleanerHelper
        public void releaseNativeMemory(long j) {
            if (j != 0) {
                nativeCanvasRelease(j);
            }
        }
    }

    public long getNativePtr() {
        return this.mNativeCanvasHandle;
    }

    public void drawRect(RectFloat rectFloat, Paint paint) {
        nativeDrawRect(this.mNativeCanvasHandle, rectFloat, paint.getNativeHandle());
    }

    public void drawCircle(float f, float f2, float f3, Paint paint) {
        nativeDrawCircle(this.mNativeCanvasHandle, f, f2, f3, paint.getNativeHandle());
    }

    public void drawRoundRect(RectFloat rectFloat, float f, float f2, Paint paint) {
        nativeDrawRoundRect(this.mNativeCanvasHandle, rectFloat, f, f2, paint.getNativeHandle());
    }

    public void drawLine(Point point, Point point2, Paint paint) {
        nativeDrawLine(this.mNativeCanvasHandle, point, point2, paint.getNativeHandle());
    }

    public void drawOval(RectFloat rectFloat, Paint paint) {
        nativeDrawOval(this.mNativeCanvasHandle, rectFloat, paint.getNativeHandle());
    }

    public void drawArc(RectFloat rectFloat, Arc arc, Paint paint) {
        nativeDrawArc(this.mNativeCanvasHandle, rectFloat, arc, paint.getNativeHandle());
    }

    public void drawPolylineShadow(float[] fArr, Point point, Point point2, Paint paint) {
        nativeDrawPolylineShadow(this.mNativeCanvasHandle, fArr, point, point2, fArr.length, paint.getNativeHandle());
    }

    public void drawElement(Element element) {
        nativeDrawElement(this.mNativeCanvasHandle, element == null ? 0 : element.getNativeElementPtr());
    }

    public int save() {
        return nativeSave(this.mNativeCanvasHandle);
    }

    public void restore() {
        nativeRestore(this.mNativeCanvasHandle);
    }

    public void restoreToCount(int i) {
        long j = this.mNativeCanvasHandle;
        if (i <= 0) {
            i = 1;
        }
        nativeRestoreToCount(j, i);
    }

    public void setMatrix(Matrix matrix) {
        if (matrix != null) {
            nativeSetMatrix(this.mNativeCanvasHandle, matrix.getNativeHandle());
        }
    }

    public void clipRect(RectFloat rectFloat) {
        clipRect(rectFloat, ClipOp.INTERSECT);
    }

    public void clipRect(RectFloat rectFloat, ClipOp clipOp) {
        nativeClipRect(this.mNativeCanvasHandle, rectFloat, clipOp.value());
    }

    public void rotate(float f, float f2, float f3) {
        nativeRotate(this.mNativeCanvasHandle, f, f2, f3);
    }

    public void scale(float f, float f2, float f3, float f4) {
        if (Math.abs(f - 1.0f) > 1.0E-6f || Math.abs(f2 - 1.0f) > 1.0E-6f) {
            translate(f3, f4);
            scale(f, f2);
            translate(-f3, -f4);
        }
    }

    public void scale(float f, float f2) {
        if (Math.abs(f - 1.0f) > 1.0E-6f || Math.abs(f2 - 1.0f) > 1.0E-6f) {
            nativeScale(this.mNativeCanvasHandle, f, f2);
        }
    }

    public void translate(float f, float f2) {
        nativeTranslate(this.mNativeCanvasHandle, f, f2);
    }

    public void drawText(Paint paint, String str, float f, float f2) {
        if (paint != null && str != null) {
            this.mPaintForDrawText = paint;
            nativeDrawText(this.mNativeCanvasHandle, paint.getNativeHandle(), str, f, f2);
        }
    }

    public void drawCharSequence(Paint paint, CharSequence charSequence, float f, float f2) {
        drawText(paint, charSequence.toString(), f, f2);
    }

    public void drawChars(Paint paint, char[] cArr, float f, float f2) {
        if (cArr != null && cArr.length != 0) {
            drawText(paint, String.valueOf(cArr), f, f2);
        }
    }

    public void drawTextOnPath(Paint paint, String str, Path path, float f, float f2) {
        if (paint != null && str != null && path != null) {
            this.mPathForText = path;
            this.mPaintForDrawTextOnPath = paint;
            nativeDrawTextOnPath(this.mNativeCanvasHandle, paint.getNativeHandle(), str, path.getNativeHandle(), f, f2);
        }
    }

    public void setTexture(Texture texture) {
        if (texture != null) {
            nativeSetTexture(this.mNativeCanvasHandle, texture.getNativeHandle());
        }
    }

    public void drawLines(float[] fArr, Paint paint) {
        drawLines(fArr, 0, fArr.length, paint);
    }

    public void drawLines(float[] fArr, int i, int i2, Paint paint) {
        if (i >= 0 && i2 >= 4 && i2 % 4 == 0 && i + i2 <= fArr.length) {
            nativeDrawLines(this.mNativeCanvasHandle, fArr, i, i2, fArr.length, paint.getNativeHandle());
        }
    }

    public void drawDeformedPixelMap(PixelMapHolder pixelMapHolder, PixelMapDrawInfo pixelMapDrawInfo, Paint paint) {
        if (pixelMapDrawInfo != null && pixelMapHolder != null) {
            long j = 0;
            if (paint != null) {
                j = paint.getNativeHandle();
            }
            nativeDrawDeformedPixelMap(this.mNativeCanvasHandle, pixelMapHolder, pixelMapDrawInfo.getWidth(), pixelMapDrawInfo.getHeight(), pixelMapDrawInfo.getVertices(), pixelMapDrawInfo.getVertOffset(), pixelMapDrawInfo.getColors(), pixelMapDrawInfo.getColorOffset(), j);
        }
    }

    public void drawPixelMapHolder(PixelMapHolder pixelMapHolder, float f, float f2, Paint paint) {
        nativeDrawPixelMapHolder(this.mNativeCanvasHandle, pixelMapHolder, f, f2, paint.getNativeHandle());
    }

    public void drawPixelMapHolderRect(PixelMapHolder pixelMapHolder, RectFloat rectFloat, Paint paint) {
        nativeDrawPixelMapHolderRect(this.mNativeCanvasHandle, pixelMapHolder, rectFloat, paint.getNativeHandle());
    }

    public void drawPixelMapHolderRect(PixelMapHolder pixelMapHolder, RectFloat rectFloat, RectFloat rectFloat2, Paint paint) {
        if (rectFloat != null && rectFloat2 != null) {
            nativeDrawPixelMapHolderRectShape(this.mNativeCanvasHandle, pixelMapHolder, rectFloat, rectFloat2, paint.getNativeHandle());
        }
    }

    public void drawPixelMapHolderCircleShape(PixelMapHolder pixelMapHolder, RectFloat rectFloat, float f, float f2, float f3) {
        nativeDrawPixelMapHolderCircleShape(this.mNativeCanvasHandle, pixelMapHolder, rectFloat, f, f2, f3);
    }

    public void drawPixelMapHolderRoundRectShape(PixelMapHolder pixelMapHolder, RectFloat rectFloat, RectFloat rectFloat2, float f, float f2) {
        nativeDrawPixelMapHolderRoundRectShape(this.mNativeCanvasHandle, pixelMapHolder, rectFloat, rectFloat2, f, f2);
    }

    public void drawPath(Path path, Paint paint) {
        long nativeHandle = path == null ? 0 : path.getNativeHandle();
        if (this.mPath != path) {
            this.mPath = path;
        }
        nativeDrawPath(this.mNativeCanvasHandle, nativeHandle, paint.getNativeHandle());
    }

    public void drawTexture(float f, float f2, Texture texture) {
        if (this.mTexture != texture) {
            this.mTexture = texture;
        }
        Texture texture2 = this.mTexture;
        if (texture2 != null) {
            nativeDrawTexture(this.mNativeCanvasHandle, f, f2, texture2.getNativeHandle());
        }
    }

    public void drawChangelessTexture(float f, float f2, Texture texture) {
        if (this.mTexture != texture) {
            this.mTexture = texture;
        }
        Texture texture2 = this.mTexture;
        if (texture2 != null) {
            nativeDrawChangelessTexture(this.mNativeCanvasHandle, f, f2, texture2.getNativeHandle());
        }
    }

    public void clipPath(Path path, ClipOp clipOp) {
        nativeClipPath(this.mNativeCanvasHandle, path == null ? 0 : path.getNativeHandle(), clipOp.value());
    }

    public void drawPoint(float f, float f2, Paint paint) {
        nativeDrawPoint(this.mNativeCanvasHandle, f, f2, paint.getNativeHandle());
    }

    public void drawPaint(Paint paint) {
        nativeDrawPaint(this.mNativeCanvasHandle, paint.getNativeHandle());
    }

    public void drawColor(int i, PorterDuffMode porterDuffMode) {
        if (porterDuffMode != null) {
            nativeDrawColor(this.mNativeCanvasHandle, i, porterDuffMode.value());
        }
    }

    public void concat(Matrix matrix) {
        if (matrix != null) {
            nativeConcat(this.mNativeCanvasHandle, matrix.getNativeHandle());
        }
    }

    public Rect getLocalClipBounds() {
        return nativeGetLocalClipBounds(this.mNativeCanvasHandle);
    }

    public void getMatrix(Matrix matrix) {
        if (matrix != null) {
            nativeGetMatrix(this.mNativeCanvasHandle, matrix.getNativeHandle());
        }
    }

    public int getSaveCount() {
        return nativeGetSaveCount(this.mNativeCanvasHandle);
    }

    public int saveLayer(RectFloat rectFloat, Paint paint) {
        if (rectFloat == null) {
            rectFloat = new RectFloat(getLocalClipBounds());
        }
        return nativeSaveLayer(this.mNativeCanvasHandle, rectFloat.left, rectFloat.top, rectFloat.right, rectFloat.bottom, paint.getNativeHandle());
    }

    public int saveLayerAlpha(RectFloat rectFloat, int i) {
        if (rectFloat == null) {
            rectFloat = new RectFloat(getLocalClipBounds());
        }
        return nativeSaveLayerAlpha(this.mNativeCanvasHandle, rectFloat.left, rectFloat.top, rectFloat.right, rectFloat.bottom, getValidColorInt(i));
    }

    public int getDeviceDensity(Context context) {
        ResourceManager resourceManager;
        DeviceCapability deviceCapability;
        if (context == null || (resourceManager = context.getResourceManager()) == null || (deviceCapability = resourceManager.getDeviceCapability()) == null) {
            return 0;
        }
        return deviceCapability.screenDensity;
    }

    public void skew(float f, float f2) {
        if (f != 0.0f || f2 != 0.0f) {
            nativeSkew(this.mNativeCanvasHandle, f, f2);
        }
    }

    public void drawVertices(VertexMode vertexMode, Vertices vertices, Paint paint) {
        if (vertexMode == null || vertices == null) {
            HiLog.error(TAG, "vertices or vertexMode is illegal", new Object[0]);
            return;
        }
        float[] verts = vertices.getVerts();
        int vertOffset = vertices.getVertOffset();
        int vertexCount = vertices.getVertexCount();
        if (isArrayIllegal(verts.length, vertOffset, vertexCount)) {
            HiLog.error(TAG, "vertices vert array is illegal", new Object[0]);
            return;
        }
        float[] texs = vertices.getTexs();
        int texOffset = vertices.getTexOffset();
        if (texs.length <= 0 || !isArrayIllegal(texs.length, texOffset, vertexCount)) {
            int[] colors = vertices.getColors();
            int colorOffset = vertices.getColorOffset();
            if (colors.length <= 0 || !isArrayIllegal(colors.length, colorOffset, vertexCount / 2)) {
                short[] indices = vertices.getIndices();
                int indexOffset = vertices.getIndexOffset();
                int indexCount = vertices.getIndexCount();
                if (indices.length <= 0 || !isArrayIllegal(indices.length, indexOffset, indexCount)) {
                    nativeDrawVertices(this.mNativeCanvasHandle, vertexMode.value(), vertexCount, verts, vertOffset, texs, texOffset, colors, colorOffset, indices, indexOffset, indexCount, paint.getNativeHandle());
                } else {
                    HiLog.error(TAG, "vertices indices array is illegal", new Object[0]);
                }
            } else {
                HiLog.error(TAG, "vertices color array is illegal", new Object[0]);
            }
        } else {
            HiLog.error(TAG, "vertices tex array is illegal", new Object[0]);
        }
    }

    public void drawPicture(Picture picture) {
        picture.endRecording();
        int save = save();
        nativeDrawPicture(this.mNativeCanvasHandle, picture.getNativeHandle());
        restoreToCount(save);
    }

    public void drawPicture(Picture picture, RectFloat rectFloat) {
        save();
        translate(rectFloat.left, rectFloat.top);
        if (picture.getWidth() > 0 && picture.getHeight() > 0) {
            scale(rectFloat.getWidth() / ((float) picture.getWidth()), rectFloat.getHeight() / ((float) picture.getHeight()));
        }
        drawPicture(picture);
        restore();
    }

    public boolean quickReject(float f, float f2, float f3, float f4) {
        return nativeQuickReject(this.mNativeCanvasHandle, f, f2, f3, f4);
    }

    public void drawPoints(float[] fArr, int i, int i2, Paint paint) {
        if (i < 0 || i2 < 0 || fArr == null) {
            throw new IllegalArgumentException("Points param is illegal");
        } else if (fArr.length % 2 == 0 && i2 % 2 == 0) {
            nativeDrawPoints(this.mNativeCanvasHandle, fArr, i, i2, paint.getNativeHandle());
        } else {
            throw new IllegalArgumentException("Points count or array is illegal");
        }
    }

    private int getValidColorInt(int i) {
        return Math.min(255, Math.max(0, i));
    }
}
