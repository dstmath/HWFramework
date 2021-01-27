package huawei.android.hwutil;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.Log;

public class IconBitmapUtils {
    private static final float HFACTOR = 0.9f;
    private static final boolean IS_DEBUG_ICON = false;
    private static final int MASK_ABS_VALID_RANGE = 10;
    private static final float SFACTOR = 0.8f;
    private static final float SMALLER_HUE = 30.0f;
    private static final String TAG = "IconBitmapUtils";
    private static final String TAG_ICON = "ResourcesEx";
    private static final int VALID_TRANSPARENCY = 40;
    private static final float VFACTOR = 1.2f;

    public static void mask(int[] pixels, int width, int height, Bitmap mask) {
        if (mask != null) {
            int maskWidth = mask.getWidth();
            int maskHeight = mask.getHeight();
            if (maskWidth < width || maskHeight < height) {
                Log.w(TAG, "Mask size error, maskWidth =" + maskWidth + ",maskHeight =" + maskHeight + ",width =" + width + ",height =" + height);
                return;
            }
            int[] maskPixels = new int[(width * height)];
            mask.getPixels(maskPixels, 0, width, (maskWidth - width) / 2, (maskWidth - height) / 2, width, height);
            for (int i = (width * height) - 1; i >= 0; i--) {
                pixels[i] = ((((pixels[i] >>> 24) * (maskPixels[i] >>> 24)) / 255) << 24) | (pixels[i] & 16777215);
            }
        }
    }

    public static Bitmap mask(Bitmap bmpSrc, Bitmap bmpMask) {
        int srcStartX;
        int srcStartY;
        int height;
        if (bmpSrc == null || bmpMask == null) {
            return null;
        }
        Bitmap newBitMap = bmpMask.copy(Bitmap.Config.ARGB_8888, true);
        try {
            int width = bmpSrc.getWidth();
            int height2 = bmpSrc.getHeight();
            int maskWidth = newBitMap.getWidth();
            int maskHeight = newBitMap.getHeight();
            if (width > maskWidth) {
                width = maskWidth;
                srcStartX = (width - maskWidth) / 2;
            } else {
                srcStartX = 0;
            }
            if (height2 > maskHeight) {
                height = maskHeight;
                srcStartY = (height2 - maskHeight) / 2;
            } else {
                height = height2;
                srcStartY = 0;
            }
            int[] srcPixels = new int[(width * height)];
            int[] maskPixels = new int[(width * height)];
            bmpSrc.getPixels(srcPixels, 0, width, srcStartX, srcStartY, width, height);
            newBitMap.getPixels(maskPixels, 0, width, (maskWidth - width) / 2, (maskHeight - height) / 2, width, height);
            for (int i = (width * height) - 1; i >= 0; i--) {
                maskPixels[i] = ((((maskPixels[i] >>> 24) * (srcPixels[i] >>> 24)) / 255) << 24) | (srcPixels[i] & 16777215);
            }
            newBitMap.setPixels(maskPixels, 0, width, (maskWidth - width) / 2, (maskHeight - height) / 2, width, height);
            int[] mask = new int[(maskWidth * maskHeight)];
            newBitMap.getPixels(mask, 0, maskWidth, 0, 0, maskWidth, maskHeight);
            setTransparent(mask, maskWidth, 0, 0, maskWidth, (maskHeight - height) / 2);
            setTransparent(mask, maskWidth, 0, (maskHeight + height) / 2, maskWidth, maskHeight);
            setTransparent(mask, maskWidth, 0, 0, (maskWidth - width) / 2, maskHeight);
            setTransparent(mask, maskWidth, (maskWidth + width) / 2, 0, maskWidth, maskHeight);
            newBitMap.setPixels(mask, 0, maskWidth, 0, 0, maskWidth, maskHeight);
            return newBitMap;
        } catch (IllegalArgumentException e) {
            if (newBitMap != null) {
                newBitMap.recycle();
            }
            return null;
        } catch (RuntimeException e2) {
            if (newBitMap != null) {
                newBitMap.recycle();
            }
            return null;
        }
    }

    public static Bitmap overlap2Bitmap(Bitmap bmpSrc, Bitmap bmpBg, int deltaX, int deltaY) {
        if (bmpSrc == null || bmpBg == null) {
            return null;
        }
        int width = bmpSrc.getWidth();
        int height = bmpSrc.getHeight();
        int bgWidth = bmpBg.getWidth();
        int bgHeight = bmpBg.getHeight();
        if (width > bgWidth || height > bgHeight) {
            Log.e(TAG, "The size of background(" + bgWidth + "," + bgHeight + ") little than the source bitmap(" + width + "," + height + ")");
            return null;
        }
        Canvas canvas = new Canvas();
        Paint paint = new Paint();
        Bitmap newBitmap = Bitmap.createBitmap(bgWidth, bgHeight, Bitmap.Config.ARGB_8888);
        canvas.setBitmap(newBitmap);
        canvas.drawBitmap(bmpBg, 0.0f, 0.0f, paint);
        canvas.drawBitmap(bmpSrc, (float) (((bgWidth - width) >> 1) + deltaX), (float) (((bgHeight - height) >> 1) + deltaY), paint);
        return newBitmap;
    }

