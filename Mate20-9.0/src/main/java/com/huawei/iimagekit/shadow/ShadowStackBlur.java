package com.huawei.iimagekit.shadow;

import android.graphics.Bitmap;
import android.util.IMonitorKeys;

public class ShadowStackBlur {
    private static final short[] stack_blur_mul = {512, 512, IMonitorKeys.E904200009_DSAPP3RTTL1_INT, 512, IMonitorKeys.E904200009_WCDMAVOICETIMEONBAND8_INT, IMonitorKeys.E904200009_DSAPP3RTTL1_INT, IMonitorKeys.E904200009_ACTIVECALLLOHANGUPNUM_INT, 512, IMonitorKeys.E904200009_DSAPP1WEBDELAY_INT, IMonitorKeys.E904200009_WCDMAVOICETIMEONBAND8_INT, 271, IMonitorKeys.E904200009_DSAPP3RTTL1_INT, IMonitorKeys.E904200009_DNSDELAY_0_20_INT, IMonitorKeys.E904200009_ACTIVECALLLOHANGUPNUM_INT, IMonitorKeys.E904200009_TIMECDMASIGGRID1_INT, 512, IMonitorKeys.E904200009_DSAPP3DELAYL5_INT, IMonitorKeys.E904200009_DSAPP1WEBDELAY_INT, IMonitorKeys.E904200009_GU2GPDPSUCC_INT, IMonitorKeys.E904200009_WCDMAVOICETIMEONBAND8_INT, IMonitorKeys.E904200009_TIMEHDRSIGGRID1_INT, 271, IMonitorKeys.E904200009_DSAPP5RTTL3_INT, IMonitorKeys.E904200009_DSAPP3RTTL1_INT, IMonitorKeys.E904200009_DSAPP1RTTL3_INT, IMonitorKeys.E904200009_DNSDELAY_0_20_INT, IMonitorKeys.E904200009_EPSATTACHSUCC_INT, IMonitorKeys.E904200009_ACTIVECALLLOHANGUPNUM_INT, IMonitorKeys.E904200009_GSMCSCALLCHAFSTIMELEN_INT, IMonitorKeys.E904200009_TIMECDMASIGGRID1_INT, 273, 512, IMonitorKeys.E904200009_DSAPP5SUCCNUM_INT, IMonitorKeys.E904200009_DSAPP3DELAYL5_INT, IMonitorKeys.E904200009_DSAPP2TOTALNUM_INT, IMonitorKeys.E904200009_DSAPP1WEBDELAY_INT, IMonitorKeys.E904200009_PSDURNOREG_INT, IMonitorKeys.E904200009_GU2GPDPSUCC_INT, IMonitorKeys.E904200009_DSFAILNUM_INT, IMonitorKeys.E904200009_WCDMAVOICETIMEONBAND8_INT, IMonitorKeys.E904200009_GSMCSCALLCHAFSTIMELEN_INT, IMonitorKeys.E904200009_TIMEHDRSIGGRID1_INT, 284, 271, 259, IMonitorKeys.E904200009_DSAPP5RTTL3_INT, IMonitorKeys.E904200009_DSAPP4RTTL1_INT, IMonitorKeys.E904200009_DSAPP3RTTL1_INT, IMonitorKeys.E904200009_DSAPP2RTTL1_INT, IMonitorKeys.E904200009_DSAPP1RTTL3_INT, IMonitorKeys.E904200009_DSAPP1RTT_INT, IMonitorKeys.E904200009_DNSDELAY_0_20_INT, IMonitorKeys.E904200009_LUSUCC_INT, IMonitorKeys.E904200009_EPSATTACHSUCC_INT, IMonitorKeys.E904200009_DSTOTALNUM_INT, IMonitorKeys.E904200009_ACTIVECALLLOHANGUPNUM_INT, IMonitorKeys.E904200009_MTCSFBREDIRTOTDSCALLSUC_INT, IMonitorKeys.E904200009_GSMCSCALLCHAFSTIMELEN_INT, IMonitorKeys.E904200009_TIMEHDRSIGGRID5_INT, IMonitorKeys.E904200009_TIMECDMASIGGRID1_INT, 282, 273, 265, 512, IMonitorKeys.E904200009_DSAPP5RTTL4_INT, IMonitorKeys.E904200009_DSAPP5SUCCNUM_INT, IMonitorKeys.E904200009_DSAPP4TCPSUCCNUM_INT, IMonitorKeys.E904200009_DSAPP3DELAYL5_INT, IMonitorKeys.E904200009_DSAPP2RTTL5_INT, IMonitorKeys.E904200009_DSAPP2TOTALNUM_INT, IMonitorKeys.E904200009_DSAPP1DELAYL6_INT, IMonitorKeys.E904200009_DSAPP1WEBDELAY_INT, IMonitorKeys.E904200009_ACCESSEDCELLCOUNT_SMALLINT, IMonitorKeys.E904200009_PSDURNOREG_INT, IMonitorKeys.E904200009_RAUFAIL_INT, IMonitorKeys.E904200009_GU2GPDPSUCC_INT, IMonitorKeys.E904200009_DSDELAYNUML6_INT, IMonitorKeys.E904200009_DSFAILNUM_INT, IMonitorKeys.E904200009_CALLDURATIONRE_60_180_INT, IMonitorKeys.E904200009_WCDMAVOICETIMEONBAND8_INT, IMonitorKeys.E904200009_MTSRVCCCALLSUCC_INT, IMonitorKeys.E904200009_GSMCSCALLCHAFSTIMELEN_INT, IMonitorKeys.E904200009_GSMCSCALLB900TIMELEN_INT, IMonitorKeys.E904200009_TIMEHDRSIGGRID1_INT, 291, 284, 278, 271, 265, 259, IMonitorKeys.E904200009_DSAPP6DELAYL1_INT, IMonitorKeys.E904200009_DSAPP5RTTL3_INT, IMonitorKeys.E904200009_DSAPP5TOTALNUM_INT, IMonitorKeys.E904200009_DSAPP4RTTL1_INT, IMonitorKeys.E904200009_DSAPP4NOACKNUM_INT, IMonitorKeys.E904200009_DSAPP3RTTL1_INT, IMonitorKeys.E904200009_DSAPP3NOACKNUM_INT, IMonitorKeys.E904200009_DSAPP2RTTL1_INT, IMonitorKeys.E904200009_DSAPP2TOTALNUM_INT, IMonitorKeys.E904200009_DSAPP1RTTL3_INT, IMonitorKeys.E904200009_DSAPP1DELAYL1_INT, IMonitorKeys.E904200009_DSAPP1RTT_INT, IMonitorKeys.E904200009_IT310REGION1_SMALLINT, IMonitorKeys.E904200009_DNSDELAY_0_20_INT, IMonitorKeys.E904200009_PSDURREG3G_INT, IMonitorKeys.E904200009_LUSUCC_INT, IMonitorKeys.E904200009_GU3GPDPSUCC_INT, IMonitorKeys.E904200009_EPSATTACHSUCC_INT, IMonitorKeys.E904200009_DSDELAYNUML6_INT, IMonitorKeys.E904200009_DSTOTALNUM_INT, IMonitorKeys.E904200009_CALLDURATIONLO_180_I_INT, IMonitorKeys.E904200009_ACTIVECALLLOHANGUPNUM_INT, IMonitorKeys.E904200009_WCDMAVOICETIMEONBANDOTHER_INT, IMonitorKeys.E904200009_MTCSFBREDIRTOTDSCALLSUC_INT, IMonitorKeys.E904200009_VOLTECSREDIALCALLSUCC_INT, IMonitorKeys.E904200009_GSMCSCALLCHAFSTIMELEN_INT, IMonitorKeys.E904200009_GSMCSCALLB850TIMELEN_INT, IMonitorKeys.E904200009_TIMEHDRSIGGRID5_INT, IMonitorKeys.E904200009_TIMEHDRSIGGRID0_INT, IMonitorKeys.E904200009_TIMECDMASIGGRID1_INT, 287, 282, 278, 273, 269, 265, 261, 512, IMonitorKeys.E904200009_DSAPP6TCPTOTALNUM_INT, IMonitorKeys.E904200009_DSAPP5RTTL4_INT, IMonitorKeys.E904200009_DSAPP5DELAYL2_INT, IMonitorKeys.E904200009_DSAPP5SUCCNUM_INT, IMonitorKeys.E904200009_DSAPP4RTTL1_INT, IMonitorKeys.E904200009_DSAPP4TCPSUCCNUM_INT, IMonitorKeys.E904200009_DSAPP4RTT_INT, IMonitorKeys.E904200009_DSAPP3DELAYL5_INT, IMonitorKeys.E904200009_DSAPP3TOTALNUM_INT, IMonitorKeys.E904200009_DSAPP2RTTL5_INT, IMonitorKeys.E904200009_DSAPP2DELAYL5_INT, IMonitorKeys.E904200009_DSAPP2TOTALNUM_INT, IMonitorKeys.E904200009_DSAPP1RTTL5_INT, IMonitorKeys.E904200009_DSAPP1DELAYL6_INT, IMonitorKeys.E904200009_DSAPP1TCPSUCCNUM_INT, IMonitorKeys.E904200009_DSAPP1WEBDELAY_INT, IMonitorKeys.E904200009_NORMALCALLBCELL_INT, IMonitorKeys.E904200009_ACCESSEDCELLCOUNT_SMALLINT, IMonitorKeys.E904200009_DNSDELAY_20_150_INT, IMonitorKeys.E904200009_PSDURNOREG_INT, IMonitorKeys.E904200009_CSDURREG4G_INT, IMonitorKeys.E904200009_RAUFAIL_INT, IMonitorKeys.E904200009_GU3GPDPFAIL_INT, IMonitorKeys.E904200009_GU2GPDPSUCC_INT, IMonitorKeys.E904200009_DSRTTNUML5_INT, IMonitorKeys.E904200009_DSDELAYNUML6_INT, IMonitorKeys.E904200009_DSDELAYNUML2_INT, IMonitorKeys.E904200009_DSFAILNUM_INT, IMonitorKeys.E904200009_CALLDURATIONLO_180_I_INT, IMonitorKeys.E904200009_CALLDURATIONRE_60_180_INT, IMonitorKeys.E904200009_LTECSFBFRREDIRDUR_INT, IMonitorKeys.E904200009_WCDMAVOICETIMEONBAND8_INT, IMonitorKeys.E904200009_MTCSFBLTEACFAILCALLSUC_INT, IMonitorKeys.E904200009_MTSRVCCCALLSUCC_INT, IMonitorKeys.E904200009_CALLNUMWITHLAC_CLASS, IMonitorKeys.E904200009_GSMCSCALLCHAFSTIMELEN_INT, IMonitorKeys.E904200009_GSMCSCALLCHFSTIMELEN_INT, IMonitorKeys.E904200009_GSMCSCALLB900TIMELEN_INT, IMonitorKeys.E904200009_TIMEHDRSIGGRID4_INT, IMonitorKeys.E904200009_TIMEHDRSIGGRID1_INT, IMonitorKeys.E904200009_TIMECDMASIGGRID3_INT, 291, 287, 284, 281, 278, 274, 271, 268, 265, 262, 259, 257, IMonitorKeys.E904200009_DSAPP6DELAYL1_INT, IMonitorKeys.E904200009_DSAPP6SUCCNUM_INT, IMonitorKeys.E904200009_DSAPP5RTTL3_INT, IMonitorKeys.E904200009_DSAPP5DELAYL4_INT, IMonitorKeys.E904200009_DSAPP5TOTALNUM_INT, IMonitorKeys.E904200009_DSAPP5RTT_INT, IMonitorKeys.E904200009_DSAPP4RTTL1_INT, IMonitorKeys.E904200009_DSAPP4DELAYL2_INT, IMonitorKeys.E904200009_DSAPP4NOACKNUM_INT, IMonitorKeys.E904200009_DSAPP3RTTL5_INT, IMonitorKeys.E904200009_DSAPP3RTTL1_INT, IMonitorKeys.E904200009_DSAPP3DELAYL2_INT, IMonitorKeys.E904200009_DSAPP3NOACKNUM_INT, IMonitorKeys.E904200009_DSAPP3RTT_INT, IMonitorKeys.E904200009_DSAPP2RTTL1_INT, IMonitorKeys.E904200009_DSAPP2DELAYL3_INT, IMonitorKeys.E904200009_DSAPP2TOTALNUM_INT, IMonitorKeys.E904200009_DSAPP2WEBDELAY_INT, IMonitorKeys.E904200009_DSAPP1RTTL3_INT, IMonitorKeys.E904200009_DSAPP1DELAYL5_INT, IMonitorKeys.E904200009_DSAPP1DELAYL1_INT, IMonitorKeys.E904200009_DSAPP1NOACKNUM_INT, IMonitorKeys.E904200009_DSAPP1RTT_INT, IMonitorKeys.E904200009_ABNORMALCALLBCELL_INT, IMonitorKeys.E904200009_IT310REGION1_SMALLINT, IMonitorKeys.E904200009_DNSDELAY1000_2000_INT, IMonitorKeys.E904200009_DNSDELAY_0_20_INT, IMonitorKeys.E904200009_DNSTOTALDELAY_INT, IMonitorKeys.E904200009_PSDURREG3G_INT, IMonitorKeys.E904200009_CSDURREG3G_INT, IMonitorKeys.E904200009_LUSUCC_INT, IMonitorKeys.E904200009_GUATTSUCC_INT, IMonitorKeys.E904200009_GU3GPDPSUCC_INT, IMonitorKeys.E904200009_EPSTAUFAIL_INT, IMonitorKeys.E904200009_EPSATTACHSUCC_INT, IMonitorKeys.E904200009_DSRTTNUML3_INT, IMonitorKeys.E904200009_DSDELAYNUML6_INT, IMonitorKeys.E904200009_DSDELAYNUML2_INT, IMonitorKeys.E904200009_DSTOTALNUM_INT, IMonitorKeys.E904200009_DSSUCCNUM_INT, IMonitorKeys.E904200009_CALLDURATIONLO_180_I_INT, IMonitorKeys.E904200009_CALLDURATIONRE_180_I_INT, IMonitorKeys.E904200009_ACTIVECALLLOHANGUPNUM_INT, IMonitorKeys.E904200009_LTECSFBFRREDIRDUR_INT, IMonitorKeys.E904200009_WCDMAVOICETIMEONBANDOTHER_INT, IMonitorKeys.E904200009_WCDMAVOICETIMEONBAND2_INT, IMonitorKeys.E904200009_MTCSFBREDIRTOTDSCALLSUC_INT, IMonitorKeys.E904200009_MTSRVCCCALLSUCC_INT, IMonitorKeys.E904200009_VOLTECSREDIALCALLSUCC_INT, IMonitorKeys.E904200009_DSDACALLNUM_INT, IMonitorKeys.E904200009_GSMCSCALLCHAFSTIMELEN_INT, IMonitorKeys.E904200009_GSMCSCALLCHHSTIMELEN_INT, IMonitorKeys.E904200009_GSMCSCALLB850TIMELEN_INT, IMonitorKeys.E904200009_CHRRECORDNUM_INT, IMonitorKeys.E904200009_TIMEHDRSIGGRID5_INT, IMonitorKeys.E904200009_TIMEHDRSIGGRID2_INT, IMonitorKeys.E904200009_TIMEHDRSIGGRID0_INT, IMonitorKeys.E904200009_TIMECDMASIGGRID3_INT, IMonitorKeys.E904200009_TIMECDMASIGGRID1_INT, 289, 287, 285, 282, 280, 278, 275, 273, 271, 269, 267, 265, 263, 261, 259};
    private static final byte[] stack_blur_shr = {9, 11, 12, 13, 13, 14, 14, 15, 15, 15, 15, 16, 16, 16, 16, 17, 17, 17, 17, 17, 17, 17, 18, 18, 18, 18, 18, 18, 18, 18, 18, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 19, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 20, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 21, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 22, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 23, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24, 24};

