package android.widget.sr;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.ColorSpace;
import android.graphics.ColorSpace.Named;
import android.graphics.ColorSpace.Rgb;
import android.graphics.ColorSpace.Rgb.TransferParameters;
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
        NativeBitmap nativeBitmap = null;
        Bitmap desAshBmp = createSRBitmap(w * ratio, h * ratio, Config.ARGB_8888);
        if (desAshBmp != null) {
            long desPtr = BitmapUtils.getAshBitmapPtr(desAshBmp);
            int desFd = HwSuperResolution.nativeGetFdFromPtr(desPtr);
            if (-1 == desFd) {
                return null;
            }
            nativeBitmap = new NativeBitmap(desAshBmp, desPtr, desFd);
        }
        return nativeBitmap;
    }

    private static Bitmap createSRBitmap(int width, int height, Config config) {
        return createSRBitmap(null, width, height, config, true, ColorSpace.get(Named.SRGB));
    }

    private static Bitmap createSRBitmap(DisplayMetrics display, int width, int height, Config config, boolean hasAlpha, ColorSpace colorSpace) {
        if (width <= 0 || height <= 0) {
            throw new IllegalArgumentException("width and height must be > 0");
        } else if (config == Config.HARDWARE) {
            throw new IllegalArgumentException("can't create mutable bitmap with Config.HARDWARE");
        } else if (colorSpace == null) {
            throw new IllegalArgumentException("can't create bitmap without a color space");
        } else {
            Bitmap bm;
            if (config != Config.ARGB_8888 || colorSpace == ColorSpace.get(Named.SRGB)) {
                bm = HwSuperResolution.nativeSRCreate(null, 0, width, width, height, 5, true, null, null);
            } else if (colorSpace instanceof Rgb) {
                Rgb rgb = (Rgb) colorSpace;
                TransferParameters parameters = rgb.getTransferParameters();
                if (parameters == null) {
                    throw new IllegalArgumentException("colorSpace must use an ICC parametric transfer function");
                }
                bm = HwSuperResolution.nativeSRCreate(null, 0, width, width, height, 5, true, ((Rgb) ColorSpace.adapt(rgb, ColorSpace.ILLUMINANT_D50)).getTransform(), parameters);
            } else {
                throw new IllegalArgumentException("colorSpace must be an RGB color space");
            }
            if (bm == null) {
                return null;
            }
            if (display != null) {
                bm.setDensity(display.densityDpi);
            }
            bm.setHasAlpha(hasAlpha);
            if ((config == Config.ARGB_8888 || config == Config.RGBA_F16) && (hasAlpha ^ 1) != 0) {
                HwSuperResolution.nativeErase(BitmapUtils.getAshBitmapPtr(bm), -16777216);
            }
            return bm;
        }
    }
}
