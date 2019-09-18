package android.graphics;

import android.app.ActivityThread;
import android.app.Application;
import android.content.pm.IPackageManager;
import android.content.res.ResourcesImpl;
import android.graphics.BitmapFactory;
import android.graphics.ColorSpace;
import android.graphics.NinePatch;
import android.hwgallerycache.HwGalleryCacheManager;
import android.os.Binder;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.StrictMode;
import android.os.Trace;
import android.util.DisplayMetrics;
import android.util.HwPCUtils;
import android.util.Log;
import android.view.DisplayListCanvas;
import android.view.RenderNode;
import android.view.ThreadedRenderer;
import java.io.OutputStream;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.HashMap;
import libcore.util.NativeAllocationRegistry;

public final class Bitmap implements Parcelable {
    public static final Parcelable.Creator<Bitmap> CREATOR = new Parcelable.Creator<Bitmap>() {
        public Bitmap createFromParcel(Parcel p) {
            Bitmap bm = Bitmap.nativeCreateFromParcel(p);
            if (bm != null) {
                return bm;
            }
            throw new RuntimeException("Failed to unparcel Bitmap");
        }

        public Bitmap[] newArray(int size) {
            return new Bitmap[size];
        }
    };
    public static final int DENSITY_NONE = 0;
    private static final long NATIVE_ALLOCATION_SIZE = 32;
    private static final String TAG = "Bitmap";
    private static final int WORKING_COMPRESS_STORAGE = 4096;
    private static volatile int sDefaultDensity = -1;
    public static volatile int sPreloadTracingNumInstantiatedBitmaps;
    public static volatile long sPreloadTracingTotalBitmapsSize;
    public GalleryCacheInfo mCacheInfo = null;
    private ColorSpace mColorSpace;
    public int mDensity = getDefaultDensity();
    public boolean mGalleryCached = false;
    private int mHeight;
    private final boolean mIsMutable;
    private final long mNativePtr;
    private byte[] mNinePatchChunk;
    private NinePatch.InsetStruct mNinePatchInsets;
    private boolean mRecycled;
    private volatile int mReferenceCount = 0;
    private boolean mRequestPremultiplied;
    private int mWidth;

    public enum CompressFormat {
        JPEG(0),
        PNG(1),
        WEBP(2);
        
        final int nativeInt;

        private CompressFormat(int nativeInt2) {
            this.nativeInt = nativeInt2;
        }
    }

    public enum Config {
        ALPHA_8(1),
        RGB_565(3),
        ARGB_4444(4),
        ARGB_8888(5),
        RGBA_F16(6),
        HARDWARE(7),
        YCRCB_420_SP(8);
        
        private static Config[] sConfigs;
        final int nativeInt;

        static {
            sConfigs = new Config[]{null, ALPHA_8, null, RGB_565, ARGB_4444, ARGB_8888, RGBA_F16, HARDWARE, YCRCB_420_SP};
        }

        private Config(int ni) {
            this.nativeInt = ni;
        }

        static Config nativeToConfig(int ni) {
            return sConfigs[ni];
        }
    }

    public static class GalleryCacheInfo {
        /* access modifiers changed from: private */
        public boolean mFilter = false;
        private boolean mIsDecoding = false;
        private GalleryCacheInfo mLast = null;
        /* access modifiers changed from: private */
        public Matrix mMatrix = null;
        private GalleryCacheInfo mNext = null;
        private BitmapFactory.Options mOptions = null;
        private String mPath = null;
        private boolean mRecycled = false;
        private long mRef = 1;
        private Bitmap mWechatThumb = null;

        public String getPath() {
            return this.mPath;
        }

        public BitmapFactory.Options getOptions() {
            return this.mOptions;
        }

        public boolean getIsDecoding() {
            return this.mIsDecoding;
        }

        public Bitmap getWechatThumb() {
            return this.mWechatThumb;
        }

        public GalleryCacheInfo getLast() {
            return this.mLast;
        }

        public GalleryCacheInfo getNext() {
            return this.mNext;
        }

        public Matrix getMatrix() {
            return this.mMatrix;
        }

        public boolean getFilter() {
            return this.mFilter;
        }

        public void setPath(String path) {
            this.mPath = path;
        }

        public void setOptions(BitmapFactory.Options options) {
            this.mOptions = options;
        }

        public void setIsDecoding(boolean isDecoding) {
            this.mIsDecoding = isDecoding;
        }

        public void setWechatThumb(Bitmap wechatThumb) {
            this.mWechatThumb = wechatThumb;
        }

        public void setLast(GalleryCacheInfo last) {
            this.mLast = last;
        }

        public void setNext(GalleryCacheInfo next) {
            this.mNext = next;
        }

        public void setMatrix(Matrix matrix) {
            this.mMatrix = matrix;
        }

        public void setFilter(boolean filter) {
            this.mFilter = filter;
        }

        public void addRef() {
            this.mRef++;
        }

        public void recycle() {
            if (!this.mRecycled) {
                long j = this.mRef - 1;
                this.mRef = j;
                if (j <= 0) {
                    HwGalleryCacheManager.recycleCacheInfo(this);
                    this.mRecycled = true;
                }
            }
        }
    }

    static class UidMatcher {
        static final int FLAG_ANTIALIAS = 1;
        static final int FLAG_COMPRESS = 2;
        static final int FLAG_NONE = 0;
        static final int flag = matchUid();
        private static final HashMap<String, Integer> sMatcher = new HashMap<>();

