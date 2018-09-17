package android.graphics;

import android.app.ActivityThread;
import android.app.Application;
import android.content.pm.IPackageManager;
import android.content.pm.IPackageManager.Stub;
import android.graphics.BitmapFactory.Options;
import android.graphics.ColorSpace.Named;
import android.graphics.ColorSpace.Rgb;
import android.graphics.ColorSpace.Rgb.TransferParameters;
import android.graphics.NinePatch.InsetStruct;
import android.hardware.camera2.params.TonemapCurve;
import android.hwgallerycache.HwGalleryCacheManager;
import android.os.Binder;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.StrictMode;
import android.os.Trace;
import android.util.DisplayMetrics;
import android.util.HwPCUtils;
import android.util.Log;
import java.io.OutputStream;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.HashMap;
import libcore.util.NativeAllocationRegistry;

public final class Bitmap implements Parcelable {
    private static final /* synthetic */ int[] -android-graphics-Bitmap$ConfigSwitchesValues = null;
    public static final Creator<Bitmap> CREATOR = new Creator<Bitmap>() {
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
    public GalleryCacheInfo mCacheInfo = null;
    private ColorSpace mColorSpace;
    public int mDensity = getDefaultDensity();
    public boolean mGalleryCached = false;
    private int mHeight;
    private final boolean mIsMutable;
    private final long mNativePtr;
    private byte[] mNinePatchChunk;
    private InsetStruct mNinePatchInsets;
    private boolean mRecycled;
    private boolean mRequestPremultiplied;
    private int mWidth;

    public enum CompressFormat {
        JPEG(0),
        PNG(1),
        WEBP(2);
        
        final int nativeInt;

        private CompressFormat(int nativeInt) {
            this.nativeInt = nativeInt;
        }
    }

    public enum Config {
        ALPHA_8(1),
        RGB_565(3),
        ARGB_4444(4),
        ARGB_8888(5),
        RGBA_F16(6),
        HARDWARE(7);
        
        private static Config[] sConfigs;
        final int nativeInt;

        static {
            sConfigs = new Config[]{null, ALPHA_8, null, RGB_565, ARGB_4444, ARGB_8888, RGBA_F16, HARDWARE};
        }

        private Config(int ni) {
            this.nativeInt = ni;
        }

        static Config nativeToConfig(int ni) {
            return sConfigs[ni];
        }
    }

    public static class GalleryCacheInfo {
        private boolean mFilter = false;
        private boolean mIsDecoding = false;
        private GalleryCacheInfo mLast = null;
        private Matrix mMatrix = null;
        private GalleryCacheInfo mNext = null;
        private Options mOptions = null;
        private String mPath = null;
        private boolean mRecycled = false;
        private long mRef = 1;
        private Bitmap mWechatThumb = null;

        public String getPath() {
            return this.mPath;
        }

        public Options getOptions() {
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

        public void setOptions(Options options) {
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
        private static final HashMap<String, Integer> sMatcher = new HashMap();

        UidMatcher() {
        }

        static {
            sMatcher.put("com.sina.weibo", Integer.valueOf(1));
            sMatcher.put("com.tencent.mm", Integer.valueOf(2));
        }

        private static int matchUid() {
            IPackageManager pm = Stub.asInterface(ServiceManager.getService("package"));
            if (pm == null) {
                return 0;
            }
            try {
                String[] pkgs = pm.getPackagesForUid(Binder.getCallingUid());
                if (pkgs == null) {
                    return 0;
                }
                for (String pkg : pkgs) {
                    Integer obj = (Integer) sMatcher.get(pkg);
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

    private static /* synthetic */ int[] -getandroid-graphics-Bitmap$ConfigSwitchesValues() {
        if (-android-graphics-Bitmap$ConfigSwitchesValues != null) {
            return -android-graphics-Bitmap$ConfigSwitchesValues;
        }
        int[] iArr = new int[Config.values().length];
        try {
            iArr[Config.ALPHA_8.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[Config.ARGB_4444.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[Config.ARGB_8888.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[Config.HARDWARE.ordinal()] = 6;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[Config.RGBA_F16.ordinal()] = 4;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[Config.RGB_565.ordinal()] = 5;
        } catch (NoSuchFieldError e6) {
        }
        -android-graphics-Bitmap$ConfigSwitchesValues = iArr;
        return iArr;
    }

    private static native boolean nativeCompress(long j, int i, int i2, OutputStream outputStream, byte[] bArr);

    private static native int nativeConfig(long j);

    private static native Bitmap nativeCopy(long j, int i, boolean z);

    private static native Bitmap nativeCopyAshmem(long j);

    private static native Bitmap nativeCopyAshmemConfig(long j, int i);

    private static native void nativeCopyColorSpace(long j, long j2);

    private static native void nativeCopyPixelsFromBuffer(long j, Buffer buffer);

    private static native void nativeCopyPixelsToBuffer(long j, Buffer buffer);

    private static native Bitmap nativeCopyPreserveInternalConfig(long j);

    private static native Bitmap nativeCreate(int[] iArr, int i, int i2, int i3, int i4, int i5, boolean z, float[] fArr, TransferParameters transferParameters);

    private static native Bitmap nativeCreateFromParcel(Parcel parcel);

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
            if (!(a == null || a.mLoadedApk == null || (a.mLoadedApk.getResources().getCompatibilityInfo().supportsScreen() ^ 1) == 0)) {
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

    Bitmap(long nativeBitmap, int width, int height, int density, boolean isMutable, boolean requestPremultiplied, byte[] ninePatchChunk, InsetStruct ninePatchInsets) {
        if (nativeBitmap == 0) {
            throw new RuntimeException("internal error: native bitmap is 0");
        }
        this.mWidth = width;
        this.mHeight = height;
        this.mIsMutable = isMutable;
        this.mRequestPremultiplied = requestPremultiplied;
        this.mNinePatchChunk = ninePatchChunk;
        this.mNinePatchInsets = ninePatchInsets;
        if (density >= 0) {
            this.mDensity = density;
        } else {
            Application a = ActivityThread.currentApplication();
            if (!(a == null || a.mLoadedApk == null)) {
                DisplayMetrics metrics = a.mLoadedApk.getResources().getDisplayMetrics();
                if (Float.compare(metrics.density, metrics.noncompatDensity) != 0) {
                    this.mDensity = metrics.densityDpi;
                }
            }
        }
        this.mNativePtr = nativeBitmap;
        new NativeAllocationRegistry(Bitmap.class.getClassLoader(), nativeGetNativeFinalizer(), 32 + ((long) getAllocationByteCount())).registerNativeAllocation(this, nativeBitmap);
    }

    public long getNativeInstance() {
        return this.mNativePtr;
    }

    void reinit(int width, int height, boolean requestPremultiplied) {
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

    public void recycle() {
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
        } else if (dst instanceof ShortBuffer) {
            shift = 1;
        } else if (dst instanceof IntBuffer) {
            shift = 2;
        } else {
            throw new RuntimeException("unsupported Buffer subclass");
        }
        long pixelSize = (long) getByteCount();
        if ((((long) elements) << shift) < pixelSize) {
            throw new RuntimeException("Buffer not large enough for pixels");
        }
        nativeCopyPixelsToBuffer(this.mNativePtr, dst);
        dst.position((int) (((long) dst.position()) + (pixelSize >> shift)));
    }

    public void copyPixelsFromBuffer(Buffer src) {
        int shift;
        checkRecycled("copyPixelsFromBuffer called on recycled bitmap");
        checkHardware("unable to copyPixelsFromBuffer, Config#HARDWARE bitmaps are immutable");
        int elements = src.remaining();
        if (src instanceof ByteBuffer) {
            shift = 0;
        } else if (src instanceof ShortBuffer) {
            shift = 1;
        } else if (src instanceof IntBuffer) {
            shift = 2;
        } else {
            throw new RuntimeException("unsupported Buffer subclass");
        }
        long bitmapBytes = (long) getByteCount();
        if ((((long) elements) << shift) < bitmapBytes) {
            throw new RuntimeException("Buffer not large enough for pixels");
        }
        nativeCopyPixelsFromBuffer(this.mNativePtr, src);
        src.position((int) (((long) src.position()) + (bitmapBytes >> shift)));
    }

    private void noteHardwareBitmapSlowCall() {
        if (getConfig() == Config.HARDWARE) {
            StrictMode.noteSlowCall("Warning: attempt to read pixels from hardware bitmap, which is very slow operation");
        }
    }

    public Bitmap copy(Config config, boolean isMutable) {
        checkRecycled("Can't copy a recycled bitmap");
        if (config == Config.HARDWARE && isMutable) {
            throw new IllegalArgumentException("Hardware bitmaps are always immutable");
        }
        noteHardwareBitmapSlowCall();
        Bitmap b = nativeCopy(this.mNativePtr, config.nativeInt, isMutable);
        if (b != null) {
            b.setPremultiplied(this.mRequestPremultiplied);
            b.mDensity = this.mDensity;
        }
        return b;
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
        return createBitmap(source, x, y, width, height, null, false);
    }

    public static Bitmap createBitmap(Bitmap source, int x, int y, int width, int height, Matrix m, boolean filter) {
        checkXYSign(x, y);
        checkWidthHeight(width, height);
        if (x + width > source.getWidth()) {
            throw new IllegalArgumentException("x + width must be <= bitmap.width()");
        }
        if (y + height > source.getHeight()) {
            throw new IllegalArgumentException("y + height must be <= bitmap.height()");
        } else if (!source.isMutable() && x == 0 && y == 0 && width == source.getWidth() && height == source.getHeight() && (m == null || m.isIdentity())) {
            return source;
        } else {
            Bitmap bitmap;
            Paint paint;
            boolean isHardware = source.getConfig() == Config.HARDWARE;
            if (isHardware) {
                source.noteHardwareBitmapSlowCall();
                source = nativeCopyPreserveInternalConfig(source.mNativePtr);
            }
            int neww = width;
            int newh = height;
            Rect srcR = new Rect(x, y, x + width, y + height);
            RectF dstR = new RectF(TonemapCurve.LEVEL_BLACK, TonemapCurve.LEVEL_BLACK, (float) width, (float) height);
            RectF deviceR = new RectF();
            Config newConfig = Config.ARGB_8888;
            Config config = source.getConfig();
            if (config != null) {
                switch (-getandroid-graphics-Bitmap$ConfigSwitchesValues()[config.ordinal()]) {
                    case 1:
                        newConfig = Config.ALPHA_8;
                        break;
                    case 4:
                        newConfig = Config.RGBA_F16;
                        break;
                    case 5:
                        newConfig = Config.RGB_565;
                        break;
                    default:
                        newConfig = Config.ARGB_8888;
                        break;
                }
            }
            if (m == null || m.isIdentity()) {
                bitmap = createBitmap(width, height, newConfig, source.hasAlpha());
                paint = null;
            } else {
                boolean transformed = m.rectStaysRect() ^ 1;
                m.mapRect(deviceR, dstR);
                neww = Math.round(deviceR.width());
                newh = Math.round(deviceR.height());
                Config transformedConfig = newConfig;
                if (!(!transformed || transformedConfig == Config.ARGB_8888 || transformedConfig == Config.RGBA_F16)) {
                    transformedConfig = Config.ARGB_8888;
                }
                if (neww <= 0 || newh <= 0) {
                    Log.e(TAG, "width and height must be > 0, width=" + width + ", height=" + height);
                    return null;
                }
                bitmap = createBitmap(neww, newh, transformedConfig, !transformed ? source.hasAlpha() : true);
                if (bitmap == null) {
                    Log.e(TAG, "createBitmap error!");
                    return null;
                }
                paint = new Paint();
                paint.setFilterBitmap(filter);
                if (transformed) {
                    paint.setAntiAlias(true);
                }
            }
            if (bitmap == null) {
                return null;
            }
            nativeCopyColorSpace(source.mNativePtr, bitmap.mNativePtr);
            bitmap.mDensity = source.mDensity;
            bitmap.setHasAlpha(source.hasAlpha());
            bitmap.setPremultiplied(source.mRequestPremultiplied);
            Canvas canvas = new Canvas(bitmap);
            canvas.translate(-deviceR.left, -deviceR.top);
            canvas.concat(m);
            canvas.drawBitmap(source, srcR, dstR, paint);
            canvas.setBitmap(null);
            if (HwGalleryCacheManager.isGalleryCacheEffect() && source.mGalleryCached && source.mCacheInfo != null) {
                if (bitmap != source) {
                    bitmap.mGalleryCached = source.mGalleryCached;
                    bitmap.mCacheInfo = source.mCacheInfo;
                    bitmap.mCacheInfo.addRef();
                }
                if (!(m == null || (m.isIdentity() ^ 1) == 0)) {
                    if (bitmap.mCacheInfo.mMatrix == null) {
                        bitmap.mCacheInfo.mMatrix = new Matrix(m);
                    } else {
                        bitmap.mCacheInfo.mMatrix.postConcat(m);
                    }
                    bitmap.mCacheInfo.mFilter = filter;
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
        return createBitmap(null, width, height, config, hasAlpha);
    }

    public static Bitmap createBitmap(int width, int height, Config config, boolean hasAlpha, ColorSpace colorSpace) {
        return createBitmap(null, width, height, config, hasAlpha, colorSpace);
    }

    public static Bitmap createBitmap(DisplayMetrics display, int width, int height, Config config, boolean hasAlpha) {
        return createBitmap(display, width, height, config, hasAlpha, ColorSpace.get(Named.SRGB));
    }

    public static Bitmap createBitmap(DisplayMetrics display, int width, int height, Config config, boolean hasAlpha, ColorSpace colorSpace) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("width and height must be > 0");
        } else if (config == Config.HARDWARE) {
            throw new IllegalArgumentException("can't create mutable bitmap with Config.HARDWARE");
        } else if (colorSpace == null) {
            throw new IllegalArgumentException("can't create bitmap without a color space");
        } else {
            Bitmap bm;
            if (config != Config.ARGB_8888 || colorSpace == ColorSpace.get(Named.SRGB)) {
                bm = nativeCreate(null, 0, width, width, height, config.nativeInt, true, null, null);
            } else if (colorSpace instanceof Rgb) {
                Rgb rgb = (Rgb) colorSpace;
                TransferParameters parameters = rgb.getTransferParameters();
                if (parameters == null) {
                    throw new IllegalArgumentException("colorSpace must use an ICC parametric transfer function");
                }
                int i = width;
                int i2 = width;
                int i3 = height;
                bm = nativeCreate(null, 0, i, i2, i3, config.nativeInt, true, ((Rgb) ColorSpace.adapt(rgb, ColorSpace.ILLUMINANT_D50)).getTransform(), parameters);
            } else {
                throw new IllegalArgumentException("colorSpace must be an RGB color space");
            }
            if (display != null) {
                bm.mDensity = display.densityDpi;
            }
            bm.setHasAlpha(hasAlpha);
            if ((config == Config.ARGB_8888 || config == Config.RGBA_F16) && (hasAlpha ^ 1) != 0) {
                nativeErase(bm.mNativePtr, -16777216);
            }
            return bm;
        }
    }

    public static Bitmap createBitmap(int[] colors, int offset, int stride, int width, int height, Config config) {
        return createBitmap(null, colors, offset, stride, width, height, config);
    }

    public static Bitmap createBitmap(DisplayMetrics display, int[] colors, int offset, int stride, int width, int height, Config config) {
        checkWidthHeight(width, height);
        if (Math.abs(stride) < width) {
            throw new IllegalArgumentException("abs(stride) must be >= width");
        }
        int lastScanline = offset + ((height - 1) * stride);
        int length = colors.length;
        if (offset < 0 || offset + width > length || lastScanline < 0 || lastScanline + width > length) {
            throw new ArrayIndexOutOfBoundsException();
        } else if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("width and height must be > 0");
        } else {
            Bitmap bm = nativeCreate(colors, offset, stride, width, height, config.nativeInt, false, null, null);
            if (display != null) {
                bm.mDensity = display.densityDpi;
            }
            return bm;
        }
    }

    public static Bitmap createBitmap(int[] colors, int width, int height, Config config) {
        return createBitmap(null, colors, 0, width, width, height, config);
    }

    public static Bitmap createBitmap(DisplayMetrics display, int[] colors, int width, int height, Config config) {
        return createBitmap(display, colors, 0, width, width, height, config);
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

    public InsetStruct getNinePatchInsets() {
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

    public final boolean isMutable() {
        return this.mIsMutable;
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
        if (getConfig() == Config.RGBA_F16) {
            this.mColorSpace = null;
            return ColorSpace.get(Named.LINEAR_EXTENDED_SRGB);
        }
        if (this.mColorSpace == null) {
            if (nativeIsSRGB(this.mNativePtr)) {
                this.mColorSpace = ColorSpace.get(Named.SRGB);
            } else {
                float[] xyz = new float[9];
                float[] params = new float[7];
                if (nativeGetColorSpace(this.mNativePtr, xyz, params)) {
                    TransferParameters parameters = new TransferParameters((double) params[0], (double) params[1], (double) params[2], (double) params[3], (double) params[4], (double) params[5], (double) params[6]);
                    ColorSpace cs = ColorSpace.match(xyz, parameters);
                    if (cs != null) {
                        this.mColorSpace = cs;
                    } else {
                        this.mColorSpace = new Rgb("Unknown", xyz, parameters);
                    }
                }
            }
        }
        return this.mColorSpace;
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
        } else if (Math.abs(stride) < width) {
            throw new IllegalArgumentException("abs(stride) must be >= width");
        } else {
            int lastScanline = offset + ((height - 1) * stride);
            int length = pixels.length;
            if (offset < 0 || offset + width > length || lastScanline < 0 || lastScanline + width > length) {
                throw new ArrayIndexOutOfBoundsException();
            }
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
        if (bm == null) {
            throw new RuntimeException("Failed to extractAlpha on Bitmap");
        }
        bm.mDensity = this.mDensity;
        return bm;
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
