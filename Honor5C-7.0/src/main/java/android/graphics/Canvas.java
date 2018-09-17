package android.graphics;

import android.graphics.Bitmap.Config;
import android.graphics.PorterDuff.Mode;
import android.graphics.Region.Op;
import android.os.Process;
import android.text.GraphicsOperations;
import android.text.SpannableString;
import android.text.SpannedString;
import android.text.TextUtils;
import javax.microedition.khronos.opengles.GL;
import libcore.util.NativeAllocationRegistry;

public class Canvas {
    public static final int ALL_SAVE_FLAG = 31;
    public static final int CLIP_SAVE_FLAG = 2;
    public static final int CLIP_TO_LAYER_SAVE_FLAG = 16;
    public static final int FULL_COLOR_LAYER_SAVE_FLAG = 8;
    public static final int HAS_ALPHA_LAYER_SAVE_FLAG = 4;
    public static final int MATRIX_SAVE_FLAG = 1;
    private static final int MAXMIMUM_BITMAP_SIZE = 32766;
    private static final long NATIVE_ALLOCATION_SIZE = 525;
    public static boolean sCompatibilityRestore;
    private Bitmap mBitmap;
    protected int mDensity;
    private DrawFilter mDrawFilter;
    private Runnable mFinalizer;
    protected long mNativeCanvasWrapper;
    protected int mScreenDensity;

    public enum EdgeType {
        ;
        
        public final int nativeInt;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.graphics.Canvas.EdgeType.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.graphics.Canvas.EdgeType.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.graphics.Canvas.EdgeType.<clinit>():void");
        }