        UidMatcher() {
        }

        static {
            sMatcher.put("com.sina.weibo", 1);
            sMatcher.put("com.tencent.mm", 2);
        }

        private static int matchUid() {
            IPackageManager pm = IPackageManager.Stub.asInterface(ServiceManager.getService("package"));
            if (pm == null) {
                return 0;
            }
            try {
                String[] pkgs = pm.getPackagesForUid(Binder.getCallingUid());
                if (pkgs == null) {
                    return 0;
                }
                for (String pkg : pkgs) {
                    Integer obj = sMatcher.get(pkg);
                    if (obj != null) {
                        return obj.intValue();
                    }
                }
                return 0;
            } catch (RemoteException e) {
                return 0;
            } catch (Throwable th) {
                return 0;
            }
        }
    }

    private static native boolean nativeCompress(long j, int i, int i2, OutputStream outputStream, byte[] bArr);

    private static native boolean nativeCompressYuvToJpeg(long j, int i, int i2, OutputStream outputStream, byte[] bArr);

    private static native int nativeConfig(long j);

    private static native Bitmap nativeCopy(long j, int i, boolean z);

    private static native Bitmap nativeCopyAshmem(long j);

    private static native Bitmap nativeCopyAshmemConfig(long j, int i);

    private static native void nativeCopyColorSpace(long j, long j2);

    private static native void nativeCopyPixelsFromBuffer(long j, Buffer buffer);

    private static native void nativeCopyPixelsToBuffer(long j, Buffer buffer);

    private static native Bitmap nativeCopyPreserveInternalConfig(long j);

    private static native Bitmap nativeCreate(int[] iArr, int i, int i2, int i3, int i4, int i5, boolean z, float[] fArr, ColorSpace.Rgb.TransferParameters transferParameters);

    /* access modifiers changed from: private */
    public static native Bitmap nativeCreateFromParcel(Parcel parcel);

    private static native GraphicBuffer nativeCreateGraphicBufferHandle(long j);

    private static native Bitmap nativeCreateHardwareBitmap(GraphicBuffer graphicBuffer);

    private static native void nativeErase(long j, int i);

    private static native Bitmap nativeExtractAlpha(long j, long j2, int[] iArr);

    private static native int nativeGenerationId(long j);

    private static native int nativeGetAllocationByteCount(long j);

    private static native boolean nativeGetColorSpace(long j, float[] fArr, float[] fArr2);

    private static native long nativeGetNativeFinalizer();

    private static native int nativeGetPixel(long j, int i, int i2);

    private static native void nativeGetPixels(long j, int[] iArr, int i, int i2, int i3, int i4, int i5, int i6);

    private static native boolean nativeHasAlpha(long j);

    private static native boolean nativeHasMipMap(long j);

    private static native boolean nativeIsPremultiplied(long j);

    private static native boolean nativeIsSRGB(long j);

    private static native boolean nativeIsSRGBLinear(long j);

    private static native void nativePrepareToDraw(long j);

    private static native void nativeReconfigure(long j, int i, int i2, int i3, boolean z);

    private static native boolean nativeRecycle(long j);

    private static native int nativeRowBytes(long j);

    private static native boolean nativeSameAs(long j, long j2);

    private static native void nativeSetHasAlpha(long j, boolean z, boolean z2);

    private static native void nativeSetHasMipMap(long j, boolean z);

    private static native void nativeSetPixel(long j, int i, int i2, int i3);

    private static native void nativeSetPixels(long j, int[] iArr, int i, int i2, int i3, int i4, int i5, int i6);

    private static native void nativeSetPremultiplied(long j, boolean z);

    private static native boolean nativeWriteToParcel(long j, boolean z, int i, Parcel parcel);

    public static void setDefaultDensity(int density) {
        sDefaultDensity = density;
    }

    static int getDefaultDensity() {
        if (sDefaultDensity >= 0) {
            Application a = ActivityThread.currentApplication();
            if (!(a == null || a.mLoadedApk == null || a.mLoadedApk.getResources().getCompatibilityInfo().supportsScreen())) {
                sDefaultDensity = a.mLoadedApk.getResources().getDisplayMetrics().densityDpi;
            }
            if (!(!HwPCUtils.enabled() || a == null || a.getResources() == null || a.getResources().getDisplayMetrics() == null)) {
                sDefaultDensity = a.getResources().getDisplayMetrics().densityDpi;
            }
            return sDefaultDensity;
        }
        sDefaultDensity = DisplayMetrics.DENSITY_DEVICE;
        return sDefaultDensity;
    }

