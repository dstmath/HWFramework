package com.android.server;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import com.android.server.gesture.GestureNavConst;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import java.lang.reflect.Array;

public class BlurUtils {
    public static Bitmap stackBlur(Bitmap input, int radius) {
        int i;
        int h;
        int i2;
        int pixel = radius;
        Bitmap result = input.copy(Bitmap.Config.ARGB_8888, true);
        if (pixel < 1) {
            return null;
        }
        int w = result.getWidth();
        int pixel2 = result.getHeight();
        int[] pixels = new int[(w * pixel2)];
        result.getPixels(pixels, 0, w, 0, 0, w, pixel2);
        int wm = w - 1;
        int hm = pixel2 - 1;
        int wh = w * pixel2;
        int div = pixel + pixel + 1;
        int[] r = new int[wh];
        int[] g = new int[wh];
        int[] b = new int[wh];
        int[] vmin = new int[Math.max(w, pixel2)];
        int divsum = (div + 1) >> 1;
        int divsum2 = divsum * divsum;
        int[] dv = new int[(256 * divsum2)];
        int i3 = 0;
        while (true) {
            i = i3;
            if (i >= 256 * divsum2) {
                break;
            }
            dv[i] = i / divsum2;
            i3 = i + 1;
            Bitmap bitmap = input;
        }
        int yi = 0;
        int yw = 0;
        int i4 = i;
        int[][] stack = (int[][]) Array.newInstance(int.class, new int[]{div, 3});
        int r1 = pixel + 1;
        int y = 0;
        while (y < pixel2) {
            int i5 = 0;
            int gsum = 0;
            int rsum = 0;
            int boutsum = 0;
            int goutsum = 0;
            int routsum = 0;
            int binsum = 0;
            int ginsum = 0;
            int rinsum = 0;
            int wh2 = wh;
            int i6 = -pixel;
            int bsum = 0;
            while (i6 <= pixel) {
                Bitmap result2 = result;
                int h2 = pixel2;
                int i7 = i5;
                int pixel3 = pixels[yi + Math.min(wm, Math.max(i6, i7))];
                int[] sir = stack[i6 + pixel];
                sir[i7] = (pixel3 & MemoryConstant.LARGE_CPU_MASK) >> 16;
                sir[1] = (pixel3 & 65280) >> 8;
                sir[2] = pixel3 & 255;
                int rbs = r1 - Math.abs(i6);
                rsum += sir[0] * rbs;
                gsum += sir[1] * rbs;
                bsum += sir[2] * rbs;
                if (i6 > 0) {
                    rinsum += sir[0];
                    ginsum += sir[1];
                    binsum += sir[2];
                } else {
                    routsum += sir[0];
                    goutsum += sir[1];
                    boutsum += sir[2];
                }
                i6++;
                result = result2;
                pixel2 = h2;
                i5 = 0;
            }
            Bitmap result3 = result;
            int h3 = pixel2;
            int stackpointer = pixel;
            int x = 0;
            while (x < w) {
                r[yi] = dv[rsum];
                g[yi] = dv[gsum];
                b[yi] = dv[bsum];
                int rsum2 = rsum - routsum;
                int gsum2 = gsum - goutsum;
                int bsum2 = bsum - boutsum;
                int[] sir2 = stack[((stackpointer - pixel) + div) % div];
                int routsum2 = routsum - sir2[0];
                int goutsum2 = goutsum - sir2[1];
                int boutsum2 = boutsum - sir2[2];
                if (y == 0) {
                    i2 = i6;
                    vmin[x] = Math.min(x + pixel + 1, wm);
                } else {
                    i2 = i6;
                }
                int pixel4 = pixels[yw + vmin[x]];
                sir2[0] = (pixel4 & MemoryConstant.LARGE_CPU_MASK) >> 16;
                sir2[1] = (pixel4 & 65280) >> 8;
                int wm2 = wm;
                sir2[2] = pixel4 & 255;
                int rinsum2 = rinsum + sir2[0];
                int ginsum2 = ginsum + sir2[1];
                int binsum2 = binsum + sir2[2];
                rsum = rsum2 + rinsum2;
                gsum = gsum2 + ginsum2;
                bsum = bsum2 + binsum2;
                stackpointer = (stackpointer + 1) % div;
                int[] sir3 = stack[stackpointer % div];
                routsum = routsum2 + sir3[0];
                goutsum = goutsum2 + sir3[1];
                boutsum = boutsum2 + sir3[2];
                rinsum = rinsum2 - sir3[0];
                ginsum = ginsum2 - sir3[1];
                binsum = binsum2 - sir3[2];
                yi++;
                x++;
                i6 = i2;
                wm = wm2;
            }
            int i8 = i6;
            int i9 = wm;
            yw += w;
            y++;
            wh = wh2;
            result = result3;
            pixel2 = h3;
            int bsum3 = i8;
        }
        Bitmap result4 = result;
        int wh3 = wh;
        int h4 = pixel2;
        int i10 = wm;
        int rbs2 = y;
        int x2 = 0;
        while (x2 < w) {
            int goutsum3 = 0;
            int routsum3 = 0;
            int binsum3 = 0;
            int ginsum3 = 0;
            int rinsum3 = 0;
            int i11 = -pixel;
            int gsum3 = 0;
            int rsum3 = 0;
            int boutsum3 = 0;
            int bsum4 = 0;
            int yp = (-pixel) * w;
            while (i11 <= pixel) {
                int y2 = rbs2;
                int yi2 = Math.max(0, yp) + x2;
                int[] sir4 = stack[i11 + pixel];
                sir4[0] = r[yi2];
                sir4[1] = g[yi2];
                sir4[2] = b[yi2];
                int rbs3 = r1 - Math.abs(i11);
                rsum3 += r[yi2] * rbs3;
                gsum3 += g[yi2] * rbs3;
                bsum4 += b[yi2] * rbs3;
                if (i11 > 0) {
                    rinsum3 += sir4[0];
                    ginsum3 += sir4[1];
                    binsum3 += sir4[2];
                } else {
                    routsum3 += sir4[0];
                    goutsum3 += sir4[1];
                    boutsum3 += sir4[2];
                }
                if (i11 < hm) {
                    yp += w;
                }
                i11++;
                rbs2 = y2;
            }
            int y3 = rbs2;
            int yi3 = x2;
            rbs2 = 0;
            int binsum4 = binsum3;
            int stackpointer2 = pixel;
            while (true) {
                int yp2 = yp;
                h = h4;
                if (rbs2 >= h) {
                    break;
                }
                pixels[yi3] = (-16777216 & pixels[yi3]) | (dv[rsum3] << 16) | (dv[gsum3] << 8) | dv[bsum4];
                int rsum4 = rsum3 - routsum3;
                int gsum4 = gsum3 - goutsum3;
                int bsum5 = bsum4 - boutsum3;
                int[] sir5 = stack[((stackpointer2 - pixel) + div) % div];
                int routsum4 = routsum3 - sir5[0];
                int goutsum4 = goutsum3 - sir5[1];
                int boutsum4 = boutsum3 - sir5[2];
                if (x2 == 0) {
                    vmin[rbs2] = Math.min(rbs2 + r1, hm) * w;
                }
                int pixel5 = vmin[rbs2] + x2;
                sir5[0] = r[pixel5];
                sir5[1] = g[pixel5];
                sir5[2] = b[pixel5];
                int rinsum4 = rinsum3 + sir5[0];
                int ginsum4 = ginsum3 + sir5[1];
                int binsum5 = binsum4 + sir5[2];
                rsum3 = rsum4 + rinsum4;
                gsum3 = gsum4 + ginsum4;
                bsum4 = bsum5 + binsum5;
                stackpointer2 = (stackpointer2 + 1) % div;
                int[] sir6 = stack[stackpointer2];
                routsum3 = routsum4 + sir6[0];
                goutsum3 = goutsum4 + sir6[1];
                boutsum3 = boutsum4 + sir6[2];
                rinsum3 = rinsum4 - sir6[0];
                ginsum3 = ginsum4 - sir6[1];
                binsum4 = binsum5 - sir6[2];
                yi3 += w;
                rbs2++;
                h4 = h;
                yp = yp2;
                pixel = radius;
            }
            x2++;
            int gsum5 = i11;
            h4 = h;
            pixel = radius;
        }
        int i12 = rbs2;
        int[] iArr = vmin;
        int[] iArr2 = b;
        int[] iArr3 = g;
        int[] iArr4 = r;
        int i13 = div;
        int i14 = wh3;
        int i15 = hm;
        result4.setPixels(pixels, 0, w, 0, 0, w, h4);
        return result4;
    }

    public static Bitmap blurImage(Context context, Bitmap input, float radius) {
        Bitmap tempInput = Bitmap.createScaledBitmap(input, input.getWidth() / 4, input.getHeight() / 4, false);
        Bitmap result = tempInput.copy(tempInput.getConfig(), true);
        RenderScript rsScript = RenderScript.create(context);
        if (rsScript == null) {
            return null;
        }
        Allocation alloc = Allocation.createFromBitmap(rsScript, tempInput, Allocation.MipmapControl.MIPMAP_NONE, 1);
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
        Bitmap newBitmap = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(), Bitmap.Config.ARGB_8888);
        canvas.setBitmap(newBitmap);
        canvas.drawBitmap(bmp, GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO, GestureNavConst.BOTTOM_WINDOW_SINGLE_HAND_RATIO, paint);
        canvas.drawColor(color);
        return newBitmap;
    }
}