        private EdgeType(int nativeInt) {
            this.nativeInt = nativeInt;
        }
    }

    private static class NoImagePreloadHolder {
        public static final NativeAllocationRegistry sRegistry = null;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.graphics.Canvas.NoImagePreloadHolder.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.graphics.Canvas.NoImagePreloadHolder.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.graphics.Canvas.NoImagePreloadHolder.<clinit>():void");
        }

        private NoImagePreloadHolder() {
        }
    }

    public enum VertexMode {
        ;
        
        public final int nativeInt;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.graphics.Canvas.VertexMode.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.graphics.Canvas.VertexMode.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.graphics.Canvas.VertexMode.<clinit>():void");
        }

        private VertexMode(int nativeInt) {
            this.nativeInt = nativeInt;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.graphics.Canvas.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.graphics.Canvas.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.graphics.Canvas.<clinit>():void");
    }

    public static native void freeCaches();

    public static native void freeTextLayoutCaches();

    private static native long getNativeFinalizer();

    private static native long initRaster(Bitmap bitmap);

    private static native void nativeDrawBitmapMatrix(long j, Bitmap bitmap, long j2, long j3);

    private static native void nativeDrawBitmapMesh(long j, Bitmap bitmap, int i, int i2, float[] fArr, int i3, int[] iArr, int i4, long j2);

    private static native void nativeDrawVertices(long j, int i, int i2, float[] fArr, int i3, float[] fArr2, int i4, int[] iArr, int i5, short[] sArr, int i6, int i7, long j2);

    private static native void nativeSetDrawFilter(long j, long j2);

    private static native boolean native_clipPath(long j, long j2, int i);

    private static native boolean native_clipRect(long j, float f, float f2, float f3, float f4, int i);

    private static native boolean native_clipRegion(long j, long j2, int i);

    private static native void native_concat(long j, long j2);

    private static native void native_drawArc(long j, float f, float f2, float f3, float f4, float f5, float f6, boolean z, long j2);

    private native void native_drawBitmap(long j, Bitmap bitmap, float f, float f2, float f3, float f4, float f5, float f6, float f7, float f8, long j2, int i, int i2);

    private native void native_drawBitmap(long j, Bitmap bitmap, float f, float f2, long j2, int i, int i2, int i3);

    private static native void native_drawBitmap(long j, int[] iArr, int i, int i2, float f, float f2, int i3, int i4, boolean z, long j2);

    private static native void native_drawCircle(long j, float f, float f2, float f3, long j2);

    private static native void native_drawColor(long j, int i, int i2);

    private static native void native_drawLine(long j, float f, float f2, float f3, float f4, long j2);

    private static native void native_drawLines(long j, float[] fArr, int i, int i2, long j2);

    private native void native_drawNinePatch(long j, long j2, long j3, float f, float f2, float f3, float f4, long j4, int i, int i2);

    private static native void native_drawOval(long j, float f, float f2, float f3, float f4, long j2);

    private static native void native_drawPaint(long j, long j2);

    private static native void native_drawPath(long j, long j2, long j3);

    private static native void native_drawPoint(long j, float f, float f2, long j2);

    private static native void native_drawPoints(long j, float[] fArr, int i, int i2, long j2);

    private static native void native_drawRect(long j, float f, float f2, float f3, float f4, long j2);

    private static native void native_drawRegion(long j, long j2, long j3);

    private static native void native_drawRoundRect(long j, float f, float f2, float f3, float f4, float f5, float f6, long j2);

    private static native void native_drawText(long j, String str, int i, int i2, float f, float f2, int i3, long j2, long j3);

    private static native void native_drawText(long j, char[] cArr, int i, int i2, float f, float f2, int i3, long j2, long j3);

    private static native void native_drawTextOnPath(long j, String str, long j2, float f, float f2, int i, long j3, long j4);

    private static native void native_drawTextOnPath(long j, char[] cArr, int i, int i2, long j2, float f, float f2, int i3, long j3, long j4);

    private static native void native_drawTextRun(long j, String str, int i, int i2, int i3, int i4, float f, float f2, boolean z, long j2, long j3);

    private static native void native_drawTextRun(long j, char[] cArr, int i, int i2, int i3, int i4, float f, float f2, boolean z, long j2, long j3);

    private static native void native_getCTM(long j, long j2);

    private static native boolean native_getClipBounds(long j, Rect rect);

    private static native int native_getHeight(long j);

    private static native int native_getSaveCount(long j);

    private static native int native_getWidth(long j);

    private static native boolean native_isOpaque(long j);

    private static native boolean native_quickReject(long j, float f, float f2, float f3, float f4);

    private static native boolean native_quickReject(long j, long j2);

    private static native void native_restore(long j, boolean z);

    private static native void native_restoreToCount(long j, int i, boolean z);

    private static native void native_rotate(long j, float f);

    private static native int native_save(long j, int i);

    private static native int native_saveLayer(long j, float f, float f2, float f3, float f4, long j2, int i);

    private static native int native_saveLayerAlpha(long j, float f, float f2, float f3, float f4, int i, int i2);

    private static native void native_scale(long j, float f, float f2);

    private static native void native_setBitmap(long j, Bitmap bitmap);

    private static native void native_setHighContrastText(long j, boolean z);

    private static native void native_setMatrix(long j, long j2);

    private static native void native_skew(long j, float f, float f2);

    private static native void native_translate(long j, float f, float f2);

    public long getNativeCanvasWrapper() {
        return this.mNativeCanvasWrapper;
    }

    public boolean isRecordingFor(Object o) {
        return false;
    }

    public Canvas() {
        this.mDensity = 0;
        this.mScreenDensity = 0;
        if (isHardwareAccelerated()) {
            this.mFinalizer = null;
            return;
        }
        this.mNativeCanvasWrapper = initRaster(null);
        this.mFinalizer = NoImagePreloadHolder.sRegistry.registerNativeAllocation(this, this.mNativeCanvasWrapper);
    }

    public Canvas(Bitmap bitmap) {
        this.mDensity = 0;
        this.mScreenDensity = 0;
        if (bitmap.isMutable()) {
            throwIfCannotDraw(bitmap);
            this.mNativeCanvasWrapper = initRaster(bitmap);
            this.mFinalizer = NoImagePreloadHolder.sRegistry.registerNativeAllocation(this, this.mNativeCanvasWrapper);
            this.mBitmap = bitmap;
            this.mDensity = bitmap.mDensity;
            return;
        }
        throw new IllegalStateException("Immutable bitmap passed to Canvas constructor");
    }

    public Canvas(long nativeCanvas) {
        this.mDensity = 0;
        this.mScreenDensity = 0;
        if (nativeCanvas == 0) {
            throw new IllegalStateException();
        }
        this.mNativeCanvasWrapper = nativeCanvas;
        this.mFinalizer = NoImagePreloadHolder.sRegistry.registerNativeAllocation(this, this.mNativeCanvasWrapper);
        this.mDensity = Bitmap.getDefaultDensity();
    }

    @Deprecated
    protected GL getGL() {
        return null;
    }

    public boolean isHardwareAccelerated() {
        return false;
    }

    public void setBitmap(Bitmap bitmap) {
        if (isHardwareAccelerated()) {
            throw new RuntimeException("Can't set a bitmap device on a HW accelerated canvas");
        }
        if (bitmap == null) {
            native_setBitmap(this.mNativeCanvasWrapper, null);
            this.mDensity = 0;
        } else if (bitmap.isMutable()) {
            throwIfCannotDraw(bitmap);
            native_setBitmap(this.mNativeCanvasWrapper, bitmap);
            this.mDensity = bitmap.mDensity;
        } else {
            throw new IllegalStateException();
        }
        this.mBitmap = bitmap;
    }

    public void setHighContrastText(boolean highContrastText) {
        native_setHighContrastText(this.mNativeCanvasWrapper, highContrastText);
    }

    public void insertReorderBarrier() {
    }

    public void insertInorderBarrier() {
    }

    public boolean isOpaque() {
        return native_isOpaque(this.mNativeCanvasWrapper);
    }

    public int getWidth() {
        return native_getWidth(this.mNativeCanvasWrapper);
    }

    public int getHeight() {
        return native_getHeight(this.mNativeCanvasWrapper);
    }

    public int getDensity() {
        return this.mDensity;
    }

    public void setDensity(int density) {
        if (this.mBitmap != null) {
            this.mBitmap.setDensity(density);
        }
        this.mDensity = density;
    }

    public void setScreenDensity(int density) {
        this.mScreenDensity = density;
    }

    public int getMaximumBitmapWidth() {
        return MAXMIMUM_BITMAP_SIZE;
    }

    public int getMaximumBitmapHeight() {
        return MAXMIMUM_BITMAP_SIZE;
    }

    public int save() {
        return native_save(this.mNativeCanvasWrapper, 3);
    }

    public int save(int saveFlags) {
        return native_save(this.mNativeCanvasWrapper, saveFlags);
    }

    public int saveLayer(RectF bounds, Paint paint, int saveFlags) {
        if (bounds == null) {
            bounds = new RectF(getClipBounds());
        }
        return saveLayer(bounds.left, bounds.top, bounds.right, bounds.bottom, paint, saveFlags);
    }

    public int saveLayer(RectF bounds, Paint paint) {
        return saveLayer(bounds, paint, ALL_SAVE_FLAG);
    }

    public int saveLayer(float left, float top, float right, float bottom, Paint paint, int saveFlags) {
        return native_saveLayer(this.mNativeCanvasWrapper, left, top, right, bottom, paint != null ? paint.getNativeInstance() : 0, saveFlags);
    }

    public int saveLayer(float left, float top, float right, float bottom, Paint paint) {
        return saveLayer(left, top, right, bottom, paint, ALL_SAVE_FLAG);
    }

    public int saveLayerAlpha(RectF bounds, int alpha, int saveFlags) {
        if (bounds == null) {
            bounds = new RectF(getClipBounds());
        }
        return saveLayerAlpha(bounds.left, bounds.top, bounds.right, bounds.bottom, alpha, saveFlags);
    }

    public int saveLayerAlpha(RectF bounds, int alpha) {
        return saveLayerAlpha(bounds, alpha, ALL_SAVE_FLAG);
    }

    public int saveLayerAlpha(float left, float top, float right, float bottom, int alpha, int saveFlags) {
        return native_saveLayerAlpha(this.mNativeCanvasWrapper, left, top, right, bottom, Math.min(Process.PROC_TERM_MASK, Math.max(0, alpha)), saveFlags);
    }

    public int saveLayerAlpha(float left, float top, float right, float bottom, int alpha) {
        return saveLayerAlpha(left, top, right, bottom, alpha, ALL_SAVE_FLAG);
    }

    public void restore() {
        boolean throwOnUnderflow = (sCompatibilityRestore && isHardwareAccelerated()) ? false : true;
        native_restore(this.mNativeCanvasWrapper, throwOnUnderflow);
    }

    public int getSaveCount() {
        return native_getSaveCount(this.mNativeCanvasWrapper);
    }

    public void restoreToCount(int saveCount) {
        boolean throwOnUnderflow = (sCompatibilityRestore && isHardwareAccelerated()) ? false : true;
        native_restoreToCount(this.mNativeCanvasWrapper, saveCount, throwOnUnderflow);
    }

    public void translate(float dx, float dy) {
        native_translate(this.mNativeCanvasWrapper, dx, dy);
    }

    public void scale(float sx, float sy) {
        native_scale(this.mNativeCanvasWrapper, sx, sy);
    }

    public final void scale(float sx, float sy, float px, float py) {
        translate(px, py);
        scale(sx, sy);
        translate(-px, -py);
    }

    public void rotate(float degrees) {
        native_rotate(this.mNativeCanvasWrapper, degrees);
    }

    public final void rotate(float degrees, float px, float py) {
        translate(px, py);
        rotate(degrees);
        translate(-px, -py);
    }

    public void skew(float sx, float sy) {
        native_skew(this.mNativeCanvasWrapper, sx, sy);
    }

    public void concat(Matrix matrix) {
        if (matrix != null) {
            native_concat(this.mNativeCanvasWrapper, matrix.native_instance);
        }
    }

    public void setMatrix(Matrix matrix) {
        native_setMatrix(this.mNativeCanvasWrapper, matrix == null ? 0 : matrix.native_instance);
    }

    @Deprecated
    public void getMatrix(Matrix ctm) {
        native_getCTM(this.mNativeCanvasWrapper, ctm.native_instance);
    }

    @Deprecated
    public final Matrix getMatrix() {
        Matrix m = new Matrix();
        getMatrix(m);
        return m;
    }

    public boolean clipRect(RectF rect, Op op) {
        return native_clipRect(this.mNativeCanvasWrapper, rect.left, rect.top, rect.right, rect.bottom, op.nativeInt);
    }

    public boolean clipRect(Rect rect, Op op) {
        return native_clipRect(this.mNativeCanvasWrapper, (float) rect.left, (float) rect.top, (float) rect.right, (float) rect.bottom, op.nativeInt);
    }

    public boolean clipRect(RectF rect) {
        return native_clipRect(this.mNativeCanvasWrapper, rect.left, rect.top, rect.right, rect.bottom, Op.INTERSECT.nativeInt);
    }

    public boolean clipRect(Rect rect) {
        return native_clipRect(this.mNativeCanvasWrapper, (float) rect.left, (float) rect.top, (float) rect.right, (float) rect.bottom, Op.INTERSECT.nativeInt);
    }

    public boolean clipRect(float left, float top, float right, float bottom, Op op) {
        return native_clipRect(this.mNativeCanvasWrapper, left, top, right, bottom, op.nativeInt);
    }

    public boolean clipRect(float left, float top, float right, float bottom) {
        return native_clipRect(this.mNativeCanvasWrapper, left, top, right, bottom, Op.INTERSECT.nativeInt);
    }

    public boolean clipRect(int left, int top, int right, int bottom) {
        return native_clipRect(this.mNativeCanvasWrapper, (float) left, (float) top, (float) right, (float) bottom, Op.INTERSECT.nativeInt);
    }

    public boolean clipPath(Path path, Op op) {
        return native_clipPath(this.mNativeCanvasWrapper, path.ni(), op.nativeInt);
    }

    public boolean clipPath(Path path) {
        return clipPath(path, Op.INTERSECT);
    }

    @Deprecated
    public boolean clipRegion(Region region, Op op) {
        return native_clipRegion(this.mNativeCanvasWrapper, region.ni(), op.nativeInt);
    }

    @Deprecated
    public boolean clipRegion(Region region) {
        return clipRegion(region, Op.INTERSECT);
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
        nativeSetDrawFilter(this.mNativeCanvasWrapper, nativeFilter);
    }

    public boolean quickReject(RectF rect, EdgeType type) {
        return native_quickReject(this.mNativeCanvasWrapper, rect.left, rect.top, rect.right, rect.bottom);
    }

    public boolean quickReject(Path path, EdgeType type) {
        return native_quickReject(this.mNativeCanvasWrapper, path.ni());
    }

    public boolean quickReject(float left, float top, float right, float bottom, EdgeType type) {
        return native_quickReject(this.mNativeCanvasWrapper, left, top, right, bottom);
    }

    public boolean getClipBounds(Rect bounds) {
        return native_getClipBounds(this.mNativeCanvasWrapper, bounds);
    }

    public final Rect getClipBounds() {
        Rect r = new Rect();
        getClipBounds(r);
        return r;
    }

    public void drawRGB(int r, int g, int b) {
        drawColor(Color.rgb(r, g, b));
    }

    public void drawARGB(int a, int r, int g, int b) {
        drawColor(Color.argb(a, r, g, b));
    }

    public void drawColor(int color) {
        native_drawColor(this.mNativeCanvasWrapper, color, Mode.SRC_OVER.nativeInt);
    }

    public void drawColor(int color, Mode mode) {
        native_drawColor(this.mNativeCanvasWrapper, color, mode.nativeInt);
    }

    public void drawPaint(Paint paint) {
        native_drawPaint(this.mNativeCanvasWrapper, paint.getNativeInstance());
    }

    public void drawPoints(float[] pts, int offset, int count, Paint paint) {
        native_drawPoints(this.mNativeCanvasWrapper, pts, offset, count, paint.getNativeInstance());
    }

    public void drawPoints(float[] pts, Paint paint) {
        drawPoints(pts, 0, pts.length, paint);
    }

    public void drawPoint(float x, float y, Paint paint) {
        native_drawPoint(this.mNativeCanvasWrapper, x, y, paint.getNativeInstance());
    }

    public void drawLine(float startX, float startY, float stopX, float stopY, Paint paint) {
        native_drawLine(this.mNativeCanvasWrapper, startX, startY, stopX, stopY, paint.getNativeInstance());
    }

    public void drawLines(float[] pts, int offset, int count, Paint paint) {
        native_drawLines(this.mNativeCanvasWrapper, pts, offset, count, paint.getNativeInstance());
    }

    public void drawLines(float[] pts, Paint paint) {
        drawLines(pts, 0, pts.length, paint);
    }

    public void drawRect(RectF rect, Paint paint) {
        native_drawRect(this.mNativeCanvasWrapper, rect.left, rect.top, rect.right, rect.bottom, paint.getNativeInstance());
    }

    public void drawRect(Rect r, Paint paint) {
        drawRect((float) r.left, (float) r.top, (float) r.right, (float) r.bottom, paint);
    }

    public void drawRect(float left, float top, float right, float bottom, Paint paint) {
        native_drawRect(this.mNativeCanvasWrapper, left, top, right, bottom, paint.getNativeInstance());
    }

    public void drawOval(RectF oval, Paint paint) {
        if (oval == null) {
            throw new NullPointerException();
        }
        drawOval(oval.left, oval.top, oval.right, oval.bottom, paint);
    }

    public void drawOval(float left, float top, float right, float bottom, Paint paint) {
        native_drawOval(this.mNativeCanvasWrapper, left, top, right, bottom, paint.getNativeInstance());
    }

    public void drawCircle(float cx, float cy, float radius, Paint paint) {
        native_drawCircle(this.mNativeCanvasWrapper, cx, cy, radius, paint.getNativeInstance());
    }

    public void drawArc(RectF oval, float startAngle, float sweepAngle, boolean useCenter, Paint paint) {
        drawArc(oval.left, oval.top, oval.right, oval.bottom, startAngle, sweepAngle, useCenter, paint);
    }

    public void drawArc(float left, float top, float right, float bottom, float startAngle, float sweepAngle, boolean useCenter, Paint paint) {
        native_drawArc(this.mNativeCanvasWrapper, left, top, right, bottom, startAngle, sweepAngle, useCenter, paint.getNativeInstance());
    }

    public void drawRoundRect(RectF rect, float rx, float ry, Paint paint) {
        drawRoundRect(rect.left, rect.top, rect.right, rect.bottom, rx, ry, paint);
    }

    public void drawRoundRect(float left, float top, float right, float bottom, float rx, float ry, Paint paint) {
        native_drawRoundRect(this.mNativeCanvasWrapper, left, top, right, bottom, rx, ry, paint.getNativeInstance());
    }

    public void drawPath(Path path, Paint paint) {
        if (!path.isSimplePath || path.rects == null) {
            native_drawPath(this.mNativeCanvasWrapper, path.ni(), paint.getNativeInstance());
        } else {
            native_drawRegion(this.mNativeCanvasWrapper, path.rects.mNativeRegion, paint.getNativeInstance());
        }
    }

    protected void throwIfCannotDraw(Bitmap bitmap) {
        if (bitmap.isRecycled()) {
            throw new RuntimeException("Canvas: trying to use a recycled bitmap " + bitmap);
        } else if (!bitmap.isPremultiplied() && bitmap.getConfig() == Config.ARGB_8888 && bitmap.hasAlpha()) {
            throw new RuntimeException("Canvas: trying to use a non-premultiplied bitmap " + bitmap);
        }
    }

    public void drawPatch(NinePatch patch, Rect dst, Paint paint) {
        Bitmap bitmap = patch.getBitmap();
        throwIfCannotDraw(bitmap);
        native_drawNinePatch(this.mNativeCanvasWrapper, bitmap.getNativeInstance(), patch.mNativeChunk, (float) dst.left, (float) dst.top, (float) dst.right, (float) dst.bottom, paint == null ? 0 : paint.getNativeInstance(), this.mDensity, patch.getDensity());
    }

    public void drawPatch(NinePatch patch, RectF dst, Paint paint) {
        Bitmap bitmap = patch.getBitmap();
        throwIfCannotDraw(bitmap);
        native_drawNinePatch(this.mNativeCanvasWrapper, bitmap.getNativeInstance(), patch.mNativeChunk, dst.left, dst.top, dst.right, dst.bottom, paint == null ? 0 : paint.getNativeInstance(), this.mDensity, patch.getDensity());
    }

    public void drawBitmap(Bitmap bitmap, float left, float top, Paint paint) {
        throwIfCannotDraw(bitmap);
        native_drawBitmap(this.mNativeCanvasWrapper, bitmap, left, top, paint != null ? paint.getNativeInstance() : 0, this.mDensity, this.mScreenDensity, bitmap.mDensity);
    }

    public void drawBitmap(Bitmap bitmap, Rect src, RectF dst, Paint paint) {
        if (dst == null) {
            throw new NullPointerException();
        }
        float top;
        float left;
        float right;
        float bottom;
        throwIfCannotDraw(bitmap);
        long nativePaint = paint == null ? 0 : paint.getNativeInstance();
        if (src == null) {
            top = 0.0f;
            left = 0.0f;
            right = (float) bitmap.getWidth();
            bottom = (float) bitmap.getHeight();
        } else {
            left = (float) src.left;
            right = (float) src.right;
            top = (float) src.top;
            bottom = (float) src.bottom;
        }
        native_drawBitmap(this.mNativeCanvasWrapper, bitmap, left, top, right, bottom, dst.left, dst.top, dst.right, dst.bottom, nativePaint, this.mScreenDensity, bitmap.mDensity);
    }

    public void drawBitmap(Bitmap bitmap, Rect src, Rect dst, Paint paint) {
        if (dst == null) {
            throw new NullPointerException();
        }
        int top;
        int left;
        int right;
        int bottom;
        throwIfCannotDraw(bitmap);
        long nativePaint = paint == null ? 0 : paint.getNativeInstance();
        if (src == null) {
            top = 0;
            left = 0;
            right = bitmap.getWidth();
            bottom = bitmap.getHeight();
        } else {
            left = src.left;
            right = src.right;
            top = src.top;
            bottom = src.bottom;
        }
        native_drawBitmap(this.mNativeCanvasWrapper, bitmap, (float) left, (float) top, (float) right, (float) bottom, (float) dst.left, (float) dst.top, (float) dst.right, (float) dst.bottom, nativePaint, this.mScreenDensity, bitmap.mDensity);
    }

    @Deprecated
    public void drawBitmap(int[] colors, int offset, int stride, float x, float y, int width, int height, boolean hasAlpha, Paint paint) {
        if (width < 0) {
            throw new IllegalArgumentException("width must be >= 0");
        } else if (height < 0) {
            throw new IllegalArgumentException("height must be >= 0");
        } else if (Math.abs(stride) < width) {
            throw new IllegalArgumentException("abs(stride) must be >= width");
        } else {
            int lastScanline = offset + ((height - 1) * stride);
            int length = colors.length;
            if (offset < 0 || offset + width > length || lastScanline < 0 || lastScanline + width > length) {
                throw new ArrayIndexOutOfBoundsException();
            } else if (width != 0 && height != 0) {
                native_drawBitmap(this.mNativeCanvasWrapper, colors, offset, stride, x, y, width, height, hasAlpha, paint != null ? paint.getNativeInstance() : 0);
            }
        }
    }

    @Deprecated
    public void drawBitmap(int[] colors, int offset, int stride, int x, int y, int width, int height, boolean hasAlpha, Paint paint) {
        drawBitmap(colors, offset, stride, (float) x, (float) y, width, height, hasAlpha, paint);
    }

    public void drawBitmap(Bitmap bitmap, Matrix matrix, Paint paint) {
        if (paint != null && UidMatcher.flag == MATRIX_SAVE_FLAG) {
            paint.setFilterBitmap(true);
            paint.setDither(false);
            paint.setAntiAlias(true);
        }
        nativeDrawBitmapMatrix(this.mNativeCanvasWrapper, bitmap, matrix.ni(), paint != null ? paint.getNativeInstance() : 0);
    }

    protected static void checkRange(int length, int offset, int count) {
        if ((offset | count) < 0 || offset + count > length) {
            throw new ArrayIndexOutOfBoundsException();
        }
    }

    public void drawBitmapMesh(Bitmap bitmap, int meshWidth, int meshHeight, float[] verts, int vertOffset, int[] colors, int colorOffset, Paint paint) {
        if ((((meshWidth | meshHeight) | vertOffset) | colorOffset) < 0) {
            throw new ArrayIndexOutOfBoundsException();
        } else if (meshWidth != 0 && meshHeight != 0) {
            int count = (meshWidth + MATRIX_SAVE_FLAG) * (meshHeight + MATRIX_SAVE_FLAG);
            checkRange(verts.length, vertOffset, count * CLIP_SAVE_FLAG);
            if (colors != null) {
                checkRange(colors.length, colorOffset, count);
            }
            nativeDrawBitmapMesh(this.mNativeCanvasWrapper, bitmap, meshWidth, meshHeight, verts, vertOffset, colors, colorOffset, paint != null ? paint.getNativeInstance() : 0);
        }
    }

    public void drawVertices(VertexMode mode, int vertexCount, float[] verts, int vertOffset, float[] texs, int texOffset, int[] colors, int colorOffset, short[] indices, int indexOffset, int indexCount, Paint paint) {
        checkRange(verts.length, vertOffset, vertexCount);
        if (!isHardwareAccelerated()) {
            if (texs != null) {
                checkRange(texs.length, texOffset, vertexCount);
            }
            if (colors != null) {
                checkRange(colors.length, colorOffset, vertexCount / CLIP_SAVE_FLAG);
            }
            if (indices != null) {
                checkRange(indices.length, indexOffset, indexCount);
            }
            nativeDrawVertices(this.mNativeCanvasWrapper, mode.nativeInt, vertexCount, verts, vertOffset, texs, texOffset, colors, colorOffset, indices, indexOffset, indexCount, paint.getNativeInstance());
        }
    }

    public void drawText(char[] text, int index, int count, float x, float y, Paint paint) {
        if ((((index | count) | (index + count)) | ((text.length - index) - count)) < 0) {
            throw new IndexOutOfBoundsException();
        }
        native_drawText(this.mNativeCanvasWrapper, text, index, count, x, y, paint.mBidiFlags, paint.getNativeInstance(), paint.mNativeTypeface);
    }

    public void drawText(String text, float x, float y, Paint paint) {
        native_drawText(this.mNativeCanvasWrapper, text, 0, text.length(), x, y, paint.mBidiFlags, paint.getNativeInstance(), paint.mNativeTypeface);
    }

    public void drawText(String text, int start, int end, float x, float y, Paint paint) {
        if ((((start | end) | (end - start)) | (text.length() - end)) < 0) {
            throw new IndexOutOfBoundsException();
        }
        native_drawText(this.mNativeCanvasWrapper, text, start, end, x, y, paint.mBidiFlags, paint.getNativeInstance(), paint.mNativeTypeface);
    }

    public void drawText(CharSequence text, int start, int end, float x, float y, Paint paint) {
        if ((((start | end) | (end - start)) | (text.length() - end)) < 0) {
            throw new IndexOutOfBoundsException();
        } else if ((text instanceof String) || (text instanceof SpannedString) || (text instanceof SpannableString)) {
            native_drawText(this.mNativeCanvasWrapper, text.toString(), start, end, x, y, paint.mBidiFlags, paint.getNativeInstance(), paint.mNativeTypeface);
        } else if (text instanceof GraphicsOperations) {
            ((GraphicsOperations) text).drawText(this, start, end, x, y, paint);
        } else {
            char[] buf = TemporaryBuffer.obtain(end - start);
            TextUtils.getChars(text, start, end, buf, 0);
            native_drawText(this.mNativeCanvasWrapper, buf, 0, end - start, x, y, paint.mBidiFlags, paint.getNativeInstance(), paint.mNativeTypeface);
            TemporaryBuffer.recycle(buf);
        }
    }

    public void drawTextRun(char[] text, int index, int count, int contextIndex, int contextCount, float x, float y, boolean isRtl, Paint paint) {
        if (text == null) {
            throw new NullPointerException("text is null");
        } else if (paint == null) {
            throw new NullPointerException("paint is null");
        } else if (((((((index | count) | contextIndex) | contextCount) | (index - contextIndex)) | ((contextIndex + contextCount) - (index + count))) | (text.length - (contextIndex + contextCount))) < 0) {
            throw new IndexOutOfBoundsException();
        } else {
            native_drawTextRun(this.mNativeCanvasWrapper, text, index, count, contextIndex, contextCount, x, y, isRtl, paint.getNativeInstance(), paint.mNativeTypeface);
        }
    }

    public void drawTextRun(CharSequence text, int start, int end, int contextStart, int contextEnd, float x, float y, boolean isRtl, Paint paint) {
        if (text == null) {
            throw new NullPointerException("text is null");
        } else if (paint == null) {
            throw new NullPointerException("paint is null");
        } else if ((((((((start | end) | contextStart) | contextEnd) | (start - contextStart)) | (end - start)) | (contextEnd - end)) | (text.length() - contextEnd)) < 0) {
            throw new IndexOutOfBoundsException();
        } else if ((text instanceof String) || (text instanceof SpannedString) || (text instanceof SpannableString)) {
            int i = start;
            r8 = end;
            int i2 = contextStart;
            r10 = contextEnd;
            r11 = x;
            r12 = y;
            r13 = isRtl;
            native_drawTextRun(this.mNativeCanvasWrapper, text.toString(), i, r8, i2, r10, r11, r12, r13, paint.getNativeInstance(), paint.mNativeTypeface);
        } else if (text instanceof GraphicsOperations) {
            ((GraphicsOperations) text).drawTextRun(this, start, end, contextStart, contextEnd, x, y, isRtl, paint);
        } else {
            r10 = contextEnd - contextStart;
            r8 = end - start;
            char[] buf = TemporaryBuffer.obtain(r10);
            TextUtils.getChars(text, contextStart, contextEnd, buf, 0);
            r11 = x;
            r12 = y;
            r13 = isRtl;
            native_drawTextRun(this.mNativeCanvasWrapper, buf, start - contextStart, r8, 0, r10, r11, r12, r13, paint.getNativeInstance(), paint.mNativeTypeface);
            TemporaryBuffer.recycle(buf);
        }
    }

    @Deprecated
    public void drawPosText(char[] text, int index, int count, float[] pos, Paint paint) {
        if (index < 0 || index + count > text.length || count * CLIP_SAVE_FLAG > pos.length) {
            throw new IndexOutOfBoundsException();
        }
        for (int i = 0; i < count; i += MATRIX_SAVE_FLAG) {
            drawText(text, index + i, (int) MATRIX_SAVE_FLAG, pos[i * CLIP_SAVE_FLAG], pos[(i * CLIP_SAVE_FLAG) + MATRIX_SAVE_FLAG], paint);
        }
    }

    @Deprecated
    public void drawPosText(String text, float[] pos, Paint paint) {
        drawPosText(text.toCharArray(), 0, text.length(), pos, paint);
    }

    public void drawTextOnPath(char[] text, int index, int count, Path path, float hOffset, float vOffset, Paint paint) {
        if (index < 0 || index + count > text.length) {
            throw new ArrayIndexOutOfBoundsException();
        }
        native_drawTextOnPath(this.mNativeCanvasWrapper, text, index, count, path.ni(), hOffset, vOffset, paint.mBidiFlags, paint.getNativeInstance(), paint.mNativeTypeface);
    }

    public void drawTextOnPath(String text, Path path, float hOffset, float vOffset, Paint paint) {
        if (text.length() > 0) {
            native_drawTextOnPath(this.mNativeCanvasWrapper, text, path.ni(), hOffset, vOffset, paint.mBidiFlags, paint.getNativeInstance(), paint.mNativeTypeface);
        }
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

    public void release() {
        this.mNativeCanvasWrapper = 0;
        if (this.mFinalizer != null) {
            this.mFinalizer.run();
            this.mFinalizer = null;
        }
    }
}
