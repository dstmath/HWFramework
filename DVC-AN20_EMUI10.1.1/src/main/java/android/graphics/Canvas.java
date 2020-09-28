package android.graphics;

import android.annotation.UnsupportedAppUsage;
import android.graphics.PorterDuff;
import android.graphics.Region;
import android.graphics.text.MeasuredText;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import javax.microedition.khronos.opengles.GL;
import libcore.util.NativeAllocationRegistry;

public class Canvas extends BaseCanvas {
    public static final int ALL_SAVE_FLAG = 31;
    public static final int CLIP_SAVE_FLAG = 2;
    public static final int CLIP_TO_LAYER_SAVE_FLAG = 16;
    public static final int FULL_COLOR_LAYER_SAVE_FLAG = 8;
    public static final int HAS_ALPHA_LAYER_SAVE_FLAG = 4;
    public static final int MATRIX_SAVE_FLAG = 1;
    private static final int MAXMIMUM_BITMAP_SIZE = 32766;
    public static boolean sCompatibilityRestore = false;
    public static boolean sCompatibilitySetBitmap = false;
    private static int sCompatiblityVersion = 0;
    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 117521088)
    private Bitmap mBitmap;
    private DrawFilter mDrawFilter;
    private Runnable mFinalizer;
    private boolean mLastHwForceDark = false;

    @Retention(RetentionPolicy.SOURCE)
    public @interface Saveflags {
    }

    private static native boolean nClipPath(long j, long j2, int i);

    private static native boolean nClipRect(long j, float f, float f2, float f3, float f4, int i);

    private static native void nConcat(long j, long j2);

    private static native void nFreeCaches();

    private static native void nFreeTextLayoutCaches();

    private static native boolean nGetClipBounds(long j, Rect rect);

    private static native int nGetHeight(long j);

    private static native void nGetMatrix(long j, long j2);

    /* access modifiers changed from: private */
    public static native long nGetNativeFinalizer();

    private static native int nGetSaveCount(long j);

    private static native int nGetWidth(long j);

    private static native long nInitRaster(long j);

    private static native boolean nIsOpaque(long j);

    private static native boolean nQuickReject(long j, float f, float f2, float f3, float f4);

    private static native boolean nQuickReject(long j, long j2);

    private static native boolean nRestore(long j);

    private static native void nRestoreToCount(long j, int i);

    private static native void nRestoreUnclippedLayer(long j, int i, long j2);

    private static native void nRotate(long j, float f);

    private static native int nSave(long j, int i);

    private static native int nSaveLayer(long j, float f, float f2, float f3, float f4, long j2, int i);

    private static native int nSaveLayerAlpha(long j, float f, float f2, float f3, float f4, int i, int i2);

    private static native int nSaveUnclippedLayer(long j, int i, int i2, int i3, int i4);

    private static native void nScale(long j, float f, float f2);

    private static native void nSetAllowedHwForceDark(long j, boolean z, int i);

    private static native void nSetBitmap(long j, long j2);

    private static native void nSetCompatibilityVersion(int i);

    private static native void nSetDrawFilter(long j, long j2);

    private static native void nSetMatrix(long j, long j2);

    private static native void nSkew(long j, float f, float f2);

    private static native void nTranslate(long j, float f, float f2);

    @UnsupportedAppUsage
    public long getNativeCanvasWrapper() {
        return this.mNativeCanvasWrapper;
    }

    public boolean isRecordingFor(Object o) {
        return false;
    }

    private static class NoImagePreloadHolder {
        public static final NativeAllocationRegistry sRegistry = NativeAllocationRegistry.createMalloced(Canvas.class.getClassLoader(), Canvas.nGetNativeFinalizer());

        private NoImagePreloadHolder() {
        }
    }

    public Canvas() {
        if (!isHardwareAccelerated()) {
            this.mNativeCanvasWrapper = nInitRaster(0);
            this.mFinalizer = NoImagePreloadHolder.sRegistry.registerNativeAllocation(this, this.mNativeCanvasWrapper);
            return;
        }
        this.mFinalizer = null;
    }

    public Canvas(Bitmap bitmap) {
        if (bitmap.isMutable()) {
            throwIfCannotDraw(bitmap);
            this.mNativeCanvasWrapper = nInitRaster(bitmap.getNativeInstance());
            this.mFinalizer = NoImagePreloadHolder.sRegistry.registerNativeAllocation(this, this.mNativeCanvasWrapper);
            this.mBitmap = bitmap;
            this.mDensity = bitmap.mDensity;
            return;
        }
        throw new IllegalStateException("Immutable bitmap passed to Canvas constructor");
    }

    @UnsupportedAppUsage(maxTargetSdk = 28, trackingBug = 115609023)
    public Canvas(long nativeCanvas) {
        if (nativeCanvas != 0) {
            this.mNativeCanvasWrapper = nativeCanvas;
            this.mFinalizer = NoImagePreloadHolder.sRegistry.registerNativeAllocation(this, this.mNativeCanvasWrapper);
            this.mDensity = Bitmap.getDefaultDensity();
            return;
        }
        throw new IllegalStateException();
    }

    /* access modifiers changed from: protected */
    @UnsupportedAppUsage
    @Deprecated
    public GL getGL() {
        return null;
    }

    @Override // android.graphics.BaseCanvas
    public boolean isHardwareAccelerated() {
        return false;
    }

    public void setBitmap(Bitmap bitmap) {
        if (!isHardwareAccelerated()) {
            Matrix preservedMatrix = null;
            if (bitmap != null && sCompatibilitySetBitmap) {
                preservedMatrix = getMatrix();
            }
            if (bitmap == null) {
                nSetBitmap(this.mNativeCanvasWrapper, 0);
                this.mDensity = 0;
            } else if (bitmap.isMutable()) {
                throwIfCannotDraw(bitmap);
                nSetBitmap(this.mNativeCanvasWrapper, bitmap.getNativeInstance());
                this.mDensity = bitmap.mDensity;
            } else {
                throw new IllegalStateException();
            }
            if (preservedMatrix != null) {
                setMatrix(preservedMatrix);
            }
            this.mBitmap = bitmap;
            return;
        }
        throw new RuntimeException("Can't set a bitmap device on a HW accelerated canvas");
    }

    @Deprecated
    public void insertReorderBarrier() {
        enableZ();
    }

    @Deprecated
    public void insertInorderBarrier() {
        disableZ();
    }

    public void enableZ() {
    }

    public void disableZ() {
    }

    public boolean isOpaque() {
        return nIsOpaque(this.mNativeCanvasWrapper);
    }

    public int getWidth() {
        return nGetWidth(this.mNativeCanvasWrapper);
    }

    public int getHeight() {
        return nGetHeight(this.mNativeCanvasWrapper);
    }

    public int getDensity() {
        return this.mDensity;
    }

    public void setDensity(int density) {
        Bitmap bitmap = this.mBitmap;
        if (bitmap != null) {
            bitmap.setDensity(density);
        }
        this.mDensity = density;
    }

    @UnsupportedAppUsage
    public void setScreenDensity(int density) {
        this.mScreenDensity = density;
    }

    public int getMaximumBitmapWidth() {
        return MAXMIMUM_BITMAP_SIZE;
    }

    public int getMaximumBitmapHeight() {
        return MAXMIMUM_BITMAP_SIZE;
    }

    private static void checkValidSaveFlags(int saveFlags) {
        if (sCompatiblityVersion >= 28 && saveFlags != 31) {
            throw new IllegalArgumentException("Invalid Layer Save Flag - only ALL_SAVE_FLAGS is allowed");
        }
    }

    public int save() {
        return nSave(this.mNativeCanvasWrapper, 3);
    }

    public int save(int saveFlags) {
        return nSave(this.mNativeCanvasWrapper, saveFlags);
    }

    public int saveLayer(RectF bounds, Paint paint, int saveFlags) {
        if (bounds == null) {
            bounds = new RectF(getClipBounds());
        }
        checkValidSaveFlags(saveFlags);
        return saveLayer(bounds.left, bounds.top, bounds.right, bounds.bottom, paint, 31);
    }

    public int saveLayer(RectF bounds, Paint paint) {
        return saveLayer(bounds, paint, 31);
    }

    public int saveUnclippedLayer(int left, int top, int right, int bottom) {
        return nSaveUnclippedLayer(this.mNativeCanvasWrapper, left, top, right, bottom);
    }

    public void restoreUnclippedLayer(int saveCount, Paint paint) {
        nRestoreUnclippedLayer(this.mNativeCanvasWrapper, saveCount, paint.getNativeInstance());
    }

    public int saveLayer(float left, float top, float right, float bottom, Paint paint, int saveFlags) {
        checkValidSaveFlags(saveFlags);
        return nSaveLayer(this.mNativeCanvasWrapper, left, top, right, bottom, paint != null ? paint.getNativeInstance() : 0, 31);
    }

    public int saveLayer(float left, float top, float right, float bottom, Paint paint) {
        return saveLayer(left, top, right, bottom, paint, 31);
    }

    public int saveLayerAlpha(RectF bounds, int alpha, int saveFlags) {
        if (bounds == null) {
            bounds = new RectF(getClipBounds());
        }
        checkValidSaveFlags(saveFlags);
        return saveLayerAlpha(bounds.left, bounds.top, bounds.right, bounds.bottom, alpha, 31);
    }

    public int saveLayerAlpha(RectF bounds, int alpha) {
        return saveLayerAlpha(bounds, alpha, 31);
    }

    public int saveLayerAlpha(float left, float top, float right, float bottom, int alpha, int saveFlags) {
        checkValidSaveFlags(saveFlags);
        return nSaveLayerAlpha(this.mNativeCanvasWrapper, left, top, right, bottom, Math.min(255, Math.max(0, alpha)), 31);
    }

    public int saveLayerAlpha(float left, float top, float right, float bottom, int alpha) {
        return saveLayerAlpha(left, top, right, bottom, alpha, 31);
    }

    public void restore() {
        if (nRestore(this.mNativeCanvasWrapper)) {
            return;
        }
        if (!sCompatibilityRestore || !isHardwareAccelerated()) {
            throw new IllegalStateException("Underflow in restore - more restores than saves");
        }
    }

    public int getSaveCount() {
        return nGetSaveCount(this.mNativeCanvasWrapper);
    }

    public void restoreToCount(int saveCount) {
        if (saveCount < 1) {
            if (!sCompatibilityRestore || !isHardwareAccelerated()) {
                throw new IllegalArgumentException("Underflow in restoreToCount - more restores than saves");
            }
            saveCount = 1;
        }
        nRestoreToCount(this.mNativeCanvasWrapper, saveCount);
    }

    public void translate(float dx, float dy) {
        if (dx != 0.0f || dy != 0.0f) {
            nTranslate(this.mNativeCanvasWrapper, dx, dy);
        }
    }

    public void scale(float sx, float sy) {
        if (sx != 1.0f || sy != 1.0f) {
            nScale(this.mNativeCanvasWrapper, sx, sy);
        }
    }

    public final void scale(float sx, float sy, float px, float py) {
        if (sx != 1.0f || sy != 1.0f) {
            translate(px, py);
            scale(sx, sy);
            translate(-px, -py);
        }
    }

    public void rotate(float degrees) {
        if (degrees != 0.0f) {
            nRotate(this.mNativeCanvasWrapper, degrees);
        }
    }

    public final void rotate(float degrees, float px, float py) {
        if (degrees != 0.0f) {
            translate(px, py);
            rotate(degrees);
            translate(-px, -py);
        }
    }

    public void skew(float sx, float sy) {
        if (sx != 0.0f || sy != 0.0f) {
            nSkew(this.mNativeCanvasWrapper, sx, sy);
        }
    }

    public void concat(Matrix matrix) {
        if (matrix != null) {
            nConcat(this.mNativeCanvasWrapper, matrix.native_instance);
        }
    }

    public void setMatrix(Matrix matrix) {
        nSetMatrix(this.mNativeCanvasWrapper, matrix == null ? 0 : matrix.native_instance);
    }

    @Deprecated
    public void getMatrix(Matrix ctm) {
        nGetMatrix(this.mNativeCanvasWrapper, ctm.native_instance);
    }

    @Deprecated
    public final Matrix getMatrix() {
        Matrix m = new Matrix();
        getMatrix(m);
        return m;
    }

    private static void checkValidClipOp(Region.Op op) {
        if (sCompatiblityVersion >= 28 && op != Region.Op.INTERSECT && op != Region.Op.DIFFERENCE) {
            throw new IllegalArgumentException("Invalid Region.Op - only INTERSECT and DIFFERENCE are allowed");
        }
    }

    @Deprecated
    public boolean clipRect(RectF rect, Region.Op op) {
        checkValidClipOp(op);
        return nClipRect(this.mNativeCanvasWrapper, rect.left, rect.top, rect.right, rect.bottom, op.nativeInt);
    }

    @Deprecated
    public boolean clipRect(Rect rect, Region.Op op) {
        checkValidClipOp(op);
        return nClipRect(this.mNativeCanvasWrapper, (float) rect.left, (float) rect.top, (float) rect.right, (float) rect.bottom, op.nativeInt);
    }

    public boolean clipRectUnion(Rect rect) {
        return nClipRect(this.mNativeCanvasWrapper, (float) rect.left, (float) rect.top, (float) rect.right, (float) rect.bottom, Region.Op.UNION.nativeInt);
    }

    public boolean clipRect(RectF rect) {
        return nClipRect(this.mNativeCanvasWrapper, rect.left, rect.top, rect.right, rect.bottom, Region.Op.INTERSECT.nativeInt);
    }

    public boolean clipOutRect(RectF rect) {
        return nClipRect(this.mNativeCanvasWrapper, rect.left, rect.top, rect.right, rect.bottom, Region.Op.DIFFERENCE.nativeInt);
    }

    public boolean clipRect(Rect rect) {
        return nClipRect(this.mNativeCanvasWrapper, (float) rect.left, (float) rect.top, (float) rect.right, (float) rect.bottom, Region.Op.INTERSECT.nativeInt);
    }

    public boolean clipOutRect(Rect rect) {
        return nClipRect(this.mNativeCanvasWrapper, (float) rect.left, (float) rect.top, (float) rect.right, (float) rect.bottom, Region.Op.DIFFERENCE.nativeInt);
    }

    @Deprecated
    public boolean clipRect(float left, float top, float right, float bottom, Region.Op op) {
        checkValidClipOp(op);
        return nClipRect(this.mNativeCanvasWrapper, left, top, right, bottom, op.nativeInt);
    }

    public boolean clipRect(float left, float top, float right, float bottom) {
        return nClipRect(this.mNativeCanvasWrapper, left, top, right, bottom, Region.Op.INTERSECT.nativeInt);
    }

    public boolean clipOutRect(float left, float top, float right, float bottom) {
        return nClipRect(this.mNativeCanvasWrapper, left, top, right, bottom, Region.Op.DIFFERENCE.nativeInt);
    }

    public boolean clipRect(int left, int top, int right, int bottom) {
        return nClipRect(this.mNativeCanvasWrapper, (float) left, (float) top, (float) right, (float) bottom, Region.Op.INTERSECT.nativeInt);
    }

    public boolean clipOutRect(int left, int top, int right, int bottom) {
        return nClipRect(this.mNativeCanvasWrapper, (float) left, (float) top, (float) right, (float) bottom, Region.Op.DIFFERENCE.nativeInt);
    }

    @Deprecated
    public boolean clipPath(Path path, Region.Op op) {
        checkValidClipOp(op);
        return nClipPath(this.mNativeCanvasWrapper, path.readOnlyNI(), op.nativeInt);
    }

    public boolean clipPath(Path path) {
        return clipPath(path, Region.Op.INTERSECT);
    }

    public boolean clipOutPath(Path path) {
        return clipPath(path, Region.Op.DIFFERENCE);
    }

    @Deprecated
    public boolean clipRegion(Region region, Region.Op op) {
        return false;
    }

    @Deprecated
    public boolean clipRegion(Region region) {
        return false;
    }

    public DrawFilter getDrawFilter() {
        return this.mDrawFilter;
    }

    public void setDrawFilter(DrawFilter filter) {
        long nativeFilter = 0;
        if (filter != null) {
            nativeFilter = filter.mNativeInt;
        }
        this.mDrawFilter = filter;
        nSetDrawFilter(this.mNativeCanvasWrapper, nativeFilter);
    }

    public enum EdgeType {
        BW(0),
        AA(1);
        
        public final int nativeInt;

        private EdgeType(int nativeInt2) {
            this.nativeInt = nativeInt2;
        }
    }

    public boolean quickReject(RectF rect, EdgeType type) {
        return nQuickReject(this.mNativeCanvasWrapper, rect.left, rect.top, rect.right, rect.bottom);
    }

    public boolean quickReject(Path path, EdgeType type) {
        return nQuickReject(this.mNativeCanvasWrapper, path.readOnlyNI());
    }

    public boolean quickReject(float left, float top, float right, float bottom, EdgeType type) {
        return nQuickReject(this.mNativeCanvasWrapper, left, top, right, bottom);
    }

    public boolean getClipBounds(Rect bounds) {
        return nGetClipBounds(this.mNativeCanvasWrapper, bounds);
    }

    public boolean getAllowedHwForceDark() {
        return this.mLastHwForceDark;
    }

    public void setAllowedHwForceDark(boolean canUseHwForceDark, int appType, int windowType) {
        this.mLastHwForceDark = canUseHwForceDark;
        nSetAllowedHwForceDark(this.mNativeCanvasWrapper, canUseHwForceDark, (appType << 16) | windowType);
    }

    public final Rect getClipBounds() {
        Rect r = new Rect();
        getClipBounds(r);
        return r;
    }

    public void drawPicture(Picture picture) {
        picture.endRecording();
        int restoreCount = save();
        picture.draw(this);
        restoreToCount(restoreCount);
    }

    public void drawPicture(Picture picture, RectF dst) {
        save();
        translate(dst.left, dst.top);
        if (picture.getWidth() > 0 && picture.getHeight() > 0) {
            scale(dst.width() / ((float) picture.getWidth()), dst.height() / ((float) picture.getHeight()));
        }
        drawPicture(picture);
        restore();
    }

    public void drawPicture(Picture picture, Rect dst) {
        save();
        translate((float) dst.left, (float) dst.top);
        if (picture.getWidth() > 0 && picture.getHeight() > 0) {
            scale(((float) dst.width()) / ((float) picture.getWidth()), ((float) dst.height()) / ((float) picture.getHeight()));
        }
        drawPicture(picture);
        restore();
    }

    public enum VertexMode {
        TRIANGLES(0),
        TRIANGLE_STRIP(1),
        TRIANGLE_FAN(2);
        
        public final int nativeInt;

        private VertexMode(int nativeInt2) {
            this.nativeInt = nativeInt2;
        }
    }

    @UnsupportedAppUsage
    public void release() {
        this.mNativeCanvasWrapper = 0;
        Runnable runnable = this.mFinalizer;
        if (runnable != null) {
            runnable.run();
            this.mFinalizer = null;
        }
    }

    @UnsupportedAppUsage
    public static void freeCaches() {
        nFreeCaches();
    }

    @UnsupportedAppUsage
    public static void freeTextLayoutCaches() {
        nFreeTextLayoutCaches();
    }

    public static void setCompatibilityVersion(int apiLevel) {
        sCompatiblityVersion = apiLevel;
        nSetCompatibilityVersion(apiLevel);
    }

    @Override // android.graphics.BaseCanvas
    public void drawArc(RectF oval, float startAngle, float sweepAngle, boolean useCenter, Paint paint) {
        super.drawArc(oval, startAngle, sweepAngle, useCenter, paint);
    }

    @Override // android.graphics.BaseCanvas
    public void drawArc(float left, float top, float right, float bottom, float startAngle, float sweepAngle, boolean useCenter, Paint paint) {
        super.drawArc(left, top, right, bottom, startAngle, sweepAngle, useCenter, paint);
    }

    @Override // android.graphics.BaseCanvas
    public void drawARGB(int a, int r, int g, int b) {
        super.drawARGB(a, r, g, b);
    }

    @Override // android.graphics.BaseCanvas
    public void drawBitmap(Bitmap bitmap, float left, float top, Paint paint) {
        super.drawBitmap(bitmap, left, top, paint);
    }

    @Override // android.graphics.BaseCanvas
    public void drawBitmap(Bitmap bitmap, Rect src, RectF dst, Paint paint) {
        super.drawBitmap(bitmap, src, dst, paint);
    }

    @Override // android.graphics.BaseCanvas
    public void drawBitmap(Bitmap bitmap, Rect src, Rect dst, Paint paint) {
        super.drawBitmap(bitmap, src, dst, paint);
    }

    @Override // android.graphics.BaseCanvas
    @Deprecated
    public void drawBitmap(int[] colors, int offset, int stride, float x, float y, int width, int height, boolean hasAlpha, Paint paint) {
        super.drawBitmap(colors, offset, stride, x, y, width, height, hasAlpha, paint);
    }

    @Override // android.graphics.BaseCanvas
    @Deprecated
    public void drawBitmap(int[] colors, int offset, int stride, int x, int y, int width, int height, boolean hasAlpha, Paint paint) {
        super.drawBitmap(colors, offset, stride, x, y, width, height, hasAlpha, paint);
    }

    @Override // android.graphics.BaseCanvas
    public void drawBitmap(Bitmap bitmap, Matrix matrix, Paint paint) {
        super.drawBitmap(bitmap, matrix, paint);
    }

    @Override // android.graphics.BaseCanvas
    public void drawBitmapMesh(Bitmap bitmap, int meshWidth, int meshHeight, float[] verts, int vertOffset, int[] colors, int colorOffset, Paint paint) {
        super.drawBitmapMesh(bitmap, meshWidth, meshHeight, verts, vertOffset, colors, colorOffset, paint);
    }

    @Override // android.graphics.BaseCanvas
    public void drawCircle(float cx, float cy, float radius, Paint paint) {
        super.drawCircle(cx, cy, radius, paint);
    }

    @Override // android.graphics.BaseCanvas
    public void drawColor(int color) {
        super.drawColor(color);
    }

    public void drawColor(long color) {
        super.drawColor(color, BlendMode.SRC_OVER);
    }

    @Override // android.graphics.BaseCanvas
    public void drawColor(int color, PorterDuff.Mode mode) {
        super.drawColor(color, mode);
    }

    @Override // android.graphics.BaseCanvas
    public void drawColor(int color, BlendMode mode) {
        super.drawColor(color, mode);
    }

    @Override // android.graphics.BaseCanvas
    public void drawColor(long color, BlendMode mode) {
        super.drawColor(color, mode);
    }

    @Override // android.graphics.BaseCanvas
    public void drawLine(float startX, float startY, float stopX, float stopY, Paint paint) {
        super.drawLine(startX, startY, stopX, stopY, paint);
    }

    @Override // android.graphics.BaseCanvas
    public void drawLines(float[] pts, int offset, int count, Paint paint) {
        super.drawLines(pts, offset, count, paint);
    }

    @Override // android.graphics.BaseCanvas
    public void drawLines(float[] pts, Paint paint) {
        super.drawLines(pts, paint);
    }

    @Override // android.graphics.BaseCanvas
    public void drawOval(RectF oval, Paint paint) {
        super.drawOval(oval, paint);
    }

    @Override // android.graphics.BaseCanvas
    public void drawOval(float left, float top, float right, float bottom, Paint paint) {
        super.drawOval(left, top, right, bottom, paint);
    }

    @Override // android.graphics.BaseCanvas
    public void drawPaint(Paint paint) {
        super.drawPaint(paint);
    }

    @Override // android.graphics.BaseCanvas
    public void drawPatch(NinePatch patch, Rect dst, Paint paint) {
        super.drawPatch(patch, dst, paint);
    }

    @Override // android.graphics.BaseCanvas
    public void drawPatch(NinePatch patch, RectF dst, Paint paint) {
        super.drawPatch(patch, dst, paint);
    }

    @Override // android.graphics.BaseCanvas
    public void drawPath(Path path, Paint paint) {
        super.drawPath(path, paint);
    }

    @Override // android.graphics.BaseCanvas
    public void drawPoint(float x, float y, Paint paint) {
        super.drawPoint(x, y, paint);
    }

    @Override // android.graphics.BaseCanvas
    public void drawPoints(float[] pts, int offset, int count, Paint paint) {
        super.drawPoints(pts, offset, count, paint);
    }

    @Override // android.graphics.BaseCanvas
    public void drawPoints(float[] pts, Paint paint) {
        super.drawPoints(pts, paint);
    }

    @Override // android.graphics.BaseCanvas
    @Deprecated
    public void drawPosText(char[] text, int index, int count, float[] pos, Paint paint) {
        super.drawPosText(text, index, count, pos, paint);
    }

    @Override // android.graphics.BaseCanvas
    @Deprecated
    public void drawPosText(String text, float[] pos, Paint paint) {
        super.drawPosText(text, pos, paint);
    }

    @Override // android.graphics.BaseCanvas
    public void drawRect(RectF rect, Paint paint) {
        super.drawRect(rect, paint);
    }

    @Override // android.graphics.BaseCanvas
    public void drawRect(Rect r, Paint paint) {
        super.drawRect(r, paint);
    }

    @Override // android.graphics.BaseCanvas
    public void drawRect(float left, float top, float right, float bottom, Paint paint) {
        super.drawRect(left, top, right, bottom, paint);
    }

    @Override // android.graphics.BaseCanvas
    public void drawRGB(int r, int g, int b) {
        super.drawRGB(r, g, b);
    }

    @Override // android.graphics.BaseCanvas
    public void drawRoundRect(RectF rect, float rx, float ry, Paint paint) {
        super.drawRoundRect(rect, rx, ry, paint);
    }

    @Override // android.graphics.BaseCanvas
    public void drawRoundRect(float left, float top, float right, float bottom, float rx, float ry, Paint paint) {
        super.drawRoundRect(left, top, right, bottom, rx, ry, paint);
    }

    @Override // android.graphics.BaseCanvas
    public void drawDoubleRoundRect(RectF outer, float outerRx, float outerRy, RectF inner, float innerRx, float innerRy, Paint paint) {
        super.drawDoubleRoundRect(outer, outerRx, outerRy, inner, innerRx, innerRy, paint);
    }

    @Override // android.graphics.BaseCanvas
    public void drawDoubleRoundRect(RectF outer, float[] outerRadii, RectF inner, float[] innerRadii, Paint paint) {
        super.drawDoubleRoundRect(outer, outerRadii, inner, innerRadii, paint);
    }

    @Override // android.graphics.BaseCanvas
    public void drawText(char[] text, int index, int count, float x, float y, Paint paint) {
        super.drawText(text, index, count, x, y, paint);
    }

    @Override // android.graphics.BaseCanvas
    public void drawText(String text, float x, float y, Paint paint) {
        super.drawText(text, x, y, paint);
    }

    @Override // android.graphics.BaseCanvas
    public void drawText(String text, int start, int end, float x, float y, Paint paint) {
        super.drawText(text, start, end, x, y, paint);
    }

    @Override // android.graphics.BaseCanvas
    public void drawText(CharSequence text, int start, int end, float x, float y, Paint paint) {
        super.drawText(text, start, end, x, y, paint);
    }

    @Override // android.graphics.BaseCanvas
    public void drawTextOnPath(char[] text, int index, int count, Path path, float hOffset, float vOffset, Paint paint) {
        super.drawTextOnPath(text, index, count, path, hOffset, vOffset, paint);
    }

    @Override // android.graphics.BaseCanvas
    public void drawTextOnPath(String text, Path path, float hOffset, float vOffset, Paint paint) {
        super.drawTextOnPath(text, path, hOffset, vOffset, paint);
    }

    @Override // android.graphics.BaseCanvas
    public void drawTextRun(char[] text, int index, int count, int contextIndex, int contextCount, float x, float y, boolean isRtl, Paint paint) {
        super.drawTextRun(text, index, count, contextIndex, contextCount, x, y, isRtl, paint);
    }

    @Override // android.graphics.BaseCanvas
    public void drawTextRun(CharSequence text, int start, int end, int contextStart, int contextEnd, float x, float y, boolean isRtl, Paint paint) {
        super.drawTextRun(text, start, end, contextStart, contextEnd, x, y, isRtl, paint);
    }

    @Override // android.graphics.BaseCanvas
    public void drawTextRun(MeasuredText text, int start, int end, int contextStart, int contextEnd, float x, float y, boolean isRtl, Paint paint) {
        super.drawTextRun(text, start, end, contextStart, contextEnd, x, y, isRtl, paint);
    }

    @Override // android.graphics.BaseCanvas
    public void drawVertices(VertexMode mode, int vertexCount, float[] verts, int vertOffset, float[] texs, int texOffset, int[] colors, int colorOffset, short[] indices, int indexOffset, int indexCount, Paint paint) {
        super.drawVertices(mode, vertexCount, verts, vertOffset, texs, texOffset, colors, colorOffset, indices, indexOffset, indexCount, paint);
    }

    public void drawRenderNode(RenderNode renderNode) {
        throw new IllegalArgumentException("Software rendering doesn't support drawRenderNode");
    }
}