    public static void doBlur(Bitmap bitmapForBlur, Bitmap blurredBitmap, int radius) {
        int w = bitmapForBlur.getWidth();
        int h = bitmapForBlur.getHeight();
        int[] pixels = new int[(w * h)];
        int[] iArr = pixels;
        int i = w;
        int i2 = w;
        int i3 = h;
        bitmapForBlur.getPixels(iArr, 0, i, 0, 0, i2, i3);
        ShadowUtil.processAlphaChannelBefore(pixels);
        stackBlur(pixels, w, h, radius);
        ShadowUtil.processAlphaChannelAfter(pixels);
        blurredBitmap.setPixels(iArr, 0, i, 0, 0, i2, i3);
    }

    /* JADX WARNING: Incorrect type for immutable var: ssa=short, code=int, for r4v1, types: [short] */
    private static void stackBlur(int[] src, int w, int h, int radius) {
        int src_i;
        int rgb = w;
        int i = h;
        int r = radius;
        int r2 = stack_blur_mul[r];
        short shr_sum = (short) stack_blur_shr[r];
        int div = (r * 2) + 1;
        int[] stack = new int[div];
        int wm = rgb - 1;
        int div2 = 0;
        while (div2 < i) {
            long sum_r_o = 0;
            long sum_g = 0;
            long sum_g_i = 0;
            long sum_g_o = 0;
            long sum_b = 0;
            long sum_b_i = 0;
            long sum_b_o = 0;
            long sum_a = 0;
            long sum_a_i = 0;
            long sum_a_o = 0;
            int src_i2 = rgb * div2;
            int dst_i = div2 * rgb;
            int rgb2 = src[src_i2];
            int a = (rgb2 >>> 24) & 255;
            long sum_r = 0;
            int r3 = (rgb2 >>> 16) & 255;
            int g = (rgb2 >>> 8) & 255;
            long sum_r_i = 0;
            int b = rgb2 & 255;
            int i2 = 0;
            while (i2 <= r) {
                stack[i2] = rgb2;
                sum_a += (long) (a * (i2 + 1));
                sum_r += (long) ((i2 + 1) * r3);
                sum_g += (long) ((i2 + 1) * g);
                sum_b += (long) ((i2 + 1) * b);
                sum_a_o += (long) a;
                sum_r_o += (long) r3;
                sum_g_o += (long) g;
                sum_b_o += (long) b;
                i2++;
                rgb2 = rgb2;
                div = div;
            }
            int div3 = div;
            int i3 = rgb2;
            int i4 = 1;
            while (i4 <= r) {
                if (i4 <= wm) {
                    src_i2++;
                }
                int rgb3 = src[src_i2];
                a = (rgb3 >>> 24) & 255;
                r3 = (rgb3 >>> 16) & 255;
                g = (rgb3 >>> 8) & 255;
                b = rgb3 & 255;
                stack[i4 + r] = rgb3;
                sum_a += (long) (((r + 1) - i4) * a);
                sum_r += (long) (((r + 1) - i4) * r3);
                sum_g += (long) (((r + 1) - i4) * g);
                sum_b += (long) (((r + 1) - i4) * b);
                sum_a_i += (long) a;
                sum_r_i += (long) r3;
                sum_g_i += (long) g;
                sum_b_i += (long) b;
                i4++;
                stack = stack;
                int i5 = rgb3;
            }
            int[] stack2 = stack;
            int sp = r;
            int xp = r;
            if (xp > wm) {
                xp = wm;
            }
            int src_i3 = (div2 * rgb) + xp;
            int xp2 = xp;
            int xp3 = sp;
            int x = 0;
            while (x < rgb) {
                int i6 = r3;
                int i7 = g;
                int tmp_a = ((int) ((((long) r2) * sum_a) >>> shr_sum)) & 255;
                int i8 = b;
                int tmp_r = ((int) ((((long) r2) * sum_r) >>> shr_sum)) & 255;
                int tmp_g = ((int) ((((long) r2) * sum_g) >>> shr_sum)) & 255;
                int i9 = a;
                int y = div2;
                int tmp_b = ((int) ((((long) r2) * sum_b) >>> shr_sum)) & 255;
                src[dst_i] = (tmp_a << 24) | (tmp_r << 16) | (tmp_g << 8) | tmp_b;
                dst_i++;
                long sum_a2 = sum_a - sum_a_o;
                long sum_r2 = sum_r - sum_r_o;
                long sum_g2 = sum_g - sum_g_o;
                long sum_b2 = sum_b - sum_b_o;
                int stack_start = (xp3 + div3) - r;
                int div4 = div3;
                if (stack_start >= div4) {
                    stack_start -= div4;
                }
                int stack_i = stack_start;
                int i10 = tmp_g;
                int tmp_g2 = stack2[stack_i];
                int i11 = tmp_b;
                int i12 = stack_start;
                int i13 = tmp_a;
                int i14 = tmp_r;
                int mul_sum = r2;
                short shr_sum2 = shr_sum;
                long sum_a_o2 = sum_a_o - ((long) ((tmp_g2 >>> 24) & 255));
                long sum_r_o2 = sum_r_o - ((long) ((tmp_g2 >>> 16) & 255));
                long sum_g_o2 = sum_g_o - ((long) ((tmp_g2 >>> 8) & 255));
                long sum_b_o2 = sum_b_o - ((long) (tmp_g2 & 255));
                if (xp2 < wm) {
                    src_i3++;
                    xp2++;
                }
                int rgb4 = src[src_i3];
                stack2[stack_i] = rgb4;
                long sum_a_i2 = sum_a_i + ((long) ((rgb4 >>> 24) & 255));
                long sum_r_i2 = sum_r_i + ((long) ((rgb4 >>> 16) & 255));
                long sum_g_i2 = sum_g_i + ((long) ((rgb4 >>> 8) & 255));
                long sum_b_i2 = sum_b_i + ((long) (rgb4 & 255));
                sum_a = sum_a2 + sum_a_i2;
                sum_r = sum_r2 + sum_r_i2;
                sum_g = sum_g2 + sum_g_i2;
                sum_b = sum_b2 + sum_b_i2;
                int sp2 = xp3 + 1;
                if (sp2 >= div4) {
                    sp2 = 0;
                }
                int rgb5 = stack2[sp2];
                int a2 = (rgb5 >>> 24) & 255;
                int r4 = (rgb5 >>> 16) & 255;
                g = (rgb5 >>> 8) & 255;
                b = rgb5 & 255;
                sum_a_o = sum_a_o2 + ((long) a2);
                sum_r_o = sum_r_o2 + ((long) r4);
                sum_g_o = sum_g_o2 + ((long) g);
                sum_b_o = sum_b_o2 + ((long) b);
                sum_a_i = sum_a_i2 - ((long) a2);
                sum_r_i = sum_r_i2 - ((long) r4);
                sum_g_i = sum_g_i2 - ((long) g);
                sum_b_i = sum_b_i2 - ((long) b);
                x++;
                int i15 = rgb5;
                a = a2;
                r3 = r4;
                div3 = div4;
                div2 = y;
                r2 = mul_sum;
                shr_sum = shr_sum2;
                src_i3 = src_i3;
                xp3 = sp2;
                rgb = w;
                int a3 = h;
            }
            int mul_sum2 = r2;
            short s = shr_sum;
            div = div3;
            stack = stack2;
            i = h;
            div2++;
            rgb = w;
        }
        int mul_sum3 = r2;
        short shr_sum3 = shr_sum;
        int div5 = div;
        int[] stack3 = stack;
        int i16 = h;
        int hm = i16 - 1;
        int x2 = 0;
        while (true) {
            int i17 = w;
            if (x2 < i17) {
                long sum_a_i3 = 0;
                long sum_r3 = 0;
                long sum_r_i3 = 0;
                long sum_g3 = 0;
                long sum_g_i3 = 0;
                long sum_b3 = 0;
                long sum_b_i3 = 0;
                int src_i4 = x2;
                long sum_a3 = 0;
                int rgb6 = src[src_i4];
                int a4 = (rgb6 >>> 24) & 255;
                int src_i5 = src_i4;
                int src_i6 = (rgb6 >>> 16) & 255;
                int wm2 = wm;
                int g2 = (rgb6 >>> 8) & 255;
                int dst_i2 = x2;
                int dst_i3 = rgb6 & 255;
                long sum_b_o3 = 0;
                long sum_g_o3 = 0;
                long sum_r_o3 = 0;
                long sum_a_o3 = 0;
                int i18 = 0;
                while (i18 <= r) {
                    stack3[i18] = rgb6;
                    sum_a3 += (long) ((i18 + 1) * a4);
                    sum_r3 += (long) ((i18 + 1) * src_i6);
                    sum_g3 += (long) ((i18 + 1) * g2);
                    sum_b3 += (long) ((i18 + 1) * dst_i3);
                    sum_a_o3 += (long) a4;
                    sum_r_o3 += (long) src_i6;
                    sum_g_o3 += (long) g2;
                    sum_b_o3 += (long) dst_i3;
                    i18++;
                    sum_a_i3 = sum_a_i3;
                }
                long sum_a_i4 = sum_a_i3;
                int i19 = dst_i3;
                int g3 = g2;
                int r5 = src_i6;
                int a5 = a4;
                int rgb7 = rgb6;
                for (int i20 = 1; i20 <= r; i20++) {
                    if (i20 <= hm) {
                        src_i5 += i17;
                    }
                    rgb7 = src[src_i5];
                    a5 = (rgb7 >>> 24) & 255;
                    r5 = (rgb7 >>> 16) & 255;
                    g3 = (rgb7 >>> 8) & 255;
                    int b2 = rgb7 & 255;
                    stack3[i20 + r] = rgb7;
                    sum_a3 += (long) (((r + 1) - i20) * a5);
                    sum_r3 += (long) (((r + 1) - i20) * r5);
                    sum_g3 += (long) (((r + 1) - i20) * g3);
                    sum_b3 += (long) (((r + 1) - i20) * b2);
                    sum_a_i4 += (long) a5;
                    sum_r_i3 += (long) r5;
                    sum_g_i3 += (long) g3;
                    sum_b_i3 += (long) b2;
                }
                int sp3 = r;
                int yp = r;
                if (yp > hm) {
                    yp = hm;
                }
                int src_i7 = (yp * i17) + x2;
                int yp2 = yp;
                int sp4 = sp3;
                int y2 = 0;
                while (y2 < i16) {
                    int i21 = rgb7;
                    int i22 = a5;
                    int mul_sum4 = mul_sum3;
                    int tmp_a2 = ((int) ((((long) mul_sum4) * sum_a3) >>> shr_sum3)) & 255;
                    int i23 = r5;
                    int tmp_r2 = ((int) ((((long) mul_sum4) * sum_r3) >>> shr_sum3)) & 255;
                    int i24 = g3;
                    int tmp_g3 = ((int) ((((long) mul_sum4) * sum_g3) >>> shr_sum3)) & 255;
                    int src_i8 = src_i7;
                    int yp3 = yp2;
                    int tmp_b2 = ((int) ((((long) mul_sum4) * sum_b3) >>> shr_sum3)) & 255;
                    src[dst_i2] = (tmp_a2 << 24) | (tmp_r2 << 16) | (tmp_g3 << 8) | tmp_b2;
                    dst_i2 += i17;
                    long sum_a4 = sum_a3 - sum_a_o3;
                    long sum_r4 = sum_r3 - sum_r_o3;
                    long sum_g4 = sum_g3 - sum_g_o3;
                    long sum_b4 = sum_b3 - sum_b_o3;
                    int stack_start2 = (sp4 + div5) - r;
                    if (stack_start2 >= div5) {
                        stack_start2 -= div5;
                    }
                    int stack_i2 = stack_start2;
                    int mul_sum5 = mul_sum4;
                    int rgb8 = stack3[stack_i2];
                    int i25 = tmp_a2;
                    int i26 = tmp_r2;
                    int i27 = tmp_g3;
                    int i28 = tmp_b2;
                    long sum_a_o4 = sum_a_o3 - ((long) ((rgb8 >>> 24) & 255));
                    long sum_r_o4 = sum_r_o3 - ((long) ((rgb8 >>> 16) & 255));
                    long sum_g_o4 = sum_g_o3 - ((long) ((rgb8 >>> 8) & 255));
                    long sum_b_o4 = sum_b_o3 - ((long) (rgb8 & 255));
                    int yp4 = yp3;
                    if (yp4 < hm) {
                        src_i = src_i8 + i17;
                        yp4++;
                    } else {
                        src_i = src_i8;
                    }
                    int rgb9 = src[src_i];
                    stack3[stack_i2] = rgb9;
                    int i29 = rgb9;
                    int hm2 = hm;
                    long sum_a_i5 = sum_a_i4 + ((long) ((rgb9 >>> 24) & 255));
                    long sum_r_i4 = sum_r_i3 + ((long) ((rgb9 >>> 16) & 255));
                    long sum_g_i4 = sum_g_i3 + ((long) ((rgb9 >>> 8) & 255));
                    long sum_b_i4 = sum_b_i3 + ((long) (rgb9 & 255));
                    sum_a3 = sum_a4 + sum_a_i5;
                    sum_r3 = sum_r4 + sum_r_i4;
                    sum_g3 = sum_g4 + sum_g_i4;
                    sum_b3 = sum_b4 + sum_b_i4;
                    sp4++;
                    if (sp4 >= div5) {
                        sp4 = 0;
                    }
                    int sp5 = sp4;
                    int rgb10 = stack3[sp5];
                    int a6 = (rgb10 >>> 24) & 255;
                    int r6 = (rgb10 >>> 16) & 255;
                    int g4 = (rgb10 >>> 8) & 255;
                    int b3 = rgb10 & 255;
                    int i30 = sp5;
                    int rgb11 = rgb10;
                    sum_a_o3 = sum_a_o4 + ((long) a6);
                    sum_r_o3 = sum_r_o4 + ((long) r6);
                    sum_g_o3 = sum_g_o4 + ((long) g4);
                    sum_b_o3 = sum_b_o4 + ((long) b3);
                    sum_a_i4 = sum_a_i5 - ((long) a6);
                    sum_r_i3 = sum_r_i4 - ((long) r6);
                    sum_g_i3 = sum_g_i4 - ((long) g4);
                    sum_b_i3 = sum_b_i4 - ((long) b3);
                    y2++;
                    a5 = a6;
                    yp2 = yp4;
                    src_i7 = src_i;
                    mul_sum3 = mul_sum5;
                    hm = hm2;
                    i16 = h;
                    r5 = r6;
                    g3 = g4;
                    rgb7 = rgb11;
                    r = radius;
                }
                int i31 = mul_sum3;
                x2++;
                wm = wm2;
                i16 = h;
                r = radius;
            } else {
                int i32 = wm;
                int i33 = mul_sum3;
                return;
            }
        }
    }
}
