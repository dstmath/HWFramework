package android.graphics;

import android.common.HwFrameworkFactory;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.ColorSpace;
import android.hwcontrol.HwWidgetFactory;
import android.hwgallerycache.HwGalleryCacheManager;
import android.hwtheme.HwThemeManager;
import android.os.Trace;
import android.util.Jlog;
import android.util.Log;
import android.util.TypedValue;
import android.widget.sr.SRBitmapManager;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class BitmapFactory {
    private static final int DECODE_BUFFER_SIZE = 16384;
    private static final String FB_THREAD_NAME = "Fg_ImgDecode";
    private static IAwareBitmapCacher mIAwareBitmapCacher;
    private static int mLastHeight = -1;
    private static InputStream mLastIS = null;
    private static int mLastWidth = -1;

    public static class Options {
        public static final int CODEC_ERROR = 2;
        public static final int CODEC_PARTIAL = 3;
        public static final int IO_ERROR = 1;
        public static final int SUCCESS = 0;
        public static final int UNSUPPORTED_TYPE = 4;
        public int errorCode = 0;
        public Bitmap inBitmap;
        public int inDensity;
        public boolean inDither;
        @Deprecated
        public boolean inInputShareable;
        public boolean inJustDecodeBounds;
        public boolean inMutable;
        @Deprecated
        public boolean inPreferQualityOverSpeed;
        public ColorSpace inPreferredColorSpace = null;
        public Bitmap.Config inPreferredConfig = Bitmap.Config.ARGB_8888;
        public boolean inPremultiplied = true;
        @Deprecated
        public boolean inPurgeable;
        public int inSampleSize;
        public boolean inScaled = true;
        public int inScreenDensity;
        public int inTargetDensity;
        public byte[] inTempStorage;
        public boolean inThumbnailMode = false;
        @Deprecated
        public boolean mCancel;
        public boolean mDecodeBitmapOpt = false;
        public ColorSpace outColorSpace;
        public Bitmap.Config outConfig;
        public int outHeight;
        public String outMimeType;
        public int outWidth;

        @Deprecated
        public void requestCancelDecode() {
            this.mCancel = true;
        }

        static void validate(Options opts) {
            if (opts != null) {
                if (opts.inBitmap != null && opts.inBitmap.getConfig() == Bitmap.Config.HARDWARE) {
                    throw new IllegalArgumentException("Bitmaps with Config.HARWARE are always immutable");
                } else if (!opts.inMutable || opts.inPreferredConfig != Bitmap.Config.HARDWARE) {
                    if (opts.inPreferredColorSpace != null) {
                        if (!(opts.inPreferredColorSpace instanceof ColorSpace.Rgb)) {
                            throw new IllegalArgumentException("The destination color space must use the RGB color model");
                        } else if (((ColorSpace.Rgb) opts.inPreferredColorSpace).getTransferParameters() == null) {
                            throw new IllegalArgumentException("The destination color space must use an ICC parametric transfer function");
                        }
                    }
                } else {
                    throw new IllegalArgumentException("Bitmaps with Config.HARDWARE cannot be decoded into - they are immutable");
                }
            }
        }
    }

    private static native Bitmap nativeDecodeAsset(long j, Rect rect, Options options);

    private static native Bitmap nativeDecodeByteArray(byte[] bArr, int i, int i2, Options options);

    private static native Bitmap nativeDecodeFileDescriptor(FileDescriptor fileDescriptor, Rect rect, Options options);

    private static native Bitmap nativeDecodeStream(InputStream inputStream, byte[] bArr, Rect rect, Options options);

    private static native boolean nativeIsSeekable(FileDescriptor fileDescriptor);

    public static Bitmap decodeFile(String pathName, Options opts) {
        Bitmap bm = null;
        InputStream stream = null;
        Trace.traceBegin(2, "decodeFile");
        if (opts == null) {
            try {
                if (mIAwareBitmapCacher == null) {
                    mIAwareBitmapCacher = HwFrameworkFactory.getHwIAwareBitmapCacher();
                }
                if (mIAwareBitmapCacher != null) {
                    bm = mIAwareBitmapCacher.getCachedBitmap(pathName);
                }
                if (bm != null) {
                    Log.d("BitmapFactory", "decodeFile getCachedBitmap ok.");
                    Trace.traceEnd(2);
                    if (stream != null) {
                        try {
                            stream.close();
                        } catch (IOException e) {
                        }
                    }
                    return bm;
                }
            } catch (Exception e2) {
                Log.e("BitmapFactory", "Unable to decode stream: " + e2);
            } catch (Throwable th) {
                Trace.traceEnd(2);
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e3) {
                    }
                }
                throw th;
            }
        } else {
            Options.validate(opts);
        }
        stream = new FileInputStream(pathName);
        bm = decodeStream(stream, null, opts);
        if (!(bm == null || mIAwareBitmapCacher == null || opts != null)) {
            mIAwareBitmapCacher.cacheBitmap(pathName, bm, opts);
        }
        Trace.traceEnd(2);
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException e4) {
            }
        }
        return bm;
    }

    public static Bitmap decodeFile(String pathName) {
        return decodeFile(pathName, null);
    }

    public static Bitmap decodeResourceStream(Resources res, TypedValue value, InputStream is, Rect pad, Options opts) {
        Options.validate(opts);
        if (opts == null) {
            opts = new Options();
        }
        if (opts.inDensity == 0 && value != null) {
            int density = value.density;
            if (density == 0) {
                opts.inDensity = 160;
            } else if (density != 65535) {
                opts.inDensity = density;
            }
        }
        if (opts.inTargetDensity == 0 && res != null) {
            opts.inTargetDensity = res.getDisplayMetrics().densityDpi;
            if (res.getResourceScaleOpt()) {
                setDecodeBitmapOpt(opts);
            }
        }
        return decodeStream(is, pad, opts);
    }

    public static Bitmap decodeResource(Resources res, int id, Options opts) {
        return decodeResource(res, id, opts, null);
    }

    public static Bitmap decodeResource(Resources res, int id, Options opts, Rect padding) {
        Bitmap bm = null;
        InputStream is = null;
        Trace.traceBegin(2, "decodeResource");
        if (padding == null && opts == null) {
            try {
                if (mIAwareBitmapCacher == null) {
                    mIAwareBitmapCacher = HwFrameworkFactory.getHwIAwareBitmapCacher();
                }
                if (mIAwareBitmapCacher != null) {
                    bm = mIAwareBitmapCacher.getCachedBitmap(res, id);
                }
                if (bm != null) {
                    Log.d("BitmapFactory", "decodeResource getCachedBitmap ok");
                    return bm;
                }
            } finally {
                Trace.traceEnd(2);
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e) {
                    }
                }
            }
        }
        Options.validate(opts);
        try {
            bm = HwThemeManager.getThemeBitmap(res, id, padding);
            if (bm != null) {
                Trace.traceEnd(2);
                if (is != null) {
                    try {
                        is.close();
                    } catch (IOException e2) {
                    }
                }
                return bm;
            }
            TypedValue value = new TypedValue();
            is = res.openRawResource(id, value);
            bm = decodeResourceStream(res, value, is, padding, opts);
            if (!(bm == null || opts != null || mIAwareBitmapCacher == null)) {
                mIAwareBitmapCacher.cacheBitmap(res, id, bm, opts);
            }
            Trace.traceEnd(2);
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e3) {
                }
            }
            if (bm != null || opts == null || opts.inBitmap == null) {
                return bm;
            }
            throw new IllegalArgumentException("Problem decoding into existing bitmap");
        } catch (Exception | RuntimeException e4) {
        }
    }

    public static Bitmap decodeResource(Resources res, int id) {
        return decodeResource(res, id, null);
    }

    public static Bitmap decodeByteArray(byte[] data, int offset, int length, Options opts) {
        if ((offset | length) < 0 || data.length < offset + length) {
            throw new ArrayIndexOutOfBoundsException();
        }
        Options.validate(opts);
        Trace.traceBegin(2, "decodeBitmap");
        try {
            long start = System.currentTimeMillis();
            Bitmap bm = nativeDecodeByteArray(data, offset, length, opts);
            Jlog.checkBitmapDecodeTime(start);
            if (bm == null && opts != null) {
                if (opts.inBitmap != null) {
                    throw new IllegalArgumentException("Problem decoding into existing bitmap");
                }
            }
            setDensityFromOptions(bm, opts);
            return bm;
        } finally {
            Trace.traceEnd(2);
        }
    }

    public static Bitmap decodeByteArray(byte[] data, int offset, int length) {
        return decodeByteArray(data, offset, length, null);
    }

    private static void setDensityFromOptions(Bitmap outputBitmap, Options opts) {
        if (outputBitmap != null && opts != null) {
            int density = opts.inDensity;
            if (density != 0) {
                outputBitmap.setDensity(density);
                int targetDensity = opts.inTargetDensity;
                if (targetDensity != 0 && density != targetDensity && density != opts.inScreenDensity) {
                    byte[] np = outputBitmap.getNinePatchChunk();
                    boolean isNinePatch = np != null && NinePatch.isNinePatchChunk(np);
                    if (opts.inScaled || isNinePatch) {
                        outputBitmap.setDensity(targetDensity);
                    }
                }
            } else if (opts.inBitmap != null) {
                outputBitmap.setDensity(Bitmap.getDefaultDensity());
            }
        }
    }

    /* JADX INFO: finally extract failed */
    public static Bitmap decodeStream(InputStream is, Rect outPadding, Options opts) {
        Bitmap bm;
        if (is == null) {
            return null;
        }
        Options.validate(opts);
        Trace.traceBegin(2, "decodeBitmap");
        if (HwGalleryCacheManager.isGalleryCacheEffect()) {
            if (opts == null || !opts.inJustDecodeBounds || is != mLastIS || mLastWidth <= 0 || mLastHeight <= 0) {
                Bitmap bm2 = HwGalleryCacheManager.getGalleryCachedImage(is, opts);
                if (bm2 != null) {
                    setDensityFromOptions(bm2, opts);
                    Trace.traceEnd(2);
                    return bm2;
                }
            } else {
                opts.outWidth = mLastWidth;
                opts.outHeight = mLastHeight;
                Trace.traceEnd(2);
                return null;
            }
        }
        try {
            if (is instanceof AssetManager.AssetInputStream) {
                long asset = ((AssetManager.AssetInputStream) is).getNativeAsset();
                long start = System.currentTimeMillis();
                bm = nativeDecodeAsset(asset, outPadding, opts);
                Jlog.checkBitmapDecodeTime(start);
            } else {
                bm = decodeStreamInternal(is, outPadding, opts);
            }
            if (bm == null && opts != null) {
                if (opts.inBitmap != null) {
                    throw new IllegalArgumentException("Problem decoding into existing bitmap");
                }
            }
            setDensityFromOptions(bm, opts);
            Trace.traceEnd(2);
            if (HwGalleryCacheManager.isGalleryCacheEffect() && opts != null && opts.inJustDecodeBounds) {
                mLastIS = is;
                mLastWidth = opts.outWidth;
                mLastHeight = opts.outHeight;
            }
            String threadName = Thread.currentThread().getName();
            if (0 != 0 && threadName.contains(FB_THREAD_NAME) && HwWidgetFactory.isSuperResolutionSupport()) {
                SRBitmapManager manager = HwWidgetFactory.getSRBitmapManager();
                if (manager != null) {
                    manager.srBitmap(bm);
                }
            }
            return bm;
        } catch (Throwable th) {
            Trace.traceEnd(2);
            throw th;
        }
    }

    private static Bitmap decodeStreamInternal(InputStream is, Rect outPadding, Options opts) {
        byte[] tempStorage = null;
        if (opts != null) {
            tempStorage = opts.inTempStorage;
        }
        if (tempStorage == null) {
            tempStorage = new byte[16384];
        }
        long start = System.currentTimeMillis();
        Bitmap bm = nativeDecodeStream(is, tempStorage, outPadding, opts);
        Jlog.checkBitmapDecodeTime(start);
        return bm;
    }

    public static Bitmap decodeStream(InputStream is) {
        return decodeStream(is, null, null);
    }

    public static Bitmap decodeFileDescriptor(FileDescriptor fd, Rect outPadding, Options opts) {
        Bitmap bm;
        FileInputStream fis;
        Options.validate(opts);
        Trace.traceBegin(2, "decodeFileDescriptor");
        try {
            if (nativeIsSeekable(fd)) {
                long start = System.currentTimeMillis();
                bm = nativeDecodeFileDescriptor(fd, outPadding, opts);
                Jlog.checkBitmapDecodeTime(start);
            } else {
                fis = new FileInputStream(fd);
                Bitmap bm2 = decodeStreamInternal(fis, outPadding, opts);
                try {
                    fis.close();
                } catch (Throwable th) {
                }
                bm = bm2;
            }
            if (bm == null && opts != null) {
                if (opts.inBitmap != null) {
                    throw new IllegalArgumentException("Problem decoding into existing bitmap");
                }
            }
            setDensityFromOptions(bm, opts);
            Trace.traceEnd(2);
            return bm;
        } catch (Throwable th2) {
            Trace.traceEnd(2);
            throw th2;
        }
        throw th;
    }

    public static Bitmap decodeFileDescriptor(FileDescriptor fd) {
        return decodeFileDescriptor(fd, null, null);
    }

    private static void setDecodeBitmapOpt(Options opts) {
        if (HwFrameworkFactory.getHwActivityThread().decodeBitmapOptEnable() && HwFrameworkFactory.getHwActivityThread().isPerfOptEnable(1) == 1 && opts.inTargetDensity > opts.inDensity && opts.inSampleSize <= 1) {
            opts.mDecodeBitmapOpt = true;
        }
    }

    public static boolean convertHeifToJpeg(String pathName, OutputStream stream) {
        Options opts = new Options();
        opts.inPreferredConfig = Bitmap.Config.YCRCB_420_SP;
        opts.inSampleSize = 1;
        opts.inScaled = false;
        Trace.traceBegin(2, "convertHeifToJpeg_decode");
        Bitmap bmp = decodeFile(pathName, opts);
        Trace.traceEnd(2);
        if (bmp == null) {
            Log.e("BitmapFactory::HEIFCONVERT", "Decode heif to yuv fail!");
            return false;
        }
        Bitmap.CompressFormat format = Bitmap.CompressFormat.JPEG;
        Trace.traceBegin(2, "convertHeifToJpeg_encode");
        boolean result = bmp.compressYuvToJpeg(format, 90, stream);
        Trace.traceEnd(2);
        if (result) {
            return true;
        }
        Log.e("BitmapFactory::HEIFCONVERT", "Encode yuv to jpeg fail!");
        return false;
    }
}
