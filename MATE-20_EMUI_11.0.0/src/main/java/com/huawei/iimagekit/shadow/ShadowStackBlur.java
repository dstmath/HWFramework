package com.huawei.iimagekit.shadow;

import android.graphics.Bitmap;
import com.huawei.internal.widget.ConstantValues;

public class ShadowStackBlur {
    static final int A_BIT_POS = 24;
    static final int G_BIT_POS = 8;
    static final int R_BIT_POS = 16;
    private static final short[] STACK_BLUR_MUL = {512, 512, 456, 512, 328, 456, 335, 512, 405, 328, 271, 456, 388, 335, 292, 512, 454, 405, 364, 328, 298, 271, 496, 456, 420, 388, 360, 335, 312, 292, 273, 512, 482, 454, 428, 405, 383, 364, 345, 328, 312, 298, 284, 271, 259, 496, 475, 456, 437, 420, 404, 388, 374, 360, 347, 335, 323, 312, 302, 292, 282, 273, 265, 512, 497, 482, 468, 454, 441, 428, 417, 405, 394, 383, 373, 364, 354, 345, 337, 328, 320, 312, 305, 298, 291, 284, 278, 271, 265, 259, 507, 496, 485, 475, 465, 456, 446, 437, 428, 420, 412, 404, 396, 388, 381, 374, 367, 360, 354, 347, 341, 335, 329, 323, 318, 312, 307, 302, 297, 292, 287, 282, 278, 273, 269, 265, 261, 512, 505, 497, 489, 482, 475, 468, 461, 454, 447, 441, 435, 428, 422, 417, 411, 405, 399, 394, 389, 383, 378, 373, 368, 364, 359, 354, 350, 345, 341, 337, 332, 328, 324, 320, 316, 312, 309, 305, 301, 298, 294, 291, 287, 284, 281, 278, 274, 271, 268, 265, 262, 259, 257, 507, 501, 496, 491, 485, 480, 475, 470, 465, 460, 456, 451, 446, 442, 437, 433, 428, 424, 420, 416, 412, 408, 404, 400, 396, 392, 388, 385, 381, 377, 374, 370, 367, 363, 360, 357, 354, 350, 347, 344, 341, 338, 335, 332, 329, 326, 323, 320, 318, 315, 312, 310, 307, 304, 302, 299, 297, 294, 292, 289, 287, 285, 282, 280, 278, 275, 273, 271, 269, 267, 265, 263, 261, 259};
    private static final byte[] STACK_BLUR_SHR = {9, 11, 12, 13, 13, 14, 14, 15, 15, 15, 15, 16, 16, 16, 16, 17, 17, 17, 17, 17, 17, 17, 18, 18, 18, 18, 18, 18, 18, 18, 18, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24};

    public static void doBlur(Bitmap bitmapForBlur, Bitmap blurredBitmap, int radius) {
        int width = bitmapForBlur.getWidth();
        int height = bitmapForBlur.getHeight();
        if (width > 0 && height > 0) {
            int[] pixels = new int[(width * height)];
            bitmapForBlur.getPixels(pixels, 0, width, 0, 0, width, height);
            ShadowUtil.processAlphaChannelBefore(pixels);
            stackBlur(pixels, width, height, radius);
            ShadowUtil.processAlphaChannelAfter(pixels);
            blurredBitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        }
    }