    public static Bitmap overlap2Bitmap(Bitmap bmpSrc, Bitmap bmpBg) {
        return overlap2Bitmap(bmpSrc, bmpBg, 0, 0);
    }

    public static Bitmap zoomIfNeed(Bitmap bmp, int standardSize, boolean isRecycledWhenScaled) {
        if (bmp == null) {
            Log.e(TAG, "Try to Zoom a null bitmap");
            return null;
        }
        int width = bmp.getWidth();
        int height = bmp.getHeight();
        if (width == standardSize && height == standardSize) {
            return bmp;
        }
        Bitmap tmpBitmap = Bitmap.createScaledBitmap(bmp, standardSize, standardSize, true);
        if (isRecycledWhenScaled) {
            bmp.recycle();
        }
        return tmpBitmap;
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        if (drawable == null) {
            Log.e(TAG, "Try to convert a null drawable");
            return null;
        } else if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable) drawable).getBitmap();
        } else {
            try {
                Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
                Canvas canvas = new Canvas(bitmap);
                drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                drawable.draw(canvas);
                return bitmap;
            } catch (IllegalArgumentException e) {
                return null;
            } catch (Exception e2) {
                return null;
            }
        }
    }

    public static Rect getIconInfo(Bitmap bmp) {
        if (bmp == null) {
            Log.e(TAG, "The given bitmap is null");
            return null;
        }
        int width = bmp.getWidth();
        int height = bmp.getHeight();
        int[] pixels = new int[(width * height)];
        bmp.getPixels(pixels, 0, width, 0, 0, width, height);
        int top = getValidTop(pixels, width, height);
        int bottom = getValidBottom(pixels, width, height);
        int left = getValidLeft(pixels, width, height);
        int right = getValidRight(pixels, width, height);
        if (left >= right || top >= bottom) {
            return null;
        }
        return new Rect(left, top, right, bottom);
    }

    public static Bitmap composeIcon(Bitmap src, Bitmap mask, Bitmap background, Bitmap border, boolean isUseAvgColor) {
        if (src == null) {
            Log.e(TAG, "ComposeIcon error, The original bitmap is null");
            return null;
        }
        int width = src.getWidth();
        int height = src.getHeight();
        int[] pixels = new int[(width * height)];
        src.getPixels(pixels, 0, width, 0, 0, width, height);
        Bitmap resultBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        if (resultBitmap != null) {
            resultBitmap.setDensity(src.getDensity());
        }
        Canvas canvas = new Canvas(resultBitmap);
        if (mask != null) {
            if (mask.getDensity() != src.getDensity()) {
                mask.setDensity(src.getDensity());
            }
            mask(pixels, width, height, mask);
        }
        if (background != null) {
            if (background.getDensity() != src.getDensity()) {
                background.setDensity(src.getDensity());
            }
            canvas.drawBitmap(background, 0.0f, 0.0f, (Paint) null);
        }
        canvas.drawBitmap(pixels, 0, width, 0, 0, width, height, true, (Paint) null);
        if (border != null) {
            if (border.getDensity() != src.getDensity()) {
                border.setDensity(src.getDensity());
            }
            canvas.drawBitmap(border, 0.0f, 0.0f, new Paint());
        }
        src.recycle();
        return resultBitmap;
    }

    public static Bitmap drawSource(Bitmap bmp, int standardSize, int iconSize) {
        if (bmp == null) {
            Log.e(TAG, "drawSource error, The original bitmap is null");
            return null;
        }
        Canvas canvas = new Canvas();
        Bitmap resultBitmap = Bitmap.createBitmap(standardSize, standardSize, Bitmap.Config.ARGB_8888);
        if (resultBitmap != null) {
            resultBitmap.setDensity(bmp.getDensity());
        }
        canvas.setBitmap(resultBitmap);
        Bitmap tmp = Bitmap.createScaledBitmap(bmp, iconSize, iconSize, true);
        canvas.translate(((float) (standardSize - iconSize)) / 2.0f, ((float) (standardSize - iconSize)) / 2.0f);
        canvas.drawBitmap(tmp, 0.0f, 0.0f, new Paint());
        if (tmp != bmp) {
            tmp.recycle();
        }
        return resultBitmap;
    }

    private static int getAvgColor(int[] tempPixels, int width, int height) {
        int count = 0;
        int avgRed = 0;
        int avgGreen = 0;
        int avgBlue = 0;
        for (int j = (width * height) - 1; j >= 0; j--) {
            int color = tempPixels[j] & 16777215;
            if (color > 0) {
                avgRed += Color.red(color);
                avgGreen += Color.green(color);
                avgBlue += Color.blue(color);
                count++;
            }
        }
        if (count > 0) {
            avgRed /= count;
            avgGreen /= count;
            avgBlue /= count;
        }
        return Color.rgb(avgRed, avgGreen, avgBlue);
    }

    private static void drawBackground(Canvas canvas, Bitmap background, int width, int height, int[] pixels) {
        int avgColor = getAvgColor(pixels, width, height);
        int bgWidth = background.getWidth();
        int bgHeight = background.getHeight();
        int[] backPixels = new int[(bgWidth * bgHeight)];
        background.getPixels(backPixels, 0, bgWidth, 0, 0, bgWidth, bgHeight);
        float[] hsv = new float[3];
        Color.colorToHSV(avgColor, hsv);
        int hue = (int) hsv[0];
        float saturation = hsv[1];
        float brightness = hsv[2];
        hsv[0] = getHue(hue, HFACTOR);
        hsv[1] = getSaturationFactor(hue, SFACTOR) * saturation;
        hsv[2] = getBrightnessFactor(hue, VFACTOR) * brightness;
        int backColor = Color.HSVToColor(hsv);
        int red = Color.red(backColor);
        int green = Color.green(backColor);
        int blue = Color.blue(backColor);
        for (int i = (bgWidth * bgHeight) - 1; i >= 0; i--) {
            int backPixel = backPixels[i];
            backPixels[i] = (-16777216 & backPixel) | (16711680 & (((backPixel & 16711680) * red) >>> 8)) | (65280 & (((backPixel & 65280) * green) >>> 8)) | ((((backPixel & 255) * blue) >>> 8) & 255);
        }
        canvas.drawBitmap(backPixels, 0, bgWidth, 0, 0, bgWidth, bgHeight, true, (Paint) null);
    }

    private static float getHue(int hue, float factor) {
        return (((float) (((float) hue) <= SMALLER_HUE ? 360 - hue : hue)) * factor) / 360.0f;
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

    private static void setTransparent(int[] pixels, int width, int left, int top, int right, int bottom) {
        for (int i = top; i < bottom; i++) {
            for (int j = left; j < right; j++) {
                int pos = (i * width) + j;
                pixels[pos] = pixels[pos] & 16777215;
            }
        }
    }

    private static boolean checkTransparency(int argb) {
        return (argb >>> 24) >= VALID_TRANSPARENCY;
    }

    private static int getValidTop(int[] pixels, int width, int height) {
        int midWidth = width % 2 == 0 ? (width / 2) - 1 : width / 2;
        for (int i = 0; i < height; i++) {
            int y = i * width;
            for (int j = midWidth; j >= 0; j--) {
                if (checkTransparency(pixels[y + j]) || checkTransparency(pixels[((width - 1) - j) + y])) {
                    return i;
                }
            }
        }
        return height;
    }

    private static int getValidBottom(int[] pixels, int width, int height) {
        int midWidth = width % 2 == 0 ? (width / 2) - 1 : width / 2;
        for (int i = height - 1; i >= 0; i--) {
            int y = i * width;
            for (int j = midWidth; j >= 0; j--) {
                if (checkTransparency(pixels[y + j]) || checkTransparency(pixels[((width - 1) - j) + y])) {
                    return i;
                }
            }
        }
        return 0;
    }

    private static int getValidLeft(int[] pixels, int width, int height) {
        int midHeight = height % 2 == 0 ? (height / 2) - 1 : height / 2;
        for (int i = 0; i < width; i++) {
            for (int j = midHeight; j >= 0; j--) {
                if (checkTransparency(pixels[(j * width) + i]) || checkTransparency(pixels[(((height - 1) - j) * width) + i])) {
                    return i;
                }
            }
        }
        return width;
    }

    private static int getValidRight(int[] pixels, int width, int height) {
        int midHeight = height % 2 == 0 ? (height / 2) - 1 : height / 2;
        for (int i = width - 1; i >= 0; i--) {
            for (int j = midHeight; j >= 0; j--) {
                if (checkTransparency(pixels[(j * width) + i]) || checkTransparency(pixels[(((height - 1) - j) * width) + i])) {
                    return i;
                }
            }
        }
        return 0;
    }
}