    Bitmap(long nativeBitmap, int width, int height, int density, boolean isMutable, boolean requestPremultiplied, byte[] ninePatchChunk, NinePatch.InsetStruct ninePatchInsets) {
        long j = nativeBitmap;
        int i = density;
        if (j != 0) {
            this.mWidth = width;
            this.mHeight = height;
            this.mIsMutable = isMutable;
            this.mRequestPremultiplied = requestPremultiplied;
            this.mNinePatchChunk = ninePatchChunk;
            this.mNinePatchInsets = ninePatchInsets;
            if (i >= 0) {
                this.mDensity = i;
            } else {
                Application a = ActivityThread.currentApplication();
                if (!(a == null || a.mLoadedApk == null)) {
                    DisplayMetrics metrics = a.mLoadedApk.getResources().getDisplayMetrics();
                    if (Float.compare(metrics.density, metrics.noncompatDensity) != 0) {
                        this.mDensity = metrics.densityDpi;
                    }
                }
            }
            this.mNativePtr = j;
            long nativeSize = 32 + ((long) getAllocationByteCount());
            NativeAllocationRegistry nativeAllocationRegistry = new NativeAllocationRegistry(Bitmap.class.getClassLoader(), nativeGetNativeFinalizer(), nativeSize);
            nativeAllocationRegistry.registerNativeAllocation(this, j);
            if (ResourcesImpl.TRACE_FOR_DETAILED_PRELOAD) {
                sPreloadTracingNumInstantiatedBitmaps++;
                sPreloadTracingTotalBitmapsSize += nativeSize;
                return;
            }
            return;
        }
        int i2 = width;
        int i3 = height;
        boolean z = isMutable;
        boolean z2 = requestPremultiplied;
        byte[] bArr = ninePatchChunk;
        NinePatch.InsetStruct insetStruct = ninePatchInsets;
        throw new RuntimeException("internal error: native bitmap is 0");
    }

    public long getNativeInstance() {
        return this.mNativePtr;
    }

    /* access modifiers changed from: package-private */
    public void reinit(int width, int height, boolean requestPremultiplied) {
        this.mWidth = width;
        this.mHeight = height;
        this.mRequestPremultiplied = requestPremultiplied;
        this.mColorSpace = null;
    }

    public int getDensity() {
        if (this.mRecycled) {
            Log.w(TAG, "Called getDensity() on a recycle()'d bitmap! This is undefined behavior!");
        }
        return this.mDensity;
    }

    public void setDensity(int density) {
        this.mDensity = density;
    }

