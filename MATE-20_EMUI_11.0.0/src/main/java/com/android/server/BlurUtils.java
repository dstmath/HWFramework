package com.android.server;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import java.lang.reflect.Array;

public class BlurUtils {
    private BlurUtils() {
    }

    public static Bitmap stackBlur(Bitmap input, int radius) {
        int index;
        int pixel = radius;
        Bitmap result = input.copy(Bitmap.Config.ARGB_8888, true);
        if (pixel < 1) {
            return null;
        }
        int width = result.getWidth();
        int pixel2 = result.getHeight();
        int[] pixels = new int[(width * pixel2)];
        result.getPixels(pixels, 0, width, 0, 0, width, pixel2);
        int widthLess = width - 1;
        int heightLess = pixel2 - 1;
        int totalPixels = width * pixel2;
        int div = pixel + pixel + 1;
        int[] reds = new int[totalPixels];
        int[] greens = new int[totalPixels];
        int[] blues = new int[totalPixels];
        int[] valMins = new int[Math.max(width, pixel2)];
        int divSum = (div + 1) >> 1;
        int divSum2 = divSum * divSum;
        int allResult = divSum2 * 256;
        int[] divResults = new int[allResult];
        int index2 = 0;
        while (index2 < allResult) {
            divResults[index2] = index2 / divSum2;
            index2++;
        }
        int column = 0;
        int row = 0;
        int[][] resultStack = (int[][]) Array.newInstance(int.class, div, 3);
        int radiusMore = pixel + 1;
        int coordinateY = 0;
        while (coordinateY < pixel2) {
            int rinSum = 0;
            int ginSum = 0;
            int binSum = 0;
            int routSum = 0;
            int goutSum = 0;
            int boutSum = 0;
            int redSum = 0;
            int greenSum = 0;
            int blueSum = 0;
            index2 = -pixel;
            while (index2 <= pixel) {
                int pixel3 = pixels[row + Math.min(widthLess, Math.max(index2, 0))];
                int[] rgbColors = resultStack[index2 + pixel];
                rgbColors[0] = (pixel3 & 16711680) >> 16;
                rgbColors[1] = (pixel3 & 65280) >> 8;
                rgbColors[2] = pixel3 & 255;
                int radiusAbsolute = radiusMore - Math.abs(index2);
                redSum += rgbColors[0] * radiusAbsolute;
                greenSum += rgbColors[1] * radiusAbsolute;
                blueSum += rgbColors[2] * radiusAbsolute;
                if (index2 > 0) {
                    rinSum += rgbColors[0];
                    ginSum += rgbColors[1];
                    binSum += rgbColors[2];
                } else {
                    routSum += rgbColors[0];
                    goutSum += rgbColors[1];
                    boutSum += rgbColors[2];
                }
                index2++;
                result = result;
                pixel2 = pixel2;
            }
            int stackPointer = radius;
            int coordinateX = 0;
            while (coordinateX < width) {
                reds[row] = divResults[redSum];
                greens[row] = divResults[greenSum];
                blues[row] = divResults[blueSum];
                int redSum2 = redSum - routSum;
                int greenSum2 = greenSum - goutSum;
                int blueSum2 = blueSum - boutSum;
                int[] rgbColors2 = resultStack[((stackPointer - pixel) + div) % div];
                int routSum2 = routSum - rgbColors2[0];
                int goutSum2 = goutSum - rgbColors2[1];
                int boutSum2 = boutSum - rgbColors2[2];
                if (coordinateY == 0) {
                    index = index2;
                    valMins[coordinateX] = Math.min(coordinateX + pixel + 1, widthLess);
                } else {
                    index = index2;
                }
                int pixel4 = pixels[column + valMins[coordinateX]];
                rgbColors2[0] = (pixel4 & 16711680) >> 16;
                rgbColors2[1] = (pixel4 & 65280) >> 8;
                rgbColors2[2] = pixel4 & 255;
                int rinSum2 = rinSum + rgbColors2[0];
                int ginSum2 = ginSum + rgbColors2[1];
                int binSum2 = binSum + rgbColors2[2];
                redSum = redSum2 + rinSum2;
                greenSum = greenSum2 + ginSum2;
                blueSum = blueSum2 + binSum2;
                stackPointer = (stackPointer + 1) % div;
                int[] rgbColors3 = resultStack[stackPointer % div];
                routSum = routSum2 + rgbColors3[0];
                goutSum = goutSum2 + rgbColors3[1];
                boutSum = boutSum2 + rgbColors3[2];
                rinSum = rinSum2 - rgbColors3[0];
                ginSum = ginSum2 - rgbColors3[1];
                binSum = binSum2 - rgbColors3[2];
                row++;
                coordinateX++;
                widthLess = widthLess;
                index2 = index;
            }
            column += width;
            coordinateY++;
            result = result;
            pixel2 = pixel2;
        }
        int height = pixel2;
        int coordinateX2 = 0;
        int radiusAbsolute2 = coordinateY;
        while (coordinateX2 < width) {
            int routSum3 = 0;
            int goutSum3 = 0;
            int boutSum3 = 0;
            int redSum3 = 0;
            int greenSum3 = 0;
            int blueSum3 = 0;
            int rinSum3 = 0;
            int index3 = -pixel;
            int binSum3 = 0;
            int ginSum3 = 0;
            int divRow = (-pixel) * width;
            while (index3 <= pixel) {
                int row2 = Math.max(0, divRow) + coordinateX2;
                int[] rgbColors4 = resultStack[index3 + pixel];
                rgbColors4[0] = reds[row2];
                rgbColors4[1] = greens[row2];
                rgbColors4[2] = blues[row2];
                int radiusAbsolute3 = radiusMore - Math.abs(index3);
                redSum3 += reds[row2] * radiusAbsolute3;
                greenSum3 += greens[row2] * radiusAbsolute3;
                blueSum3 += blues[row2] * radiusAbsolute3;
                if (index3 > 0) {
                    rinSum3 += rgbColors4[0];
                    ginSum3 += rgbColors4[1];
                    binSum3 += rgbColors4[2];
                } else {
                    routSum3 += rgbColors4[0];
                    goutSum3 += rgbColors4[1];
                    boutSum3 += rgbColors4[2];
                }
                if (index3 < heightLess) {
                    divRow += width;
                }
                index3++;
                radiusAbsolute2 = radiusAbsolute2;
            }
            int stackPointer2 = radius;
            int row3 = coordinateX2;
            radiusAbsolute2 = 0;
            while (radiusAbsolute2 < height) {
                pixels[row3] = (pixels[row3] & -16777216) | (divResults[redSum3] << 16) | (divResults[greenSum3] << 8) | divResults[blueSum3];
                int redSum4 = redSum3 - routSum3;
                int greenSum4 = greenSum3 - goutSum3;
                int blueSum4 = blueSum3 - boutSum3;
                int[] rgbColors5 = resultStack[((stackPointer2 - pixel) + div) % div];
                int routSum4 = routSum3 - rgbColors5[0];
                int goutSum4 = goutSum3 - rgbColors5[1];
                int boutSum4 = boutSum3 - rgbColors5[2];
                if (coordinateX2 == 0) {
                    valMins[radiusAbsolute2] = Math.min(radiusAbsolute2 + radiusMore, heightLess) * width;
                }
                int pixel5 = valMins[radiusAbsolute2] + coordinateX2;
                rgbColors5[0] = reds[pixel5];
                rgbColors5[1] = greens[pixel5];
                rgbColors5[2] = blues[pixel5];
                int rinSum4 = rinSum3 + rgbColors5[0];
                int ginSum4 = ginSum3 + rgbColors5[1];
                int binSum4 = binSum3 + rgbColors5[2];
                redSum3 = redSum4 + rinSum4;
                greenSum3 = greenSum4 + ginSum4;
                blueSum3 = blueSum4 + binSum4;
                stackPointer2 = (stackPointer2 + 1) % div;
                int[] rgbColors6 = resultStack[stackPointer2];
                routSum3 = routSum4 + rgbColors6[0];
                goutSum3 = goutSum4 + rgbColors6[1];
                boutSum3 = boutSum4 + rgbColors6[2];
                rinSum3 = rinSum4 - rgbColors6[0];
                ginSum3 = ginSum4 - rgbColors6[1];
                binSum3 = binSum4 - rgbColors6[2];
                row3 += width;
                radiusAbsolute2++;
                pixel = radius;
                height = height;
                divRow = divRow;
            }
            coordinateX2++;
            pixel = radius;
            height = height;
        }
        result.setPixels(pixels, 0, width, 0, 0, width, height);
        return result;
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
        canvas.drawBitmap(bmp, 0.0f, 0.0f, paint);
        canvas.drawColor(color);
        return newBitmap;
    }
}
