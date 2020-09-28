package com.huawei.iimagekit.shadow;

import android.graphics.Bitmap;
import android.util.ZRHung;

public class ShadowStackBlur {
    static final int A_BIT_POS = 24;
    static final int G_BIT_POS = 8;
    static final int R_BIT_POS = 16;
    private static final short[] STACK_BLUR_MUL = {512, 512, 456, 512, 328, 456, 335, 512, 405, 328, ZRHung.APPEYE_FWB_FREEZE, 456, 388, 335, ZRHung.APPEYE_TEMP3, 512, 454, 405, 364, 328, 298, ZRHung.APPEYE_FWB_FREEZE, 496, 456, 420, 388, 360, 335, 312, ZRHung.APPEYE_TEMP3, ZRHung.APPEYE_TWIN, 512, 482, 454, 428, 405, 383, 364, 345, 328, 312, 298, ZRHung.APPEYE_SUIP_WARNING, ZRHung.APPEYE_FWB_FREEZE, ZRHung.APPEYE_UIP_SLOW, 496, 475, 456, 437, 420, 404, 388, 374, 360, 347, 335, 323, 312, 302, ZRHung.APPEYE_TEMP3, ZRHung.APPEYE_SBF_FREEZE, ZRHung.APPEYE_TWIN, ZRHung.APPEYE_CL, 512, 497, 482, 468, 454, 441, 428, 417, 405, 394, 383, 373, 364, 354, 345, 337, 328, 320, 312, 305, 298, ZRHung.APPEYE_TEMP2, ZRHung.APPEYE_SUIP_WARNING, ZRHung.APPEYE_RECOVER_RESULT, ZRHung.APPEYE_FWB_FREEZE, ZRHung.APPEYE_CL, ZRHung.APPEYE_UIP_SLOW, 507, 496, 485, 475, 465, 456, 446, 437, 428, 420, 412, 404, 396, 388, 381, 374, 367, 360, 354, 347, 341, 335, 329, 323, 318, 312, 307, 302, 297, ZRHung.APPEYE_TEMP3, ZRHung.APPEYE_CRASH, ZRHung.APPEYE_SBF_FREEZE, ZRHung.APPEYE_RECOVER_RESULT, ZRHung.APPEYE_TWIN, ZRHung.APPEYE_CANR, ZRHung.APPEYE_CL, ZRHung.APPEYE_MTO_FREEZE, 512, 505, 497, 489, 482, 475, 468, 461, 454, 447, 441, 435, 428, 422, 417, 411, 405, 399, 394, 389, 383, 378, 373, 368, 364, 359, 354, 350, 345, 341, 337, 332, 328, 324, 320, 316, 312, 309, 305, 301, 298, 294, ZRHung.APPEYE_TEMP2, ZRHung.APPEYE_CRASH, ZRHung.APPEYE_SUIP_WARNING, ZRHung.APPEYE_BINDER_TIMEOUT, ZRHung.APPEYE_RECOVER_RESULT, ZRHung.APPEYE_FWE, ZRHung.APPEYE_FWB_FREEZE, ZRHung.APPEYE_MANR, ZRHung.APPEYE_CL, ZRHung.APPEYE_MTO_SLOW, ZRHung.APPEYE_UIP_SLOW, ZRHung.APPEYE_UIP_WARNING, 507, 501, 496, 491, 485, 480, 475, 470, 465, 460, 456, 451, 446, 442, 437, 433, 428, 424, 420, 416, 412, 408, 404, 400, 396, 392, 388, 385, 381, 377, 374, 370, 367, 363, 360, 357, 354, 350, 347, 344, 341, 338, 335, 332, 329, 326, 323, 320, 318, 315, 312, 310, 307, 304, 302, 299, 297, 294, ZRHung.APPEYE_TEMP3, ZRHung.APPEYE_RESUME, ZRHung.APPEYE_CRASH, ZRHung.APPEYE_SUIP_FREEZE, ZRHung.APPEYE_SBF_FREEZE, ZRHung.APPEYE_UIP_RECOVER, ZRHung.APPEYE_RECOVER_RESULT, ZRHung.APPEYE_SBF, ZRHung.APPEYE_TWIN, ZRHung.APPEYE_FWB_FREEZE, ZRHung.APPEYE_CANR, ZRHung.APPEYE_ANR, ZRHung.APPEYE_CL, ZRHung.APPEYE_MTO_INPUT, ZRHung.APPEYE_MTO_FREEZE, ZRHung.APPEYE_UIP_SLOW};
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

