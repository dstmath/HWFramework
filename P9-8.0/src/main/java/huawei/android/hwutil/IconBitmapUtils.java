package huawei.android.hwutil;

import android.cover.CoverManager;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

public class IconBitmapUtils {
    private static final boolean DEBUG_ICON = false;
    private static final float HFACTOR = 0.9f;
    private static final int MASK_ABS_VALID_RANGE = 10;
    private static final float SFACTOR = 0.8f;
    private static final float SMALLER_HUE = 30.0f;
    private static String TAG = "IconBitmapUtils";
    private static String TAG_ICON = "RsourcesEx";
    private static int VALID_TRANSPARENCY = 40;
    private static final float VFACTOR = 1.2f;

    public static void mask(int[] pixels, int w, int h, Bitmap mask) {
        if (mask != null) {
            int wMask = mask.getWidth();
            int hMask = mask.getHeight();
            if (wMask < w || hMask < h) {
                Log.w(TAG, "Mask size error, wMask=" + wMask + ",hMask=" + hMask + ",w=" + w + ",h=" + h);
                return;
            }
            int[] maskPixels = new int[(w * h)];
            mask.getPixels(maskPixels, 0, w, (wMask - w) / 2, (wMask - h) / 2, w, h);
            for (int i = (w * h) - 1; i >= 0; i--) {
                pixels[i] = ((((pixels[i] >>> 24) * (maskPixels[i] >>> 24)) / 255) << 24) | (pixels[i] & 16777215);
            }
        }
    }

    public static Bitmap mask(Bitmap bmpSrc, Bitmap bmpMask) {
        if (bmpSrc == null || bmpMask == null) {
            return null;
        }
        Bitmap newBitMap = bmpMask.copy(Config.ARGB_8888, true);
        try {
            int w = bmpSrc.getWidth();
            int h = bmpSrc.getHeight();
            int wMask = newBitMap.getWidth();
            int hMask = newBitMap.getHeight();
            int srcStartX = 0;
            int srcStartY = 0;
            if (w > wMask) {
                srcStartX = (w - wMask) / 2;
                w = wMask;
            }
            if (h > hMask) {
                srcStartY = (h - hMask) / 2;
                h = hMask;
            }
            int[] srcPixels = new int[(w * h)];
            int[] maskPixels = new int[(w * h)];
            bmpSrc.getPixels(srcPixels, 0, w, srcStartX, srcStartY, w, h);
            newBitMap.getPixels(maskPixels, 0, w, (wMask - w) / 2, (hMask - h) / 2, w, h);
            for (int i = (w * h) - 1; i >= 0; i--) {
                maskPixels[i] = ((((maskPixels[i] >>> 24) * (srcPixels[i] >>> 24)) / 255) << 24) | (srcPixels[i] & 16777215);
            }
            newBitMap.setPixels(maskPixels, 0, w, (wMask - w) / 2, (hMask - h) / 2, w, h);
            int[] mask = new int[(wMask * hMask)];
            newBitMap.getPixels(mask, 0, wMask, 0, 0, wMask, hMask);
            setTransparent(mask, wMask, 0, 0, wMask, (hMask - h) / 2);
            setTransparent(mask, wMask, 0, (hMask + h) / 2, wMask, hMask);
            setTransparent(mask, wMask, 0, 0, (wMask - w) / 2, hMask);
            setTransparent(mask, wMask, (wMask + w) / 2, 0, wMask, hMask);
            newBitMap.setPixels(mask, 0, wMask, 0, 0, wMask, hMask);
            return newBitMap;
        } catch (RuntimeException e) {
            if (newBitMap != null) {
                newBitMap.recycle();
            }
            return null;
        }
    }

    private static void setTransparent(int[] pixels, int w, int left, int top, int right, int bottom) {
        for (int i = top; i < bottom; i++) {
            for (int j = left; j < right; j++) {
                int pos = (i * w) + j;
                pixels[pos] = pixels[pos] & 16777215;
            }
        }
    }

