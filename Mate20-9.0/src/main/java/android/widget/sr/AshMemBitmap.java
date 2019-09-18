package android.widget.sr;

import android.graphics.Bitmap;
import android.graphics.ColorSpace;
import android.util.DisplayMetrics;

public class AshMemBitmap {
    public static final int NOT_ASH_BITMAP_FD = -1;
    public static final String TAG = "AshMemBitmap";

    public static NativeBitmap createSrcNativeBitmap(Bitmap bmp) {
        NativeBitmap resBmp;
        long ptr = BitmapUtils.getAshBitmapPtr(bmp);
        int fd = HwSuperResolution.nativeGetFdFromPtr(ptr);
        if (-1 == fd) {
            Bitmap ashSrcBmp = BitmapUtils.createAshBitmap(bmp);
            long srcPtr = BitmapUtils.getAshBitmapPtr(ashSrcBmp);
            int srcFd = HwSuperResolution.nativeGetFdFromPtr(srcPtr);
            if (-1 == srcFd) {
                return null;
            }
            resBmp = new NativeBitmap(ashSrcBmp, srcPtr, srcFd);
        } else {
            resBmp = new NativeBitmap(bmp, ptr, fd);
        }
        return resBmp;
    }

    public static NativeBitmap createDesNativeBitmap(int w, int h, int c, int ratio) {
        NativeBitmap resBmp = null;
        Bitmap desAshBmp = createSRBitmap(w * ratio, h * ratio, Bitmap.Config.ARGB_8888);
        if (desAshBmp != null) {
            long desPtr = BitmapUtils.getAshBitmapPtr(desAshBmp);
            int desFd = HwSuperResolution.nativeGetFdFromPtr(desPtr);
            if (-1 == desFd) {
                return null;
            }
            resBmp = new NativeBitmap(desAshBmp, desPtr, desFd);
        }
        return resBmp;
    }

    private static Bitmap createSRBitmap(int width, int height, Bitmap.Config config) {
        return createSRBitmap(null, width, height, config, true, ColorSpace.get(ColorSpace.Named.SRGB));
    }

    private static Bitmap createSRBitmap(DisplayMetrics display, int width, int height, Bitmap.Config config, boolean hasAlpha, ColorSpace colorSpace) {
        Bitmap bm;
        DisplayMetrics displayMetrics = display;
        Bitmap.Config config2 = config;
        boolean z = hasAlpha;
        ColorSpace colorSpace2 = colorSpace;
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("width and height must be > 0");
        } else if (config2 == Bitmap.Config.HARDWARE) {
            throw new IllegalArgumentException("can't create mutable bitmap with Config.HARDWARE");
        } else if (colorSpace2 != null) {
            if (config2 != Bitmap.Config.ARGB_8888 || colorSpace2 == ColorSpace.get(ColorSpace.Named.SRGB)) {
                bm = HwSuperResolution.nativeSRCreate(null, 0, width, width, height, 5, true, null, null);
            } else if (colorSpace2 instanceof ColorSpace.Rgb) {
                ColorSpace.Rgb rgb = (ColorSpace.Rgb) colorSpace2;
                ColorSpace.Rgb.TransferParameters parameters = rgb.getTransferParameters();
                if (parameters != null) {
                    ColorSpace.Rgb d50 = (ColorSpace.Rgb) ColorSpace.adapt(rgb, ColorSpace.ILLUMINANT_D50);
                    ColorSpace.Rgb rgb2 = d50;
                    ColorSpace.Rgb rgb3 = rgb;
                    bm = HwSuperResolution.nativeSRCreate(null, 0, width, width, height, 5, true, d50.getTransform(), parameters);
                } else {
                    ColorSpace.Rgb rgb4 = rgb;
                    throw new IllegalArgumentException("colorSpace must use an ICC parametric transfer function");
                }
            } else {
                throw new IllegalArgumentException("colorSpace must be an RGB color space");
            }
            if (bm == null) {
                return null;
            }
            if (displayMetrics != null) {
                bm.setDensity(displayMetrics.densityDpi);
            }
            bm.setHasAlpha(z);
            if ((config2 == Bitmap.Config.ARGB_8888 || config2 == Bitmap.Config.RGBA_F16) && !z) {
                HwSuperResolution.nativeErase(BitmapUtils.getAshBitmapPtr(bm), -16777216);
            }
            return bm;
        } else {
            throw new IllegalArgumentException("can't create bitmap without a color space");
        }
    }
}