    /* JADX INFO: Multiple debug info for r7v2 int: [D('wm' int), D('blue' int)] */
    /* JADX INFO: Multiple debug info for r5v15 int: [D('stackStart' int), D('blue' int)] */
    /* JADX INFO: Multiple debug info for r4v18 int: [D('green' int), D('blue' int)] */
    /* JADX INFO: Multiple debug info for r9v33 int: [D('sumR' long), D('rgb' int)] */
    /* JADX INFO: Multiple debug info for r13v2 int: [D('sumRo' long), D('blue' int)] */
    /* JADX INFO: Multiple debug info for r0v20 int: [D('rgb' int), D('tmpB' int)] */
    /* JADX INFO: Multiple debug info for r12v17 int: [D('tmpG' int), D('blue' int)] */
    /* JADX INFO: Multiple debug info for r3v11 int: [D('green' int), D('blue' int)] */
    private static void stackBlur(int[] src, int width, int height, int radius) {
        int rgb = width;
        int i = height;
        int red = radius;
        short mulSum = STACK_BLUR_MUL[red];
        short shrSum = (short) STACK_BLUR_SHR[red];
        int div = red + red + 1;
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
            int srcIndex = rgb * j;
            long sumR = 0;
            int rgb2 = src[srcIndex];
            int alpha = (rgb2 >>> 24) & 255;
            long sumRi = 0;
            int red2 = (rgb2 >>> 16) & 255;
            int green = (rgb2 >>> 8) & 255;
            long sumRo = 0;
            int blue = rgb2 & 255;
            int i2 = 0;
            while (i2 <= red) {
                stack[i2] = rgb2;
                sumA += (long) (alpha * (i2 + 1));
                sumR += (long) ((i2 + 1) * red2);
                sumG += (long) ((i2 + 1) * green);
                sumB += (long) ((i2 + 1) * blue);
                sumAo += (long) alpha;
                sumRo += (long) red2;
                sumGo += (long) green;
                sumBo += (long) blue;
                i2++;
                shrSum = shrSum;
                rgb2 = rgb2;
                div = div;
            }
            int div2 = div;
            int i3 = 1;
            while (i3 <= red) {
                if (i3 <= wm) {
                    srcIndex++;
                }
                int rgb3 = src[srcIndex];
                alpha = (rgb3 >>> 24) & 255;
                red2 = (rgb3 >>> 16) & 255;
                green = (rgb3 >>> 8) & 255;
                blue = rgb3 & 255;
                stack[i3 + red] = rgb3;
                sumA += (long) (((red + 1) - i3) * alpha);
                sumR += (long) (((red + 1) - i3) * red2);
                sumG += (long) (((red + 1) - i3) * green);
                sumB += (long) (((red + 1) - i3) * blue);
                sumAi += (long) alpha;
                sumRi += (long) red2;
                sumGi += (long) green;
                sumBi += (long) blue;
                i3++;
                stack = stack;
            }
            int xp = radius;
            if (xp > wm) {
                xp = wm;
            }
            int srcIndex2 = (j * rgb) + xp;
            int xp2 = radius;
            int i4 = 0;
            int dsti = j * rgb;
            int xp3 = xp;
            while (i4 < rgb) {
                src[dsti] = ((((int) ((((long) mulSum) * sumA) >>> shrSum)) & 255) << 24) | ((((int) ((((long) mulSum) * sumR) >>> shrSum)) & 255) << 16) | ((((int) ((((long) mulSum) * sumG) >>> shrSum)) & 255) << 8) | (((int) ((((long) mulSum) * sumB) >>> shrSum)) & 255);
                dsti++;
                long sumA2 = sumA - sumAo;
                long sumR2 = sumR - sumRo;
                long sumG2 = sumG - sumGo;
                long sumB2 = sumB - sumBo;
                int stackStart = (xp2 + div2) - red;
                if (stackStart >= div2) {
                    stackStart -= div2;
                }
                int tmpB = stack[stackStart];
                long sumAo2 = sumAo - ((long) ((tmpB >>> 24) & 255));
                long sumRo2 = sumRo - ((long) ((tmpB >>> 16) & 255));
                long sumGo2 = sumGo - ((long) ((tmpB >>> 8) & 255));
                long sumBo2 = sumBo - ((long) (tmpB & 255));
                if (xp3 < wm) {
                    srcIndex2++;
                    xp3++;
                }
                int rgb4 = src[srcIndex2];
                stack[stackStart] = rgb4;
                long sumAi2 = sumAi + ((long) ((rgb4 >>> 24) & 255));
                long sumRi2 = sumRi + ((long) ((rgb4 >>> 16) & 255));
                long sumGi2 = sumGi + ((long) ((rgb4 >>> 8) & 255));
                long sumBi2 = sumBi + ((long) (rgb4 & 255));
                sumA = sumA2 + sumAi2;
                sumR = sumR2 + sumRi2;
                sumG = sumG2 + sumGi2;
                sumB = sumB2 + sumBi2;
                int sp = xp2 + 1;
                if (sp >= div2) {
                    sp = 0;
                }
                int rgb5 = stack[sp];
                int alpha2 = (rgb5 >>> 24) & 255;
                int red3 = (rgb5 >>> 16) & 255;
                green = (rgb5 >>> 8) & 255;
                int blue2 = rgb5 & 255;
                sumAo = sumAo2 + ((long) alpha2);
                sumRo = sumRo2 + ((long) red3);
                sumGo = sumGo2 + ((long) green);
                sumBo = sumBo2 + ((long) blue2);
                sumAi = sumAi2 - ((long) alpha2);
                sumRi = sumRi2 - ((long) red3);
                sumGi = sumGi2 - ((long) green);
                sumBi = sumBi2 - ((long) blue2);
                i4++;
                red2 = red3;
                srcIndex2 = srcIndex2;
                div2 = div2;
                xp2 = sp;
                red = radius;
                alpha = alpha2;
                blue = blue2;
                mulSum = mulSum;
                rgb = width;
            }
            j++;
            rgb = width;
            i = height;
            red = radius;
            div = div2;
            stack = stack;
            shrSum = shrSum;
        }
        short mulSum2 = mulSum;
        int alpha3 = height;
        int red4 = alpha3 - 1;
        int j2 = 0;
        while (true) {
            int green2 = width;
            if (j2 < green2) {
                long sumAi3 = 0;
                long sumR3 = 0;
                long sumRi3 = 0;
                long sumG3 = 0;
                long sumGi3 = 0;
                long sumB3 = 0;
                long sumBi3 = 0;
                int rgb6 = src[j2];
                long sumA3 = 0;
                int alpha4 = (rgb6 >>> 24) & 255;
                int red5 = (rgb6 >>> 16) & 255;
                int srcIndex3 = j2;
                int green3 = (rgb6 >>> 8) & 255;
                int blue3 = rgb6 & 255;
                int k = 0;
                long sumBo3 = 0;
                long sumGo3 = 0;
                long sumRo3 = 0;
                long sumAo3 = 0;
                while (k <= radius) {
                    stack[k] = rgb6;
                    sumA3 += (long) (alpha4 * (k + 1));
                    sumR3 += (long) ((k + 1) * red5);
                    sumG3 += (long) ((k + 1) * green3);
                    sumB3 += (long) ((k + 1) * blue3);
                    sumAo3 += (long) alpha4;
                    sumRo3 += (long) red5;
                    sumGo3 += (long) green3;
                    sumBo3 += (long) blue3;
                    k++;
                    sumAi3 = sumAi3;
                }
                long sumAi4 = sumAi3;
                for (int k2 = 1; k2 <= radius; k2++) {
                    if (k2 <= red4) {
                        srcIndex3 += green2;
                    }
                    int rgb7 = src[srcIndex3];
                    alpha4 = (rgb7 >>> 24) & 255;
                    red5 = (rgb7 >>> 16) & 255;
                    green3 = (rgb7 >>> 8) & 255;
                    blue3 = rgb7 & 255;
                    stack[k2 + radius] = rgb7;
                    sumA3 += (long) (((radius + 1) - k2) * alpha4);
                    sumR3 += (long) (((radius + 1) - k2) * red5);
                    sumG3 += (long) (((radius + 1) - k2) * green3);
                    sumB3 += (long) (((radius + 1) - k2) * blue3);
                    sumAi4 += (long) alpha4;
                    sumRi3 += (long) red5;
                    sumGi3 += (long) green3;
                    sumBi3 += (long) blue3;
                }
                int yp = radius;
                if (yp > red4) {
                    yp = red4;
                }
                int srcIndex4 = (yp * green2) + j2;
                int blue4 = 0;
                int dsti2 = j2;
                int green4 = red5;
                int red6 = yp;
                int sp2 = radius;
                int hm = blue3;
                int rgb8 = green3;
                while (blue4 < alpha3) {
                    src[dsti2] = ((((int) ((((long) mulSum2) * sumA3) >>> shrSum)) & 255) << 24) | ((((int) ((((long) mulSum2) * sumR3) >>> shrSum)) & 255) << 16) | ((((int) ((((long) mulSum2) * sumG3) >>> shrSum)) & 255) << 8) | (((int) ((((long) mulSum2) * sumB3) >>> shrSum)) & 255);
                    dsti2 += green2;
                    long sumA4 = sumA3 - sumAo3;
                    long sumR4 = sumR3 - sumRo3;
                    long sumG4 = sumG3 - sumGo3;
                    long sumB4 = sumB3 - sumBo3;
                    int stackStart2 = (sp2 + div) - radius;
                    if (stackStart2 >= div) {
                        stackStart2 -= div;
                    }
                    int rgb9 = stack[stackStart2];
                    mulSum2 = mulSum2;
                    long sumAo4 = sumAo3 - ((long) ((rgb9 >>> 24) & 255));
                    long sumRo4 = sumRo3 - ((long) ((rgb9 >>> 16) & 255));
                    long sumGo4 = sumGo3 - ((long) ((rgb9 >>> 8) & 255));
                    long sumBo4 = sumBo3 - ((long) (rgb9 & 255));
                    int yp2 = red6;
                    if (yp2 < red4) {
                        srcIndex4 += green2;
                        yp2++;
                    }
                    int rgb10 = src[srcIndex4];
                    stack[stackStart2] = rgb10;
                    long sumAi5 = sumAi4 + ((long) ((rgb10 >>> 24) & 255));
                    long sumRi4 = sumRi3 + ((long) ((rgb10 >>> 16) & 255));
                    long sumGi4 = sumGi3 + ((long) ((rgb10 >>> 8) & 255));
                    long sumBi4 = sumBi3 + ((long) (rgb10 & 255));
                    sumA3 = sumA4 + sumAi5;
                    sumR3 = sumR4 + sumRi4;
                    sumG3 = sumG4 + sumGi4;
                    sumB3 = sumB4 + sumBi4;
                    sp2++;
                    if (sp2 >= div) {
                        sp2 = 0;
                    }
                    int rgb11 = stack[sp2];
                    int alpha5 = (rgb11 >>> 24) & 255;
                    int red7 = (rgb11 >>> 16) & 255;
                    int green5 = (rgb11 >>> 8) & 255;
                    int blue5 = rgb11 & 255;
                    sumAo3 = sumAo4 + ((long) alpha5);
                    sumRo3 = sumRo4 + ((long) red7);
                    sumGo3 = sumGo4 + ((long) green5);
                    sumBo3 = sumBo4 + ((long) blue5);
                    sumAi4 = sumAi5 - ((long) alpha5);
                    sumRi3 = sumRi4 - ((long) red7);
                    sumGi3 = sumGi4 - ((long) green5);
                    sumBi3 = sumBi4 - ((long) blue5);
                    green4 = red7;
                    red4 = red4;
                    red6 = yp2;
                    alpha3 = height;
                    hm = blue5;
                    blue4++;
                    rgb8 = green5;
                    j2 = j2;
                    green2 = width;
                }
                j2++;
                alpha3 = height;
                wm = wm;
            } else {
                return;
            }
        }
    }
}
