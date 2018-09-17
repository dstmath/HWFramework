package android.graphics;

import android.content.res.AssetManager.AssetInputStream;
import android.content.res.Resources;
import android.graphics.Bitmap.Config;
import android.graphics.ColorSpace.Rgb;
import android.hwgallerycache.HwGalleryCacheManager;
import android.hwtheme.HwThemeManager;
import android.os.Trace;
import android.util.Log;
import android.util.TypedValue;
import android.widget.sr.SRBitmapManager;
import android.widget.sr.Utils;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class BitmapFactory {
    private static final int DECODE_BUFFER_SIZE = 16384;
    private static final String FB_THREAD_NAME = "Fg_ImgDecode";
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
        public boolean inPreferQualityOverSpeed;
        public ColorSpace inPreferredColorSpace = null;
        public Config inPreferredConfig = Config.ARGB_8888;
        public boolean inPremultiplied = true;
        @Deprecated
        public boolean inPurgeable;
        public int inSampleSize;
        public boolean inScaled = true;
        public int inScreenDensity;
        public int inTargetDensity;
        public byte[] inTempStorage;
        public boolean inThumbnailMode = false;
        public boolean mCancel;
        public ColorSpace outColorSpace;
        public Config outConfig;
        public int outHeight;
        public String outMimeType;
        public int outWidth;

        public void requestCancelDecode() {
            this.mCancel = true;
        }

        static void validate(Options opts) {
            if (opts != null) {
                if (opts.inMutable && opts.inPreferredConfig == Config.HARDWARE) {
                    throw new IllegalArgumentException("Bitmaps with Config.HARWARE are always immutable");
                }
                if (opts.inPreferredColorSpace != null) {
                    if (!(opts.inPreferredColorSpace instanceof Rgb)) {
                        throw new IllegalArgumentException("The destination color space must use the RGB color model");
                    } else if (((Rgb) opts.inPreferredColorSpace).getTransferParameters() == null) {
                        throw new IllegalArgumentException("The destination color space must use an ICC parametric transfer function");
                    }
                }
            }
        }
    }

    private static native Bitmap nativeDecodeAsset(long j, Rect rect, Options options);

    private static native Bitmap nativeDecodeByteArray(byte[] bArr, int i, int i2, Options options);

    private static native Bitmap nativeDecodeFileDescriptor(FileDescriptor fileDescriptor, Rect rect, Options options);

    private static native Bitmap nativeDecodeStream(InputStream inputStream, byte[] bArr, Rect rect, Options options);

    private static native boolean nativeIsSeekable(FileDescriptor fileDescriptor);

    /* JADX WARNING: Removed duplicated region for block: B:21:0x003e A:{SYNTHETIC, Splitter: B:21:0x003e} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static Bitmap decodeFile(String pathName, Options opts) {
        Exception e;
        Throwable th;
        Options.validate(opts);
        Bitmap bm = null;
        InputStream stream = null;
        try {
            InputStream stream2 = new FileInputStream(pathName);
            try {
                bm = decodeStream(stream2, null, opts);
                if (stream2 != null) {
                    try {
                        stream2.close();
                    } catch (IOException e2) {
                    }
                }
                stream = stream2;
            } catch (Exception e3) {
                e = e3;
                stream = stream2;
            } catch (Throwable th2) {
                th = th2;
                stream = stream2;
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e4) {
                    }
                }
                throw th;
            }
        } catch (Exception e5) {
            e = e5;
            try {
                Log.e("BitmapFactory", "Unable to decode stream: " + e);
                if (stream != null) {
                    try {
                        stream.close();
                    } catch (IOException e6) {
                    }
                }
                return bm;
            } catch (Throwable th3) {
                th = th3;
                if (stream != null) {
                }
                throw th;
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
        }
        return decodeStream(is, pad, opts);
    }

    public static Bitmap decodeResource(Resources res, int id, Options opts) {
        return decodeResource(res, id, opts, null);
    }

    public static Bitmap decodeResource(Resources res, int id, Options opts, Rect padding) {
        Options.validate(opts);
        Bitmap bm = null;
        InputStream inputStream = null;
        try {
            bm = HwThemeManager.getThemeBitmap(res, id, padding);
            if (bm != null) {
                return bm;
            }
            TypedValue value = new TypedValue();
            inputStream = res.openRawResource(id, value);
            bm = decodeResourceStream(res, value, inputStream, padding, opts);
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                }
            }
            if (bm != null || opts == null || opts.inBitmap == null) {
                return bm;
            }
            throw new IllegalArgumentException("Problem decoding into existing bitmap");
        } catch (RuntimeException e2) {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e3) {
                }
            }
        } catch (Exception e4) {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e5) {
                }
            }
        } catch (Throwable th) {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e6) {
                }
            }
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
            Bitmap bm = nativeDecodeByteArray(data, offset, length, opts);
            if (bm != null || opts == null || opts.inBitmap == null) {
                setDensityFromOptions(bm, opts);
                return bm;
            }
            throw new IllegalArgumentException("Problem decoding into existing bitmap");
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
                    boolean isNinePatch = np != null ? NinePatch.isNinePatchChunk(np) : false;
                    if (opts.inScaled || isNinePatch) {
                        outputBitmap.setDensity(targetDensity);
                    }
                }
            } else if (opts.inBitmap != null) {
                outputBitmap.setDensity(Bitmap.getDefaultDensity());
            }
        }
    }

    public static Bitmap decodeStream(InputStream is, Rect outPadding, Options opts) {
        if (is == null) {
            return null;
        }
        Options.validate(opts);
        Bitmap bm = null;
        Trace.traceBegin(2, "decodeBitmap");
        if (HwGalleryCacheManager.isGalleryCacheEffect()) {
            if (opts == null || !opts.inJustDecodeBounds || is != mLastIS || mLastWidth <= 0 || mLastHeight <= 0) {
                bm = HwGalleryCacheManager.getGalleryCachedImage(is, opts);
                if (bm != null) {
                    setDensityFromOptions(bm, opts);
                    Trace.traceEnd(2);
                    return bm;
                }
            }
            opts.outWidth = mLastWidth;
            opts.outHeight = mLastHeight;
            Trace.traceEnd(2);
            return null;
        }
        try {
            if (is instanceof AssetInputStream) {
                bm = nativeDecodeAsset(((AssetInputStream) is).getNativeAsset(), outPadding, opts);
            } else {
                bm = decodeStreamInternal(is, outPadding, opts);
            }
            if (bm != null || opts == null || opts.inBitmap == null) {
                setDensityFromOptions(bm, opts);
                if (HwGalleryCacheManager.isGalleryCacheEffect() && opts != null && opts.inJustDecodeBounds) {
                    mLastIS = is;
                    mLastWidth = opts.outWidth;
                    mLastHeight = opts.outHeight;
                }
                String threadName = Thread.currentThread().getName();
                if (true && threadName.contains(FB_THREAD_NAME) && Utils.isSuperResolutionSupport()) {
                    SRBitmapManager.getInstance().srBitmap(bm);
                }
                return bm;
            }
            throw new IllegalArgumentException("Problem decoding into existing bitmap");
        } finally {
            Trace.traceEnd(2);
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
        return nativeDecodeStream(is, tempStorage, outPadding, opts);
    }

    public static Bitmap decodeStream(InputStream is) {
        return decodeStream(is, null, null);
    }

    public static Bitmap decodeFileDescriptor(FileDescriptor fd, Rect outPadding, Options opts) {
        Options.validate(opts);
        Trace.traceBegin(2, "decodeFileDescriptor");
        FileInputStream fis;
        try {
            Bitmap bm;
            if (nativeIsSeekable(fd)) {
                bm = nativeDecodeFileDescriptor(fd, outPadding, opts);
            } else {
                fis = new FileInputStream(fd);
                bm = decodeStreamInternal(fis, outPadding, opts);
                try {
                    fis.close();
                } catch (Throwable th) {
                }
            }
            if (bm != null || opts == null || opts.inBitmap == null) {
                setDensityFromOptions(bm, opts);
                Trace.traceEnd(2);
                return bm;
            }
            throw new IllegalArgumentException("Problem decoding into existing bitmap");
        } catch (Throwable th2) {
            Trace.traceEnd(2);
        }
    }

    public static Bitmap decodeFileDescriptor(FileDescriptor fd) {
        return decodeFileDescriptor(fd, null, null);
    }
}