    public void reconfigure(int width, int height, Config config) {
        checkRecycled("Can't call reconfigure() on a recycled bitmap");
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("width and height must be > 0");
        } else if (isMutable()) {
            nativeReconfigure(this.mNativePtr, width, height, config.nativeInt, this.mRequestPremultiplied);
            this.mWidth = width;
            this.mHeight = height;
            this.mColorSpace = null;
        } else {
            throw new IllegalStateException("only mutable bitmaps may be reconfigured");
        }
    }

    public void setWidth(int width) {
        reconfigure(width, getHeight(), getConfig());
    }

    public void setHeight(int height) {
        reconfigure(getWidth(), height, getConfig());
    }

    public void setConfig(Config config) {
        reconfigure(getWidth(), getHeight(), config);
    }

    public void setNinePatchChunk(byte[] chunk) {
        this.mNinePatchChunk = chunk;
    }

    public void incReference() {
        this.mReferenceCount++;
    }

    public void recycle() {
        if (this.mReferenceCount > 0) {
            this.mReferenceCount--;
            return;
        }
        if (!this.mRecycled && this.mNativePtr != 0) {
            if (nativeRecycle(this.mNativePtr)) {
                this.mNinePatchChunk = null;
            }
            if (HwGalleryCacheManager.isGalleryCacheEffect() && this.mCacheInfo != null) {
                this.mCacheInfo.recycle();
            }
            this.mRecycled = true;
        }
    }

    public final boolean isRecycled() {
        return this.mRecycled;
    }

    public int getGenerationId() {
        if (this.mRecycled) {
            Log.w(TAG, "Called getGenerationId() on a recycle()'d bitmap! This is undefined behavior!");
        }
        return nativeGenerationId(this.mNativePtr);
    }

    private void checkRecycled(String errorMessage) {
        if (this.mRecycled) {
            throw new IllegalStateException(errorMessage);
        }
    }

    private void checkHardware(String errorMessage) {
        if (getConfig() == Config.HARDWARE) {
            throw new IllegalStateException(errorMessage);
        }
    }

    private static void checkXYSign(int x, int y) {
        if (x < 0) {
            throw new IllegalArgumentException("x must be >= 0");
        } else if (y < 0) {
            throw new IllegalArgumentException("y must be >= 0");
        }
    }

    private static void checkWidthHeight(int width, int height) {
        if (width <= 0) {
            throw new IllegalArgumentException("width must be > 0");
        } else if (height <= 0) {
            throw new IllegalArgumentException("height must be > 0");
        }
    }

    public void copyPixelsToBuffer(Buffer dst) {
        int shift;
        checkHardware("unable to copyPixelsToBuffer, pixel access is not supported on Config#HARDWARE bitmaps");
        int elements = dst.remaining();
        if (dst instanceof ByteBuffer) {
            shift = 0;
        } else if ((dst instanceof ShortBuffer) != 0) {
            shift = 1;
        } else if (dst instanceof IntBuffer) {
            shift = 2;
        } else {
            throw new RuntimeException("unsupported Buffer subclass");
        }
        long pixelSize = (long) getByteCount();
        if ((((long) elements) << shift) >= pixelSize) {
            nativeCopyPixelsToBuffer(this.mNativePtr, dst);
            dst.position((int) (((long) dst.position()) + (pixelSize >> shift)));
            return;
        }
        throw new RuntimeException("Buffer not large enough for pixels");
    }

    public void copyPixelsFromBuffer(Buffer src) {
        int shift;
        checkRecycled("copyPixelsFromBuffer called on recycled bitmap");
        checkHardware("unable to copyPixelsFromBuffer, Config#HARDWARE bitmaps are immutable");
        int elements = src.remaining();
        if (src instanceof ByteBuffer) {
            shift = 0;
        } else if ((src instanceof ShortBuffer) != 0) {
            shift = 1;
        } else if (src instanceof IntBuffer) {
            shift = 2;
        } else {
            throw new RuntimeException("unsupported Buffer subclass");
        }
        long bitmapBytes = (long) getByteCount();
        if ((((long) elements) << shift) >= bitmapBytes) {
            nativeCopyPixelsFromBuffer(this.mNativePtr, src);
            src.position((int) (((long) src.position()) + (bitmapBytes >> shift)));
            return;
        }
        throw new RuntimeException("Buffer not large enough for pixels");
    }

    private void noteHardwareBitmapSlowCall() {
        if (getConfig() == Config.HARDWARE) {
            StrictMode.noteSlowCall("Warning: attempt to read pixels from hardware bitmap, which is very slow operation");
        }
    }

    public Bitmap copy(Config config, boolean isMutable) {
        checkRecycled("Can't copy a recycled bitmap");
        if (config != Config.HARDWARE || !isMutable) {
            noteHardwareBitmapSlowCall();
            Bitmap b = nativeCopy(this.mNativePtr, config.nativeInt, isMutable);
            if (b != null) {
                b.setPremultiplied(this.mRequestPremultiplied);
                b.mDensity = this.mDensity;
            }
            return b;
        }
        throw new IllegalArgumentException("Hardware bitmaps are always immutable");
    }

    public Bitmap createAshmemBitmap() {
        checkRecycled("Can't copy a recycled bitmap");
        noteHardwareBitmapSlowCall();
        Bitmap b = nativeCopyAshmem(this.mNativePtr);
        if (b != null) {
            b.setPremultiplied(this.mRequestPremultiplied);
            b.mDensity = this.mDensity;
        }
        return b;
    }

    public Bitmap createAshmemBitmap(Config config) {
        checkRecycled("Can't copy a recycled bitmap");
        noteHardwareBitmapSlowCall();
        Bitmap b = nativeCopyAshmemConfig(this.mNativePtr, config.nativeInt);
        if (b != null) {
            b.setPremultiplied(this.mRequestPremultiplied);
            b.mDensity = this.mDensity;
        }
        return b;
    }

    public static Bitmap createHardwareBitmap(GraphicBuffer graphicBuffer) {
        return nativeCreateHardwareBitmap(graphicBuffer);
    }

    public static Bitmap createScaledBitmap(Bitmap src, int dstWidth, int dstHeight, boolean filter) {
        Matrix m = new Matrix();
        int width = src.getWidth();
        int height = src.getHeight();
        if (!(width == dstWidth && height == dstHeight)) {
            m.setScale(((float) dstWidth) / ((float) width), ((float) dstHeight) / ((float) height));
        }
        return createBitmap(src, 0, 0, width, height, m, filter);
    }

    public static Bitmap createBitmap(Bitmap src) {
        return createBitmap(src, 0, 0, src.getWidth(), src.getHeight());
    }

    public static Bitmap createBitmap(Bitmap source, int x, int y, int width, int height) {
        return createBitmap(source, x, y, width, height, (Matrix) null, false);
    }

    public static Bitmap createBitmap(Bitmap source, int x, int y, int width, int height, Matrix m, boolean filter) {
        Paint paint;
        Bitmap bitmap;
        Bitmap bitmap2;
        Bitmap source2 = source;
        int i = x;
        int i2 = y;
        int i3 = width;
        int i4 = height;
        Matrix matrix = m;
        boolean z = filter;
        checkXYSign(x, y);
        checkWidthHeight(width, height);
        if (i + i3 > source.getWidth()) {
            throw new IllegalArgumentException("x + width must be <= bitmap.width()");
        } else if (i2 + i4 > source.getHeight()) {
            throw new IllegalArgumentException("y + height must be <= bitmap.height()");
        } else if (!source.isMutable() && i == 0 && i2 == 0 && i3 == source.getWidth() && i4 == source.getHeight() && (matrix == null || m.isIdentity())) {
            return source2;
        } else {
            boolean isHardware = source.getConfig() == Config.HARDWARE;
            if (isHardware) {
                source.noteHardwareBitmapSlowCall();
                source2 = nativeCopyPreserveInternalConfig(source2.mNativePtr);
            }
            int neww = i3;
            int newh = i4;
            Rect srcR = new Rect(i, i2, i + i3, i2 + i4);
            RectF dstR = new RectF(0.0f, 0.0f, (float) i3, (float) i4);
            RectF deviceR = new RectF();
            Config newConfig = Config.ARGB_8888;
            Config config = source2.getConfig();
            if (config != null) {
                switch (config) {
                    case RGB_565:
                        newConfig = Config.RGB_565;
                        break;
                    case ALPHA_8:
                        newConfig = Config.ALPHA_8;
                        break;
                    case RGBA_F16:
                        newConfig = Config.RGBA_F16;
                        break;
                    default:
                        newConfig = Config.ARGB_8888;
                        break;
                }
            }
            if (matrix == null || m.isIdentity()) {
                bitmap2 = null;
                bitmap = createBitmap(neww, newh, newConfig, source2.hasAlpha());
                paint = null;
            } else {
                boolean transformed = !m.rectStaysRect();
                matrix.mapRect(deviceR, dstR);
                int neww2 = Math.round(deviceR.width());
                int newh2 = Math.round(deviceR.height());
                Config transformedConfig = newConfig;
                if (!(!transformed || transformedConfig == Config.ARGB_8888 || transformedConfig == Config.RGBA_F16)) {
                    transformedConfig = Config.ARGB_8888;
                }
                if (neww2 <= 0 || newh2 <= 0) {
                    StringBuilder sb = new StringBuilder();
                    int i5 = neww2;
                    sb.append("width and height must be > 0, width=");
                    sb.append(i3);
                    sb.append(", height=");
                    sb.append(i4);
                    Log.e(TAG, sb.toString());
                    return null;
                }
                Bitmap bitmap3 = createBitmap(neww2, newh2, transformedConfig, transformed || source2.hasAlpha());
                if (bitmap3 == null) {
                    Bitmap bitmap4 = bitmap3;
                    Log.e(TAG, "createBitmap error!");
                    return null;
                }
                Bitmap bitmap5 = bitmap3;
                Paint paint2 = new Paint();
                paint2.setFilterBitmap(z);
                if (transformed) {
                    paint2.setAntiAlias(true);
                }
                paint = paint2;
                bitmap = bitmap5;
                bitmap2 = null;
            }
            if (bitmap == null) {
                return bitmap2;
            }
            Config config2 = newConfig;
            Config config3 = config;
            nativeCopyColorSpace(source2.mNativePtr, bitmap.mNativePtr);
            bitmap.mDensity = source2.mDensity;
            bitmap.setHasAlpha(source2.hasAlpha());
            bitmap.setPremultiplied(source2.mRequestPremultiplied);
            Canvas canvas = new Canvas(bitmap);
            canvas.translate(-deviceR.left, -deviceR.top);
            canvas.concat(matrix);
            canvas.drawBitmap(source2, srcR, dstR, paint);
            canvas.setBitmap(null);
            if (HwGalleryCacheManager.isGalleryCacheEffect() && source2.mGalleryCached && source2.mCacheInfo != null) {
                if (bitmap != source2) {
                    bitmap.mGalleryCached = source2.mGalleryCached;
                    bitmap.mCacheInfo = source2.mCacheInfo;
                    bitmap.mCacheInfo.addRef();
                }
                if (matrix != null && !m.isIdentity()) {
                    if (bitmap.mCacheInfo.mMatrix == null) {
                        Matrix unused = bitmap.mCacheInfo.mMatrix = new Matrix(matrix);
                    } else {
                        bitmap.mCacheInfo.mMatrix.postConcat(matrix);
                    }
                    boolean unused2 = bitmap.mCacheInfo.mFilter = z;
                }
            }
            if (isHardware) {
                return bitmap.copy(Config.HARDWARE, false);
            }
            return bitmap;
        }
    }

    public static Bitmap createBitmap(int width, int height, Config config) {
        return createBitmap(width, height, config, true);
    }

    public static Bitmap createBitmap(DisplayMetrics display, int width, int height, Config config) {
        return createBitmap(display, width, height, config, true);
    }

    public static Bitmap createBitmap(int width, int height, Config config, boolean hasAlpha) {
        return createBitmap((DisplayMetrics) null, width, height, config, hasAlpha);
    }

    public static Bitmap createBitmap(int width, int height, Config config, boolean hasAlpha, ColorSpace colorSpace) {
        return createBitmap((DisplayMetrics) null, width, height, config, hasAlpha, colorSpace);
    }

    public static Bitmap createBitmap(DisplayMetrics display, int width, int height, Config config, boolean hasAlpha) {
        return createBitmap(display, width, height, config, hasAlpha, ColorSpace.get(ColorSpace.Named.SRGB));
    }

    public static Bitmap createBitmap(DisplayMetrics display, int width, int height, Config config, boolean hasAlpha, ColorSpace colorSpace) {
        Bitmap bm;
        DisplayMetrics displayMetrics = display;
        Config config2 = config;
        boolean z = hasAlpha;
        ColorSpace colorSpace2 = colorSpace;
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("width and height must be > 0");
        } else if (config2 == Config.HARDWARE) {
            throw new IllegalArgumentException("can't create mutable bitmap with Config.HARDWARE");
        } else if (colorSpace2 != null) {
            if (config2 != Config.ARGB_8888 || colorSpace2 == ColorSpace.get(ColorSpace.Named.SRGB)) {
                bm = nativeCreate(null, 0, width, width, height, config2.nativeInt, true, null, null);
            } else if (colorSpace2 instanceof ColorSpace.Rgb) {
                ColorSpace.Rgb rgb = (ColorSpace.Rgb) colorSpace2;
                ColorSpace.Rgb.TransferParameters parameters = rgb.getTransferParameters();
                if (parameters != null) {
                    ColorSpace.Rgb d50 = (ColorSpace.Rgb) ColorSpace.adapt(rgb, ColorSpace.ILLUMINANT_D50);
                    ColorSpace.Rgb rgb2 = d50;
                    bm = nativeCreate(null, 0, width, width, height, config2.nativeInt, true, d50.getTransform(), parameters);
                } else {
                    throw new IllegalArgumentException("colorSpace must use an ICC parametric transfer function");
                }
            } else {
                throw new IllegalArgumentException("colorSpace must be an RGB color space");
            }
            if (displayMetrics != null) {
                bm.mDensity = displayMetrics.densityDpi;
            }
            bm.setHasAlpha(z);
            if ((config2 == Config.ARGB_8888 || config2 == Config.RGBA_F16) && !z) {
                nativeErase(bm.mNativePtr, -16777216);
            }
            return bm;
        } else {
            throw new IllegalArgumentException("can't create bitmap without a color space");
        }
    }

    public static Bitmap createBitmap(int[] colors, int offset, int stride, int width, int height, Config config) {
        return createBitmap((DisplayMetrics) null, colors, offset, stride, width, height, config);
    }

    public static Bitmap createBitmap(DisplayMetrics display, int[] colors, int offset, int stride, int width, int height, Config config) {
        DisplayMetrics displayMetrics = display;
        int i = width;
        checkWidthHeight(width, height);
        if (Math.abs(stride) >= i) {
            int lastScanline = offset + ((height - 1) * stride);
            int[] iArr = colors;
            int length = iArr.length;
            if (offset < 0 || offset + i > length || lastScanline < 0 || lastScanline + i > length) {
                throw new ArrayIndexOutOfBoundsException();
            } else if (i <= 0 || height <= 0) {
                throw new IllegalArgumentException("width and height must be > 0");
            } else {
                int i2 = length;
                Bitmap bm = nativeCreate(iArr, offset, stride, i, height, config.nativeInt, false, null, null);
                if (displayMetrics != null) {
                    bm.mDensity = displayMetrics.densityDpi;
                }
                return bm;
            }
        } else {
            int[] iArr2 = colors;
            throw new IllegalArgumentException("abs(stride) must be >= width");
        }
    }

    public static Bitmap createBitmap(int[] colors, int width, int height, Config config) {
        return createBitmap((DisplayMetrics) null, colors, 0, width, width, height, config);
    }

    public static Bitmap createBitmap(DisplayMetrics display, int[] colors, int width, int height, Config config) {
        return createBitmap(display, colors, 0, width, width, height, config);
    }

    public static Bitmap createBitmap(Picture source) {
        return createBitmap(source, source.getWidth(), source.getHeight(), Config.HARDWARE);
    }

    public static Bitmap createBitmap(Picture source, int width, int height, Config config) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("width & height must be > 0");
        } else if (config != null) {
            source.endRecording();
            if (source.requiresHardwareAcceleration() && config != Config.HARDWARE) {
                StrictMode.noteSlowCall("GPU readback");
            }
            if (config == Config.HARDWARE || source.requiresHardwareAcceleration()) {
                RenderNode node = RenderNode.create("BitmapTemporary", null);
                node.setLeftTopRightBottom(0, 0, width, height);
                node.setClipToBounds(false);
                DisplayListCanvas canvas = node.start(width, height);
                if (!(source.getWidth() == width && source.getHeight() == height)) {
                    canvas.scale(((float) width) / ((float) source.getWidth()), ((float) height) / ((float) source.getHeight()));
                }
                canvas.drawPicture(source);
                node.end(canvas);
                Bitmap bitmap = ThreadedRenderer.createHardwareBitmap(node, width, height);
                if (config != Config.HARDWARE) {
                    bitmap = bitmap.copy(config, false);
                }
                return bitmap;
            }
            Bitmap bitmap2 = createBitmap(width, height, config);
            Canvas canvas2 = new Canvas(bitmap2);
            if (!(source.getWidth() == width && source.getHeight() == height)) {
                canvas2.scale(((float) width) / ((float) source.getWidth()), ((float) height) / ((float) source.getHeight()));
            }
            canvas2.drawPicture(source);
            canvas2.setBitmap(null);
            bitmap2.makeImmutable();
            return bitmap2;
        } else {
            throw new IllegalArgumentException("Config must not be null");
        }
    }

    public byte[] getNinePatchChunk() {
        return this.mNinePatchChunk;
    }

    public void getOpticalInsets(Rect outInsets) {
        if (this.mNinePatchInsets == null) {
            outInsets.setEmpty();
        } else {
            outInsets.set(this.mNinePatchInsets.opticalRect);
        }
    }

    public NinePatch.InsetStruct getNinePatchInsets() {
        return this.mNinePatchInsets;
    }

    public boolean compress(CompressFormat format, int quality, OutputStream stream) {
        checkRecycled("Can't compress a recycled bitmap");
        if (stream == null) {
            throw new NullPointerException();
        } else if (HwGalleryCacheManager.isGalleryCacheEffect() && this.mGalleryCached) {
            Log.d(TAG, "This Bitmap is already cached by Gallery3D!");
            return false;
        } else if (quality < 0 || quality > 100) {
            throw new IllegalArgumentException("quality must be 0..100");
        } else {
            StrictMode.noteSlowCall("Compression of a bitmap is slow");
            Trace.traceBegin(8192, "Bitmap.compress");
            if (UidMatcher.flag == 2 && quality <= 70) {
                quality = 85;
            }
            boolean result = nativeCompress(this.mNativePtr, format.nativeInt, quality, stream, new byte[4096]);
            Trace.traceEnd(8192);
            return result;
        }
    }

    public boolean compressYuvToJpeg(CompressFormat format, int quality, OutputStream stream) {
        return nativeCompressYuvToJpeg(this.mNativePtr, format.nativeInt, quality, stream, new byte[4096]);
    }

    public final boolean isMutable() {
        return this.mIsMutable;
    }

    public final void makeImmutable() {
    }

    public final boolean isPremultiplied() {
        if (this.mRecycled) {
            Log.w(TAG, "Called isPremultiplied() on a recycle()'d bitmap! This is undefined behavior!");
        }
        return nativeIsPremultiplied(this.mNativePtr);
    }

    public final void setPremultiplied(boolean premultiplied) {
        checkRecycled("setPremultiplied called on a recycled bitmap");
        this.mRequestPremultiplied = premultiplied;
        nativeSetPremultiplied(this.mNativePtr, premultiplied);
    }

    public final int getWidth() {
        if (this.mRecycled) {
            Log.w(TAG, "Called getWidth() on a recycle()'d bitmap! This is undefined behavior!");
        }
        return this.mWidth;
    }

    public final int getHeight() {
        if (this.mRecycled) {
            Log.w(TAG, "Called getHeight() on a recycle()'d bitmap! This is undefined behavior!");
        }
        return this.mHeight;
    }

    public int getScaledWidth(Canvas canvas) {
        return scaleFromDensity(getWidth(), this.mDensity, canvas.mDensity);
    }

    public int getScaledHeight(Canvas canvas) {
        return scaleFromDensity(getHeight(), this.mDensity, canvas.mDensity);
    }

    public int getScaledWidth(DisplayMetrics metrics) {
        return scaleFromDensity(getWidth(), this.mDensity, metrics.densityDpi);
    }

    public int getScaledHeight(DisplayMetrics metrics) {
        return scaleFromDensity(getHeight(), this.mDensity, metrics.densityDpi);
    }

    public int getScaledWidth(int targetDensity) {
        return scaleFromDensity(getWidth(), this.mDensity, targetDensity);
    }

    public int getScaledHeight(int targetDensity) {
        return scaleFromDensity(getHeight(), this.mDensity, targetDensity);
    }

    public static int scaleFromDensity(int size, int sdensity, int tdensity) {
        if (sdensity == 0 || tdensity == 0 || sdensity == tdensity) {
            return size;
        }
        return ((size * tdensity) + (sdensity >> 1)) / sdensity;
    }

    public final int getRowBytes() {
        if (this.mRecycled) {
            Log.w(TAG, "Called getRowBytes() on a recycle()'d bitmap! This is undefined behavior!");
        }
        return nativeRowBytes(this.mNativePtr);
    }

    public final int getByteCount() {
        if (!this.mRecycled) {
            return getRowBytes() * getHeight();
        }
        Log.w(TAG, "Called getByteCount() on a recycle()'d bitmap! This is undefined behavior!");
        return 0;
    }

    public final int getAllocationByteCount() {
        if (!this.mRecycled) {
            return nativeGetAllocationByteCount(this.mNativePtr);
        }
        Log.w(TAG, "Called getAllocationByteCount() on a recycle()'d bitmap! This is undefined behavior!");
        return 0;
    }

    public final Config getConfig() {
        if (this.mRecycled) {
            Log.w(TAG, "Called getConfig() on a recycle()'d bitmap! This is undefined behavior!");
        }
        return Config.nativeToConfig(nativeConfig(this.mNativePtr));
    }

    public final boolean hasAlpha() {
        if (this.mRecycled) {
            Log.w(TAG, "Called hasAlpha() on a recycle()'d bitmap! This is undefined behavior!");
        }
        return nativeHasAlpha(this.mNativePtr);
    }

    public void setHasAlpha(boolean hasAlpha) {
        checkRecycled("setHasAlpha called on a recycled bitmap");
        nativeSetHasAlpha(this.mNativePtr, hasAlpha, this.mRequestPremultiplied);
    }

    public final boolean hasMipMap() {
        if (this.mRecycled) {
            Log.w(TAG, "Called hasMipMap() on a recycle()'d bitmap! This is undefined behavior!");
        }
        return nativeHasMipMap(this.mNativePtr);
    }

    public final void setHasMipMap(boolean hasMipMap) {
        checkRecycled("setHasMipMap called on a recycled bitmap");
        nativeSetHasMipMap(this.mNativePtr, hasMipMap);
    }

    public final ColorSpace getColorSpace() {
        Bitmap bitmap;
        if (getConfig() == Config.RGBA_F16) {
            this.mColorSpace = null;
            return ColorSpace.get(ColorSpace.Named.LINEAR_EXTENDED_SRGB);
        }
        if (this.mColorSpace == null) {
            if (nativeIsSRGB(this.mNativePtr)) {
                this.mColorSpace = ColorSpace.get(ColorSpace.Named.SRGB);
            } else if (getConfig() != Config.HARDWARE || !nativeIsSRGBLinear(this.mNativePtr)) {
                float[] xyz = new float[9];
                float[] params = new float[7];
                boolean hasColorSpace = nativeGetColorSpace(this.mNativePtr, xyz, params);
                if (hasColorSpace) {
                    float[] fArr = params;
                    boolean z = hasColorSpace;
                    ColorSpace.Rgb.TransferParameters transferParameters = new ColorSpace.Rgb.TransferParameters((double) params[0], (double) params[1], (double) params[2], (double) params[3], (double) params[4], (double) params[5], (double) params[6]);
                    ColorSpace.Rgb.TransferParameters parameters = transferParameters;
                    float[] xyz2 = xyz;
                    ColorSpace cs = ColorSpace.match(xyz2, parameters);
                    if (cs != null) {
                        bitmap = this;
                        bitmap.mColorSpace = cs;
                    } else {
                        bitmap = this;
                        bitmap.mColorSpace = new ColorSpace.Rgb("Unknown", xyz2, parameters);
                    }
                    return bitmap.mColorSpace;
                }
            } else {
                this.mColorSpace = ColorSpace.get(ColorSpace.Named.LINEAR_EXTENDED_SRGB);
            }
        }
        bitmap = this;
        return bitmap.mColorSpace;
    }

    public void eraseColor(int c) {
        checkRecycled("Can't erase a recycled bitmap");
        if (isMutable()) {
            nativeErase(this.mNativePtr, c);
            return;
        }
        throw new IllegalStateException("cannot erase immutable bitmaps");
    }

    public int getPixel(int x, int y) {
        checkRecycled("Can't call getPixel() on a recycled bitmap");
        checkHardware("unable to getPixel(), pixel access is not supported on Config#HARDWARE bitmaps");
        checkPixelAccess(x, y);
        return nativeGetPixel(this.mNativePtr, x, y);
    }

    public void getPixels(int[] pixels, int offset, int stride, int x, int y, int width, int height) {
        checkRecycled("Can't call getPixels() on a recycled bitmap");
        checkHardware("unable to getPixels(), pixel access is not supported on Config#HARDWARE bitmaps");
        if (width != 0 && height != 0) {
            checkPixelsAccess(x, y, width, height, offset, stride, pixels);
            nativeGetPixels(this.mNativePtr, pixels, offset, stride, x, y, width, height);
        }
    }

    public void getYuvData(int[] pixels, int offset, int stride, int x, int y, int width, int height) {
        checkRecycled("HEIFCONVERT:Can't call getPixels() on a recycled bitmap");
        checkHardware("HEIFCONVERT:unable to getPixels(), pixel access is not supported on Config#HARDWARE bitmaps");
        if (width != 0 && height != 0) {
            nativeGetPixels(this.mNativePtr, pixels, offset, stride, x, y, width, height);
        }
    }

    private void checkPixelAccess(int x, int y) {
        checkXYSign(x, y);
        if (x >= getWidth()) {
            throw new IllegalArgumentException("x must be < bitmap.width()");
        } else if (y >= getHeight()) {
            throw new IllegalArgumentException("y must be < bitmap.height()");
        }
    }

    private void checkPixelsAccess(int x, int y, int width, int height, int offset, int stride, int[] pixels) {
        checkXYSign(x, y);
        if (width < 0) {
            throw new IllegalArgumentException("width must be >= 0");
        } else if (height < 0) {
            throw new IllegalArgumentException("height must be >= 0");
        } else if (x + width > getWidth()) {
            throw new IllegalArgumentException("x + width must be <= bitmap.width()");
        } else if (y + height > getHeight()) {
            throw new IllegalArgumentException("y + height must be <= bitmap.height()");
        } else if (Math.abs(stride) >= width) {
            int lastScanline = ((height - 1) * stride) + offset;
            int length = pixels.length;
            if (offset < 0 || offset + width > length || lastScanline < 0 || lastScanline + width > length) {
                throw new ArrayIndexOutOfBoundsException();
            }
        } else {
            throw new IllegalArgumentException("abs(stride) must be >= width");
        }
    }

    public void setPixel(int x, int y, int color) {
        checkRecycled("Can't call setPixel() on a recycled bitmap");
        if (isMutable()) {
            checkPixelAccess(x, y);
            nativeSetPixel(this.mNativePtr, x, y, color);
            return;
        }
        throw new IllegalStateException();
    }

    public void setPixels(int[] pixels, int offset, int stride, int x, int y, int width, int height) {
        checkRecycled("Can't call setPixels() on a recycled bitmap");
        if (!isMutable()) {
            throw new IllegalStateException();
        } else if (width != 0 && height != 0) {
            checkPixelsAccess(x, y, width, height, offset, stride, pixels);
            nativeSetPixels(this.mNativePtr, pixels, offset, stride, x, y, width, height);
        }
    }

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel p, int flags) {
        checkRecycled("Can't parcel a recycled bitmap");
        noteHardwareBitmapSlowCall();
        if (!nativeWriteToParcel(this.mNativePtr, this.mIsMutable, this.mDensity, p)) {
            throw new RuntimeException("native writeToParcel failed");
        }
    }

    public Bitmap extractAlpha() {
        return extractAlpha(null, null);
    }

    public Bitmap extractAlpha(Paint paint, int[] offsetXY) {
        checkRecycled("Can't extractAlpha on a recycled bitmap");
        long nativePaint = paint != null ? paint.getNativeInstance() : 0;
        noteHardwareBitmapSlowCall();
        Bitmap bm = nativeExtractAlpha(this.mNativePtr, nativePaint, offsetXY);
        if (bm != null) {
            bm.mDensity = this.mDensity;
            return bm;
        }
        throw new RuntimeException("Failed to extractAlpha on Bitmap");
    }

    public boolean sameAs(Bitmap other) {
        checkRecycled("Can't call sameAs on a recycled bitmap!");
        noteHardwareBitmapSlowCall();
        if (this == other) {
            return true;
        }
        if (other == null) {
            return false;
        }
        other.noteHardwareBitmapSlowCall();
        if (!other.isRecycled()) {
            return nativeSameAs(this.mNativePtr, other.mNativePtr);
        }
        throw new IllegalArgumentException("Can't compare to a recycled bitmap!");
    }

    public void prepareToDraw() {
        checkRecycled("Can't prepareToDraw on a recycled bitmap!");
        nativePrepareToDraw(this.mNativePtr);
    }

    public GraphicBuffer createGraphicBufferHandle() {
        return nativeCreateGraphicBufferHandle(this.mNativePtr);
    }
}