    public static Bitmap overlap2Bitmap(Bitmap bmpSrc, Bitmap bmpBg, int deltaX, int deltaY) {
        if (bmpSrc == null || bmpBg == null) {
            return null;
        }
        int w = bmpSrc.getWidth();
        int h = bmpSrc.getHeight();
        int wBg = bmpBg.getWidth();
        int hBg = bmpBg.getHeight();
        if (w > wBg || h > hBg) {
            Log.e(TAG, "The size of background(" + wBg + "," + hBg + ") little than the source bitmap(" + w + "," + h + ")");
            return null;
        }
        Canvas canvas = new Canvas();
        Paint paint = new Paint();
        Bitmap newBitmap = Bitmap.createBitmap(wBg, hBg, Config.ARGB_8888);
        canvas.setBitmap(newBitmap);
        canvas.drawBitmap(bmpBg, 0.0f, 0.0f, paint);
        canvas.drawBitmap(bmpSrc, (float) (((wBg - w) >> 1) + deltaX), (float) (((hBg - h) >> 1) + deltaY), paint);
        return newBitmap;
    }

    public static Bitmap overlap2Bitmap(Bitmap bmpSrc, Bitmap bmpBg) {
        return overlap2Bitmap(bmpSrc, bmpBg, 0, 0);
    }

