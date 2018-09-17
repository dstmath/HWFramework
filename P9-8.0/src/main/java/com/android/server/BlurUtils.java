package com.android.server;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.renderscript.Allocation;
import android.renderscript.Allocation.MipmapControl;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import java.lang.reflect.Array;

public class BlurUtils {
    public static Bitmap stackBlur(Bitmap input, int radius) {
        Bitmap result = input.copy(Config.ARGB_8888, true);
        if (radius < 1) {
            return null;
        }
        int i;
        int y;
        int bsum;
        int gsum;
        int rsum;
        int boutsum;
        int goutsum;
        int routsum;
        int binsum;
        int ginsum;
        int rinsum;
        int pixel;
        int[] sir;
        int rbs;
        int stackpointer;
        int x;
        int w = result.getWidth();
        int h = result.getHeight();
        int[] pixels = new int[(w * h)];
        result.getPixels(pixels, 0, w, 0, 0, w, h);
        int wm = w - 1;
        int hm = h - 1;
        int wh = w * h;
        int div = (radius + radius) + 1;
        int[] r = new int[wh];
        int[] g = new int[wh];
        int[] b = new int[wh];
        int[] vmin = new int[Math.max(w, h)];
        int divsum = (div + 1) >> 1;
        divsum *= divsum;
        int[] dv = new int[(divsum * 256)];
        for (i = 0; i < divsum * 256; i++) {
            dv[i] = i / divsum;
        }
        int yi = 0;
        int yw = 0;
        int[][] stack = (int[][]) Array.newInstance(Integer.TYPE, new int[]{div, 3});
        int r1 = radius + 1;
        for (y = 0; y < h; y++) {
            bsum = 0;
            gsum = 0;
            rsum = 0;
            boutsum = 0;
            goutsum = 0;
            routsum = 0;
            binsum = 0;
            ginsum = 0;
            rinsum = 0;
            for (i = -radius; i <= radius; i++) {
                pixel = pixels[Math.min(wm, Math.max(i, 0)) + yi];
                sir = stack[i + radius];
                sir[0] = (MemoryConstant.LARGE_CPU_MASK & pixel) >> 16;
                sir[1] = (65280 & pixel) >> 8;
                sir[2] = pixel & 255;
                rbs = r1 - Math.abs(i);
                rsum += sir[0] * rbs;
                gsum += sir[1] * rbs;
                bsum += sir[2] * rbs;
                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }
            }
            stackpointer = radius;
            for (x = 0; x < w; x++) {
                r[yi] = dv[rsum];
                g[yi] = dv[gsum];
                b[yi] = dv[bsum];
                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;
                sir = stack[((stackpointer - radius) + div) % div];
                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];
                if (y == 0) {
                    vmin[x] = Math.min((x + radius) + 1, wm);
                }
                pixel = pixels[vmin[x] + yw];
                sir[0] = (MemoryConstant.LARGE_CPU_MASK & pixel) >> 16;
                sir[1] = (65280 & pixel) >> 8;
                sir[2] = pixel & 255;
                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];
                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;
                stackpointer = (stackpointer + 1) % div;
                sir = stack[stackpointer % div];
                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];
                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];
                yi++;
            }
            yw += w;
        }
        for (x = 0; x < w; x++) {
            bsum = 0;
            gsum = 0;
            rsum = 0;
            boutsum = 0;
            goutsum = 0;
            routsum = 0;
            binsum = 0;
            ginsum = 0;
            rinsum = 0;
            int yp = (-radius) * w;
            for (i = -radius; i <= radius; i++) {
                yi = Math.max(0, yp) + x;
                sir = stack[i + radius];
                sir[0] = r[yi];
                sir[1] = g[yi];
                sir[2] = b[yi];
                rbs = r1 - Math.abs(i);
                rsum += r[yi] * rbs;
                gsum += g[yi] * rbs;
                bsum += b[yi] * rbs;
                if (i > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }
                if (i < hm) {
                    yp += w;
                }
            }
            yi = x;
            stackpointer = radius;
            for (y = 0; y < h; y++) {
                pixels[yi] = (((pixels[yi] & -16777216) | (dv[rsum] << 16)) | (dv[gsum] << 8)) | dv[bsum];
                rsum -= routsum;
                gsum -= goutsum;
                bsum -= boutsum;
                sir = stack[((stackpointer - radius) + div) % div];
                routsum -= sir[0];
                goutsum -= sir[1];
                boutsum -= sir[2];
                if (x == 0) {
                    vmin[y] = Math.min(y + r1, hm) * w;
                }
                pixel = x + vmin[y];
                sir[0] = r[pixel];
                sir[1] = g[pixel];
                sir[2] = b[pixel];
                rinsum += sir[0];
                ginsum += sir[1];
                binsum += sir[2];
                rsum += rinsum;
                gsum += ginsum;
                bsum += binsum;
                stackpointer = (stackpointer + 1) % div;
                sir = stack[stackpointer];
                routsum += sir[0];
                goutsum += sir[1];
                boutsum += sir[2];
                rinsum -= sir[0];
                ginsum -= sir[1];
                binsum -= sir[2];
                yi += w;
            }
        }
        result.setPixels(pixels, 0, w, 0, 0, w, h);
        return result;
    }

    public static Bitmap blurImage(Context context, Bitmap input, float radius) {
        Bitmap tempInput = Bitmap.createScaledBitmap(input, input.getWidth() / 4, input.getHeight() / 4, false);
        Bitmap result = tempInput.copy(tempInput.getConfig(), true);
        RenderScript rsScript = RenderScript.create(context);
        if (rsScript == null) {
            return null;
        }
        Allocation alloc = Allocation.createFromBitmap(rsScript, tempInput, MipmapControl.MIPMAP_NONE, 1);
        Allocation outAlloc = Allocation.createTyped(rsScript, alloc.getType());
        ScriptIntrinsicBlur blur = ScriptIntrinsicBlur.create(rsScript, Element.U8_4(rsScript));
        blur.setRadius(radius);
        blur.setInput(alloc);
        blur.forEach(outAlloc);
        outAlloc.copyTo(result);
        rsScript.destroy();
        return Bitmap.createScaledBitmap(result, input.getWidth(), input.getHeight(), false);
    }

    public static Bitmap addBlackBoard(Bitmap bmp, int color) {
        Canvas canvas = new Canvas();
        Paint paint = new Paint();
        Bitmap newBitmap = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(), Config.ARGB_8888);
        canvas.setBitmap(newBitmap);
        canvas.drawBitmap(bmp, 0.0f, 0.0f, paint);
        canvas.drawColor(color);
        return newBitmap;
    }
}