    /* JADX INFO: Multiple debug info for r7v2 int: [D('blue' int), D('wm' int)] */
    /* JADX INFO: Multiple debug info for r8v6 int: [D('yp' int), D('k' int)] */
    /* JADX INFO: Multiple debug info for r7v12 int: [D('blue' int), D('tmpG' int)] */
    /* JADX INFO: Multiple debug info for r6v16 int: [D('blue' int), D('green' int)] */
    /* JADX INFO: Multiple debug info for r9v41 int: [D('rgb' int), D('sumR' long)] */
    /* JADX INFO: Multiple debug info for r13v2 int: [D('blue' int), D('sumRo' long)] */
    /* JADX INFO: Multiple debug info for r4v15 int: [D('i' int), D('xp' int)] */
    /* JADX INFO: Multiple debug info for r0v20 int: [D('rgb' int), D('tmpB' int)] */
    /* JADX INFO: Multiple debug info for r11v11 int: [D('tmpR' int), D('blue' int)] */
    /* JADX INFO: Multiple debug info for r3v7 int: [D('blue' int), D('green' int)] */
    private static void stackBlur(int[] src, int width, int height, int radius) {
        int srcIndex;
        int rgb = width;
        int i = height;
        int green = radius;
        short mulSum = STACK_BLUR_MUL[green];
        short shrSum = (short) STACK_BLUR_SHR[green];
        int div = green + green + 1;
        int[] stack = new int[div];
        int wm = rgb - 1;
        int j = 0;
        while (j < i) {
            long sumG = 0;
            long sumGi = 0;
            long sumGo = 0;
            long sumB = 0;
            long sumBi = 0;
            long sumBo = 0;
            long sumA = 0;
            long sumAi = 0;
            long sumAo = 0;
            int srcIndex2 = rgb * j;
            long sumR = 0;
            int rgb2 = src[srcIndex2];
            int alpha = (rgb2 >>> 24) & ConstantValues.MAX_CHANNEL_VALUE;
            long sumRi = 0;
            int red = (rgb2 >>> 16) & ConstantValues.MAX_CHANNEL_VALUE;
            int green2 = (rgb2 >>> 8) & ConstantValues.MAX_CHANNEL_VALUE;
            long sumRo = 0;
            int blue = rgb2 & ConstantValues.MAX_CHANNEL_VALUE;
            int i2 = 0;
            while (i2 <= green) {
                stack[i2] = rgb2;
                sumA += (long) (alpha * (i2 + 1));
                sumR += (long) ((i2 + 1) * red);
                sumG += (long) ((i2 + 1) * green2);
                sumB += (long) ((i2 + 1) * blue);
                sumAo += (long) alpha;
                sumRo += (long) red;
                sumGo += (long) green2;
                sumBo += (long) blue;
                i2++;
                shrSum = shrSum;
                rgb2 = rgb2;
                div = div;
            }
            int div2 = div;
            int i3 = 1;
            while (i3 <= green) {
                if (i3 <= wm) {
                    srcIndex2++;
                }
                int rgb3 = src[srcIndex2];
                alpha = (rgb3 >>> 24) & ConstantValues.MAX_CHANNEL_VALUE;
                red = (rgb3 >>> 16) & ConstantValues.MAX_CHANNEL_VALUE;
                green2 = (rgb3 >>> 8) & ConstantValues.MAX_CHANNEL_VALUE;
                blue = rgb3 & ConstantValues.MAX_CHANNEL_VALUE;
                stack[i3 + green] = rgb3;
                sumA += (long) (((green + 1) - i3) * alpha);
                sumR += (long) (((green + 1) - i3) * red);
                sumG += (long) (((green + 1) - i3) * green2);
                sumB += (long) (((green + 1) - i3) * blue);
                sumAi += (long) alpha;
                sumRi += (long) red;
                sumGi += (long) green2;
                sumBi += (long) blue;
                i3++;
                stack = stack;
            }
            int xp = radius;
            if (xp > wm) {
                xp = wm;
            }
            int srcIndex3 = (j * rgb) + xp;
            int xp2 = xp;
            int i4 = 0;
            int sp = radius;
            int srcIndex4 = j * rgb;
            while (i4 < rgb) {
                src[srcIndex4] = ((((int) ((((long) mulSum) * sumA) >>> shrSum)) & ConstantValues.MAX_CHANNEL_VALUE) << 24) | ((((int) ((((long) mulSum) * sumR) >>> shrSum)) & ConstantValues.MAX_CHANNEL_VALUE) << 16) | ((((int) ((((long) mulSum) * sumG) >>> shrSum)) & ConstantValues.MAX_CHANNEL_VALUE) << 8) | (((int) ((((long) mulSum) * sumB) >>> shrSum)) & ConstantValues.MAX_CHANNEL_VALUE);
                int dsti = srcIndex4 + 1;
                long sumA2 = sumA - sumAo;
                long sumR2 = sumR - sumRo;
                long sumG2 = sumG - sumGo;
                long sumB2 = sumB - sumBo;
                int stackStart = (sp + div2) - green;
                if (stackStart >= div2) {
                    stackStart -= div2;
                }
                int tmpB = stack[stackStart];
                int alpha2 = (tmpB >>> 24) & ConstantValues.MAX_CHANNEL_VALUE;
                long sumAo2 = sumAo - ((long) alpha2);
                long sumRo2 = sumRo - ((long) ((tmpB >>> 16) & ConstantValues.MAX_CHANNEL_VALUE));
                long sumGo2 = sumGo - ((long) ((tmpB >>> 8) & ConstantValues.MAX_CHANNEL_VALUE));
                long sumBo2 = sumBo - ((long) (tmpB & ConstantValues.MAX_CHANNEL_VALUE));
                if (xp2 < wm) {
                    srcIndex3++;
                    xp2++;
                }
                int rgb4 = src[srcIndex3];
                int alpha3 = (rgb4 >>> 24) & ConstantValues.MAX_CHANNEL_VALUE;
                int red2 = (rgb4 >>> 16) & ConstantValues.MAX_CHANNEL_VALUE;
                int green3 = (rgb4 >>> 8) & ConstantValues.MAX_CHANNEL_VALUE;
                int blue2 = rgb4 & ConstantValues.MAX_CHANNEL_VALUE;
                stack[stackStart] = rgb4;
                long sumAi2 = sumAi + ((long) alpha3);
                long sumRi2 = sumRi + ((long) red2);
                long sumGi2 = sumGi + ((long) green3);
                long sumBi2 = sumBi + ((long) blue2);
                sumA = sumA2 + sumAi2;
                sumR = sumR2 + sumRi2;
                sumG = sumG2 + sumGi2;
                sumB = sumB2 + sumBi2;
                int sp2 = sp + 1;
                if (sp2 >= div2) {
                    sp2 = 0;
                }
                sp = sp2;
                int rgb5 = stack[sp];
                int alpha4 = (rgb5 >>> 24) & ConstantValues.MAX_CHANNEL_VALUE;
                red = (rgb5 >>> 16) & ConstantValues.MAX_CHANNEL_VALUE;
                int green4 = (rgb5 >>> 8) & ConstantValues.MAX_CHANNEL_VALUE;
                int green5 = rgb5 & ConstantValues.MAX_CHANNEL_VALUE;
                sumAo = sumAo2 + ((long) alpha4);
                sumRo = sumRo2 + ((long) red);
                sumGo = sumGo2 + ((long) green4);
                sumBo = sumBo2 + ((long) green5);
                sumAi = sumAi2 - ((long) alpha4);
                sumRi = sumRi2 - ((long) red);
                sumGi = sumGi2 - ((long) green4);
                sumBi = sumBi2 - ((long) green5);
                i4++;
                alpha = alpha4;
                green2 = green4;
                srcIndex3 = srcIndex3;
                div2 = div2;
                xp2 = xp2;
                srcIndex4 = dsti;
                green = radius;
                blue = green5;
                mulSum = mulSum;
                rgb = width;
            }
            j++;
            rgb = width;
            i = height;
            green = radius;
            div = div2;
            stack = stack;
            shrSum = shrSum;
        }
        short mulSum2 = mulSum;
        int alpha5 = height;
        int red3 = alpha5 - 1;
        int i5 = 0;
        while (i5 < width) {
            long sumAi3 = 0;
            long sumR3 = 0;
            long sumRi3 = 0;
            long sumG3 = 0;
            long sumGi3 = 0;
            long sumB3 = 0;
            long sumBi3 = 0;
            int rgb6 = src[i5];
            long sumA3 = 0;
            int alpha6 = (rgb6 >>> 24) & ConstantValues.MAX_CHANNEL_VALUE;
            int red4 = (rgb6 >>> 16) & ConstantValues.MAX_CHANNEL_VALUE;
            int srcIndex5 = i5;
            int green6 = (rgb6 >>> 8) & ConstantValues.MAX_CHANNEL_VALUE;
            int blue3 = rgb6 & ConstantValues.MAX_CHANNEL_VALUE;
            int k = 0;
            long sumBo3 = 0;
            long sumGo3 = 0;
            long sumRo3 = 0;
            long sumAo3 = 0;
            while (k <= radius) {
                stack[k] = rgb6;
                sumA3 += (long) (alpha6 * (k + 1));
                sumR3 += (long) ((k + 1) * red4);
                sumG3 += (long) ((k + 1) * green6);
                sumB3 += (long) ((k + 1) * blue3);
                sumAo3 += (long) alpha6;
                sumRo3 += (long) red4;
                sumGo3 += (long) green6;
                sumBo3 += (long) blue3;
                k++;
                sumAi3 = sumAi3;
            }
            long sumAi4 = sumAi3;
            for (int k2 = 1; k2 <= radius; k2++) {
                if (k2 <= red3) {
                    srcIndex5 += width;
                }
                int rgb7 = src[srcIndex5];
                alpha6 = (rgb7 >>> 24) & ConstantValues.MAX_CHANNEL_VALUE;
                red4 = (rgb7 >>> 16) & ConstantValues.MAX_CHANNEL_VALUE;
                green6 = (rgb7 >>> 8) & ConstantValues.MAX_CHANNEL_VALUE;
                blue3 = rgb7 & ConstantValues.MAX_CHANNEL_VALUE;
                stack[k2 + radius] = rgb7;
                sumA3 += (long) (((radius + 1) - k2) * alpha6);
                sumR3 += (long) (((radius + 1) - k2) * red4);
                sumG3 += (long) (((radius + 1) - k2) * green6);
                sumB3 += (long) (((radius + 1) - k2) * blue3);
                sumAi4 += (long) alpha6;
                sumRi3 += (long) red4;
                sumGi3 += (long) green6;
                sumBi3 += (long) blue3;
            }
            int yp = radius;
            if (yp > red3) {
                yp = red3;
            }
            int rgb8 = (yp * width) + i5;
            int green7 = alpha6;
            int j2 = 0;
            int sp3 = radius;
            int dsti2 = i5;
            int yp2 = yp;
            int yp3 = blue3;
            int blue4 = green6;
            int blue5 = red4;
            while (j2 < alpha5) {
                src[dsti2] = ((((int) ((((long) mulSum2) * sumA3) >>> shrSum)) & ConstantValues.MAX_CHANNEL_VALUE) << 24) | ((((int) ((((long) mulSum2) * sumR3) >>> shrSum)) & ConstantValues.MAX_CHANNEL_VALUE) << 16) | ((((int) ((((long) mulSum2) * sumG3) >>> shrSum)) & ConstantValues.MAX_CHANNEL_VALUE) << 8) | (((int) ((((long) mulSum2) * sumB3) >>> shrSum)) & ConstantValues.MAX_CHANNEL_VALUE);
                dsti2 += width;
                long sumA4 = sumA3 - sumAo3;
                long sumR4 = sumR3 - sumRo3;
                long sumG4 = sumG3 - sumGo3;
                long sumB4 = sumB3 - sumBo3;
                int stackStart2 = (sp3 + div) - radius;
                if (stackStart2 >= div) {
                    stackStart2 -= div;
                }
                int rgb9 = stack[stackStart2];
                mulSum2 = mulSum2;
                int alpha7 = (rgb9 >>> 24) & ConstantValues.MAX_CHANNEL_VALUE;
                long sumAo4 = sumAo3 - ((long) alpha7);
                long sumRo4 = sumRo3 - ((long) ((rgb9 >>> 16) & ConstantValues.MAX_CHANNEL_VALUE));
                long sumGo4 = sumGo3 - ((long) ((rgb9 >>> 8) & ConstantValues.MAX_CHANNEL_VALUE));
                long sumBo4 = sumBo3 - ((long) (rgb9 & ConstantValues.MAX_CHANNEL_VALUE));
                if (yp2 < red3) {
                    srcIndex = rgb8 + width;
                    yp2++;
                } else {
                    srcIndex = rgb8;
                }
                int rgb10 = src[srcIndex];
                int alpha8 = (rgb10 >>> 24) & ConstantValues.MAX_CHANNEL_VALUE;
                int red5 = (rgb10 >>> 16) & ConstantValues.MAX_CHANNEL_VALUE;
                int green8 = (rgb10 >>> 8) & ConstantValues.MAX_CHANNEL_VALUE;
                int blue6 = rgb10 & ConstantValues.MAX_CHANNEL_VALUE;
                stack[stackStart2] = rgb10;
                long sumAi5 = sumAi4 + ((long) alpha8);
                long sumRi4 = sumRi3 + ((long) red5);
                long sumGi4 = sumGi3 + ((long) green8);
                long sumBi4 = sumBi3 + ((long) blue6);
                sumA3 = sumA4 + sumAi5;
                sumR3 = sumR4 + sumRi4;
                sumG3 = sumG4 + sumGi4;
                sumB3 = sumB4 + sumBi4;
                int sp4 = sp3 + 1;
                if (sp4 >= div) {
                    sp4 = 0;
                }
                sp3 = sp4;
                int rgb11 = stack[sp3];
                int alpha9 = (rgb11 >>> 24) & ConstantValues.MAX_CHANNEL_VALUE;
                int red6 = (rgb11 >>> 16) & ConstantValues.MAX_CHANNEL_VALUE;
                int green9 = (rgb11 >>> 8) & ConstantValues.MAX_CHANNEL_VALUE;
                int green10 = rgb11 & ConstantValues.MAX_CHANNEL_VALUE;
                sumAo3 = sumAo4 + ((long) alpha9);
                sumRo3 = sumRo4 + ((long) red6);
                sumGo3 = sumGo4 + ((long) green9);
                sumBo3 = sumBo4 + ((long) green10);
                sumAi4 = sumAi5 - ((long) alpha9);
                sumRi3 = sumRi4 - ((long) red6);
                sumGi3 = sumGi4 - ((long) green9);
                sumBi3 = sumBi4 - ((long) green10);
                j2++;
                blue4 = green9;
                yp3 = green10;
                green7 = alpha9;
                blue5 = red6;
                red3 = red3;
                alpha5 = height;
                rgb8 = srcIndex;
            }
            i5++;
            alpha5 = height;
            wm = wm;
        }
    }
}