    public static Bitmap zoomIfNeed(Bitmap bmp, int standardSize, boolean recycledWhenScaled) {
        int w = bmp.getWidth();
        int h = bmp.getHeight();
        if (w == standardSize && h == standardSize) {
            return bmp;
        }
        Bitmap tmpBitmap = Bitmap.createScaledBitmap(bmp, standardSize, standardSize, true);
        if (recycledWhenScaled) {
            bmp.recycle();
        }
        return tmpBitmap;
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        }
        try {
            Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
            return bitmap;
        } catch (Exception e) {
            return null;
        }
    }

    private static boolean checkTransparency(int argb) {
        if ((argb >>> 24) >= VALID_TRANSPARENCY) {
            return true;
        }
        return false;
    }

    private static int getValidTop(int[] pixels, int w, int h) {
        int mid_w = w % 2 == 0 ? (w / 2) - 1 : w / 2;
        for (int i = 0; i < h; i++) {
            int y = i * w;
            int j = mid_w;
            while (j >= 0) {
                if (checkTransparency(pixels[y + j]) || checkTransparency(pixels[((w - 1) - j) + y])) {
                    return i;
                }
                j--;
            }
        }
        return h;
    }

    private static int getValidBottom(int[] pixels, int w, int h) {
        int mid_w = w % 2 == 0 ? (w / 2) - 1 : w / 2;
        for (int i = h - 1; i >= 0; i--) {
            int y = i * w;
            int j = mid_w;
            while (j >= 0) {
                if (checkTransparency(pixels[y + j]) || checkTransparency(pixels[((w - 1) - j) + y])) {
                    return i;
                }
                j--;
            }
        }
        return 0;
    }

    private static int getValidLeft(int[] pixels, int w, int h) {
        int mid_h = h % 2 == 0 ? (h / 2) - 1 : h / 2;
        int i = 0;
        while (i < w) {
            int j = mid_h;
            while (j >= 0) {
                if (checkTransparency(pixels[(j * w) + i]) || checkTransparency(pixels[(((h - 1) - j) * w) + i])) {
                    return i;
                }
                j--;
            }
            i++;
        }
        return w;
    }

    private static int getValidRight(int[] pixels, int w, int h) {
        int mid_h = h % 2 == 0 ? (h / 2) - 1 : h / 2;
        int i = w - 1;
        while (i >= 0) {
            int j = mid_h;
            while (j >= 0) {
                if (checkTransparency(pixels[(j * w) + i]) || checkTransparency(pixels[(((h - 1) - j) * w) + i])) {
                    return i;
                }
                j--;
            }
            i--;
        }
        return 0;
    }

    public static Rect getIconInfo(Bitmap bmp) {
        int w = bmp.getWidth();
        int h = bmp.getHeight();
        int[] pixels = new int[(w * h)];
        bmp.getPixels(pixels, 0, w, 0, 0, w, h);
        int top = getValidTop(pixels, w, h);
        int bottom = getValidBottom(pixels, w, h);
        int left = getValidLeft(pixels, w, h);
        int right = getValidRight(pixels, w, h);
        if (left >= right || top >= bottom) {
            return null;
        }
        return new Rect(left, top, right, bottom);
    }

    public static Bitmap composeIcon(Bitmap src, Bitmap mask, Bitmap background, Bitmap border, boolean useAvgColor) {
        int w = src.getWidth();
        int h = src.getHeight();
        int[] pixels = new int[(w * h)];
        src.getPixels(pixels, 0, w, 0, 0, w, h);
        Bitmap resultBitmap = Bitmap.createBitmap(w, h, Config.ARGB_8888);
        if (resultBitmap != null) {
            resultBitmap.setDensity(src.getDensity());
        }
        Canvas canvas = new Canvas(resultBitmap);
        Paint paint = new Paint();
        if (mask != null) {
            mask(pixels, w, h, mask);
        }
        if (background != null) {
            if (useAvgColor) {
                drawBackground(canvas, background, w, h, pixels);
            } else {
                canvas.drawBitmap(background, 0.0f, 0.0f, null);
            }
        }
        canvas.drawBitmap(pixels, 0, w, 0, 0, w, h, true, null);
        if (border != null) {
            canvas.drawBitmap(border, 0.0f, 0.0f, paint);
        }
        src.recycle();
        return resultBitmap;
    }

    public static Bitmap drawSource(Bitmap bmp, int standardSize, int iconSize) {
        Canvas canvas = new Canvas();
        Paint paint = new Paint();
        Bitmap resultBitmap = Bitmap.createBitmap(standardSize, standardSize, Config.ARGB_8888);
        if (resultBitmap != null) {
            resultBitmap.setDensity(bmp.getDensity());
        }
        canvas.setBitmap(resultBitmap);
        Bitmap tmp = Bitmap.createScaledBitmap(bmp, iconSize, iconSize, true);
        canvas.translate(((float) (standardSize - iconSize)) / 2.0f, ((float) (standardSize - iconSize)) / 2.0f);
        canvas.drawBitmap(tmp, 0.0f, 0.0f, paint);
        tmp.recycle();
        return resultBitmap;
    }

    private static int getAvgColor(int[] tempPixels, int w, int h) {
        int count = 0;
        int aR = 0;
        int aG = 0;
        int aB = 0;
        for (int j = (w * h) - 1; j >= 0; j--) {
            int color = 16777215 & tempPixels[j];
            if (color > 0) {
                aR += Color.red(color);
                aG += Color.green(color);
                aB += Color.blue(color);
                count++;
            }
        }
        if (count > 0) {
            aR /= count;
            aG /= count;
            aB /= count;
        }
        return Color.rgb(aR, aG, aB);
    }

    private static void drawBackground(Canvas canvas, Bitmap background, int w, int h, int[] pixels) {
        int avgColor = getAvgColor(pixels, w, h);
        int bw = background.getWidth();
        int bh = background.getHeight();
        int[] backPixels = new int[(bw * bh)];
        background.getPixels(backPixels, 0, bw, 0, 0, bw, bh);
        float[] hsv = new float[3];
        Color.colorToHSV(avgColor, hsv);
        int hue = (int) hsv[0];
        float saturation = hsv[1];
        float brightness = hsv[2];
        hsv[0] = getHue(hue, HFACTOR);
        hsv[1] = getSaturationFactor(hue, 0.8f) * saturation;
        hsv[2] = getBrightnessFactor(hue, VFACTOR) * brightness;
        int backColor = Color.HSVToColor(hsv);
        int r = Color.red(backColor);
        int g = Color.green(backColor);
        int b = Color.blue(backColor);
        for (int i = (bw * bh) - 1; i >= 0; i--) {
            int backPixel = backPixels[i];
            backPixels[i] = (((CoverManager.DEFAULT_COLOR & backPixel) | ((((16711680 & backPixel) * r) >>> 8) & 16711680)) | ((((65280 & backPixel) * g) >>> 8) & 65280)) | ((((backPixel & 255) * b) >>> 8) & 255);
        }
        canvas.drawBitmap(backPixels, 0, bw, 0, 0, bw, bh, true, null);
    }

    private static float getHue(int hue, float factor) {
        if (((float) hue) <= SMALLER_HUE) {
            hue = 360 - hue;
        }
        return (((float) hue) * factor) / 360.0f;
    }

    private static float getSaturationFactor(int hue, float factor) {
        if (((float) hue) <= SMALLER_HUE) {
            return 0.5f * factor;
        }
        return factor;
    }

    private static float getBrightnessFactor(int hue, float factor) {
        if (((float) hue) <= SMALLER_HUE) {
            return 0.5f * factor;
        }
        return factor;
    }
}
