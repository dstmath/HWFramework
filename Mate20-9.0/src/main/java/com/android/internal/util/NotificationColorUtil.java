package com.android.internal.util;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.graphics.drawable.VectorDrawable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
import android.text.style.TextAppearanceSpan;
import android.util.Log;
import android.util.Pair;
import com.android.internal.colorextraction.types.Tonal;
import java.util.Arrays;
import java.util.WeakHashMap;

public class NotificationColorUtil {
    private static final boolean DEBUG = false;
    private static final String TAG = "NotificationColorUtil";
    private static NotificationColorUtil sInstance;
    private static final Object sLock = new Object();
    private final WeakHashMap<Bitmap, Pair<Boolean, Integer>> mGrayscaleBitmapCache = new WeakHashMap<>();
    private final int mGrayscaleIconMaxSize;
    private final ImageUtils mImageUtils = new ImageUtils();

    private static class ColorUtilsFromCompat {
        private static final int MIN_ALPHA_SEARCH_MAX_ITERATIONS = 10;
        private static final int MIN_ALPHA_SEARCH_PRECISION = 1;
        private static final ThreadLocal<double[]> TEMP_ARRAY = new ThreadLocal<>();
        private static final double XYZ_EPSILON = 0.008856d;
        private static final double XYZ_KAPPA = 903.3d;
        private static final double XYZ_WHITE_REFERENCE_X = 95.047d;
        private static final double XYZ_WHITE_REFERENCE_Y = 100.0d;
        private static final double XYZ_WHITE_REFERENCE_Z = 108.883d;

        private ColorUtilsFromCompat() {
        }

        public static int compositeColors(int foreground, int background) {
            int bgAlpha = Color.alpha(background);
            int fgAlpha = Color.alpha(foreground);
            int a = compositeAlpha(fgAlpha, bgAlpha);
            return Color.argb(a, compositeComponent(Color.red(foreground), fgAlpha, Color.red(background), bgAlpha, a), compositeComponent(Color.green(foreground), fgAlpha, Color.green(background), bgAlpha, a), compositeComponent(Color.blue(foreground), fgAlpha, Color.blue(background), bgAlpha, a));
        }

        private static int compositeAlpha(int foregroundAlpha, int backgroundAlpha) {
            return 255 - (((255 - backgroundAlpha) * (255 - foregroundAlpha)) / 255);
        }

        private static int compositeComponent(int fgC, int fgA, int bgC, int bgA, int a) {
            if (a == 0) {
                return 0;
            }
            return (((255 * fgC) * fgA) + ((bgC * bgA) * (255 - fgA))) / (a * 255);
        }

        public static double calculateLuminance(int color) {
            double[] result = getTempDouble3Array();
            colorToXYZ(color, result);
            return result[1] / XYZ_WHITE_REFERENCE_Y;
        }

        public static double calculateContrast(int foreground, int background) {
            if (Color.alpha(background) != 255) {
                Log.wtf(NotificationColorUtil.TAG, "background can not be translucent: #" + Integer.toHexString(background));
            }
            if (Color.alpha(foreground) < 255) {
                foreground = compositeColors(foreground, background);
            }
            double luminance1 = calculateLuminance(foreground) + 0.05d;
            double luminance2 = calculateLuminance(background) + 0.05d;
            return Math.max(luminance1, luminance2) / Math.min(luminance1, luminance2);
        }

        public static void colorToLAB(int color, double[] outLab) {
            RGBToLAB(Color.red(color), Color.green(color), Color.blue(color), outLab);
        }

        public static void RGBToLAB(int r, int g, int b, double[] outLab) {
            RGBToXYZ(r, g, b, outLab);
            XYZToLAB(outLab[0], outLab[1], outLab[2], outLab);
        }

        public static void colorToXYZ(int color, double[] outXyz) {
            RGBToXYZ(Color.red(color), Color.green(color), Color.blue(color), outXyz);
        }

        public static void RGBToXYZ(int r, int g, int b, double[] outXyz) {
            double[] dArr = outXyz;
            if (dArr.length == 3) {
                double sr = ((double) r) / 255.0d;
                double sr2 = sr < 0.04045d ? sr / 12.92d : Math.pow((sr + 0.055d) / 1.055d, 2.4d);
                double sg = ((double) g) / 255.0d;
                double sg2 = sg < 0.04045d ? sg / 12.92d : Math.pow((sg + 0.055d) / 1.055d, 2.4d);
                double sb = ((double) b) / 255.0d;
                double sb2 = sb < 0.04045d ? sb / 12.92d : Math.pow((0.055d + sb) / 1.055d, 2.4d);
                dArr[0] = ((0.4124d * sr2) + (0.3576d * sg2) + (0.1805d * sb2)) * XYZ_WHITE_REFERENCE_Y;
                dArr[1] = ((0.2126d * sr2) + (0.7152d * sg2) + (0.0722d * sb2)) * XYZ_WHITE_REFERENCE_Y;
                dArr[2] = XYZ_WHITE_REFERENCE_Y * ((0.0193d * sr2) + (0.1192d * sg2) + (0.9505d * sb2));
                return;
            }
            int i = r;
            int i2 = g;
            int i3 = b;
            throw new IllegalArgumentException("outXyz must have a length of 3.");
        }

        public static void XYZToLAB(double x, double y, double z, double[] outLab) {
            if (outLab.length == 3) {
                double x2 = pivotXyzComponent(x / XYZ_WHITE_REFERENCE_X);
                double y2 = pivotXyzComponent(y / XYZ_WHITE_REFERENCE_Y);
                double z2 = pivotXyzComponent(z / XYZ_WHITE_REFERENCE_Z);
                outLab[0] = Math.max(0.0d, (116.0d * y2) - 16.0d);
                outLab[1] = 500.0d * (x2 - y2);
                outLab[2] = 200.0d * (y2 - z2);
                return;
            }
            throw new IllegalArgumentException("outLab must have a length of 3.");
        }

        public static void LABToXYZ(double l, double a, double b, double[] outXyz) {
            double fy = (l + 16.0d) / 116.0d;
            double fx = (a / 500.0d) + fy;
            double fz = fy - (b / 200.0d);
            double tmp = Math.pow(fx, 3.0d);
            double xr = tmp > XYZ_EPSILON ? tmp : ((116.0d * fx) - 16.0d) / XYZ_KAPPA;
            double yr = l > 7.9996247999999985d ? Math.pow(fy, 3.0d) : l / XYZ_KAPPA;
            double tmp2 = Math.pow(fz, 3.0d);
            double zr = tmp2 > XYZ_EPSILON ? tmp2 : ((116.0d * fz) - 16.0d) / XYZ_KAPPA;
            outXyz[0] = XYZ_WHITE_REFERENCE_X * xr;
            outXyz[1] = XYZ_WHITE_REFERENCE_Y * yr;
            outXyz[2] = XYZ_WHITE_REFERENCE_Z * zr;
        }

        public static int XYZToColor(double x, double y, double z) {
            double r = (((3.2406d * x) + (-1.5372d * y)) + (-0.4986d * z)) / XYZ_WHITE_REFERENCE_Y;
            double g = (((-0.9689d * x) + (1.8758d * y)) + (0.0415d * z)) / XYZ_WHITE_REFERENCE_Y;
            double b = (((0.0557d * x) + (-0.204d * y)) + (1.057d * z)) / XYZ_WHITE_REFERENCE_Y;
            return Color.rgb(constrain((int) Math.round((r > 0.0031308d ? (Math.pow(r, 0.4166666666666667d) * 1.055d) - 0.055d : 12.92d * r) * 255.0d), 0, 255), constrain((int) Math.round((g > 0.0031308d ? (Math.pow(g, 0.4166666666666667d) * 1.055d) - 0.055d : 12.92d * g) * 255.0d), 0, 255), constrain((int) Math.round(255.0d * (b > 0.0031308d ? (1.055d * Math.pow(b, 0.4166666666666667d)) - 0.055d : 12.92d * b)), 0, 255));
        }

        public static int LABToColor(double l, double a, double b) {
            double[] result = getTempDouble3Array();
            LABToXYZ(l, a, b, result);
            return XYZToColor(result[0], result[1], result[2]);
        }

        private static int constrain(int amount, int low, int high) {
            if (amount < low) {
                return low;
            }
            return amount > high ? high : amount;
        }

        private static float constrain(float amount, float low, float high) {
            if (amount < low) {
                return low;
            }
            return amount > high ? high : amount;
        }

        private static double pivotXyzComponent(double component) {
            if (component > XYZ_EPSILON) {
                return Math.pow(component, 0.3333333333333333d);
            }
            return ((XYZ_KAPPA * component) + 16.0d) / 116.0d;
        }

        public static double[] getTempDouble3Array() {
            double[] result = TEMP_ARRAY.get();
            if (result != null) {
                return result;
            }
            double[] result2 = new double[3];
            TEMP_ARRAY.set(result2);
            return result2;
        }

        public static int HSLToColor(float[] hsl) {
            float h = hsl[0];
            float s = hsl[1];
            float l = hsl[2];
            float c = (1.0f - Math.abs((2.0f * l) - 1.0f)) * s;
            float m = l - (0.5f * c);
            float x = (1.0f - Math.abs(((h / 60.0f) % 2.0f) - 1.0f)) * c;
            int r = 0;
            int g = 0;
            int b = 0;
            switch (((int) h) / 60) {
                case 0:
                    r = Math.round((c + m) * 255.0f);
                    g = Math.round((x + m) * 255.0f);
                    b = Math.round(255.0f * m);
                    break;
                case 1:
                    r = Math.round((x + m) * 255.0f);
                    g = Math.round((c + m) * 255.0f);
                    b = Math.round(255.0f * m);
                    break;
                case 2:
                    r = Math.round(255.0f * m);
                    g = Math.round((c + m) * 255.0f);
                    b = Math.round(255.0f * (x + m));
                    break;
                case 3:
                    r = Math.round(255.0f * m);
                    g = Math.round((x + m) * 255.0f);
                    b = Math.round(255.0f * (c + m));
                    break;
                case 4:
                    r = Math.round((x + m) * 255.0f);
                    g = Math.round(255.0f * m);
                    b = Math.round(255.0f * (c + m));
                    break;
                case 5:
                case 6:
                    r = Math.round((c + m) * 255.0f);
                    g = Math.round(255.0f * m);
                    b = Math.round(255.0f * (x + m));
                    break;
            }
            return Color.rgb(constrain(r, 0, 255), constrain(g, 0, 255), constrain(b, 0, 255));
        }

        public static void colorToHSL(int color, float[] outHsl) {
            RGBToHSL(Color.red(color), Color.green(color), Color.blue(color), outHsl);
        }

        public static void RGBToHSL(int r, int g, int b, float[] outHsl) {
            float h;
            float s;
            float rf = ((float) r) / 255.0f;
            float gf = ((float) g) / 255.0f;
            float bf = ((float) b) / 255.0f;
            float max = Math.max(rf, Math.max(gf, bf));
            float min = Math.min(rf, Math.min(gf, bf));
            float deltaMaxMin = max - min;
            float l = (max + min) / 2.0f;
            if (max == min) {
                s = 0.0f;
                h = 0.0f;
            } else {
                if (max == rf) {
                    h = ((gf - bf) / deltaMaxMin) % 6.0f;
                } else if (max == gf) {
                    h = ((bf - rf) / deltaMaxMin) + 2.0f;
                } else {
                    h = ((rf - gf) / deltaMaxMin) + 4.0f;
                }
                s = deltaMaxMin / (1.0f - Math.abs((2.0f * l) - 1.0f));
            }
            float h2 = (60.0f * h) % 360.0f;
            if (h2 < 0.0f) {
                h2 += 360.0f;
            }
            outHsl[0] = constrain(h2, 0.0f, 360.0f);
            outHsl[1] = constrain(s, 0.0f, 1.0f);
            outHsl[2] = constrain(l, 0.0f, 1.0f);
        }
    }

    public static NotificationColorUtil getInstance(Context context) {
        NotificationColorUtil notificationColorUtil;
        synchronized (sLock) {
            if (sInstance == null) {
                sInstance = new NotificationColorUtil(context);
            }
            notificationColorUtil = sInstance;
        }
        return notificationColorUtil;
    }

    private NotificationColorUtil(Context context) {
        this.mGrayscaleIconMaxSize = context.getResources().getDimensionPixelSize(17104901);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0037, code lost:
        r1 = r6.mImageUtils;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x0039, code lost:
        monitor-enter(r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:18:?, code lost:
        r0 = r6.mImageUtils.isGrayscale(r7);
        r2 = r7.getGenerationId();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x0044, code lost:
        monitor-exit(r1);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:20:0x0045, code lost:
        r3 = sLock;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0047, code lost:
        monitor-enter(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:?, code lost:
        r6.mGrayscaleBitmapCache.put(r7, android.util.Pair.create(java.lang.Boolean.valueOf(r0), java.lang.Integer.valueOf(r2)));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:0x0059, code lost:
        monitor-exit(r3);
     */
    /* JADX WARNING: Code restructure failed: missing block: B:25:0x005a, code lost:
        return r0;
     */
    public boolean isGrayscaleIcon(Bitmap bitmap) {
        if (bitmap.getWidth() > this.mGrayscaleIconMaxSize || bitmap.getHeight() > this.mGrayscaleIconMaxSize) {
            return false;
        }
        synchronized (sLock) {
            Pair<Boolean, Integer> cached = this.mGrayscaleBitmapCache.get(bitmap);
            if (cached != null && ((Integer) cached.second).intValue() == bitmap.getGenerationId()) {
                boolean booleanValue = ((Boolean) cached.first).booleanValue();
                return booleanValue;
            }
        }
    }

    public boolean isGrayscaleIcon(Drawable d) {
        boolean z = false;
        if (d == null) {
            return false;
        }
        if (d instanceof BitmapDrawable) {
            BitmapDrawable bd = (BitmapDrawable) d;
            if (bd.getBitmap() != null && isGrayscaleIcon(bd.getBitmap())) {
                z = true;
            }
            return z;
        } else if (d instanceof AnimationDrawable) {
            AnimationDrawable ad = (AnimationDrawable) d;
            if (ad.getNumberOfFrames() > 0 && isGrayscaleIcon(ad.getFrame(0))) {
                z = true;
            }
            return z;
        } else if (d instanceof VectorDrawable) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isGrayscaleIcon(Context context, Icon icon) {
        if (icon == null) {
            return false;
        }
        switch (icon.getType()) {
            case 1:
                return isGrayscaleIcon(icon.getBitmap());
            case 2:
                return isGrayscaleIcon(context, icon.getResId());
            default:
                return false;
        }
    }

    public boolean isGrayscaleIcon(Context context, int drawableResId) {
        if (drawableResId == 0) {
            return false;
        }
        try {
            return isGrayscaleIcon(context.getDrawable(drawableResId));
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Drawable not found: " + drawableResId);
            return false;
        }
    }

    public CharSequence invertCharSequenceColors(CharSequence charSequence) {
        Object resultSpan;
        if (!(charSequence instanceof Spanned)) {
            return charSequence;
        }
        Spanned ss = (Spanned) charSequence;
        Object[] spans = ss.getSpans(0, ss.length(), Object.class);
        SpannableStringBuilder builder = new SpannableStringBuilder(ss.toString());
        for (Object span : spans) {
            Object resultSpan2 = span;
            if (resultSpan2 instanceof CharacterStyle) {
                resultSpan2 = ((CharacterStyle) span).getUnderlying();
            }
            if (resultSpan2 instanceof TextAppearanceSpan) {
                Object processedSpan = processTextAppearanceSpan((TextAppearanceSpan) span);
                if (processedSpan != resultSpan2) {
                    resultSpan = processedSpan;
                } else {
                    resultSpan = span;
                }
            } else if (resultSpan2 instanceof ForegroundColorSpan) {
                resultSpan = new ForegroundColorSpan(processColor(((ForegroundColorSpan) resultSpan2).getForegroundColor()));
            } else {
                resultSpan = span;
            }
            builder.setSpan(resultSpan, ss.getSpanStart(span), ss.getSpanEnd(span), ss.getSpanFlags(span));
        }
        return builder;
    }

    private TextAppearanceSpan processTextAppearanceSpan(TextAppearanceSpan span) {
        ColorStateList colorStateList = span.getTextColor();
        if (colorStateList != null) {
            int[] colors = colorStateList.getColors();
            boolean changed = false;
            for (int i = 0; i < colors.length; i++) {
                if (ImageUtils.isGrayscale(colors[i])) {
                    if (!changed) {
                        colors = Arrays.copyOf(colors, colors.length);
                    }
                    colors[i] = processColor(colors[i]);
                    changed = true;
                }
            }
            if (changed) {
                TextAppearanceSpan textAppearanceSpan = new TextAppearanceSpan(span.getFamily(), span.getTextStyle(), span.getTextSize(), new ColorStateList(colorStateList.getStates(), colors), span.getLinkTextColor());
                return textAppearanceSpan;
            }
        }
        return span;
    }

    public static CharSequence clearColorSpans(CharSequence charSequence) {
        if (!(charSequence instanceof Spanned)) {
            return charSequence;
        }
        Spanned ss = (Spanned) charSequence;
        Object[] spans = ss.getSpans(0, ss.length(), Object.class);
        SpannableStringBuilder builder = new SpannableStringBuilder(ss.toString());
        for (Object span : spans) {
            Object resultSpan = span;
            if (resultSpan instanceof CharacterStyle) {
                resultSpan = ((CharacterStyle) span).getUnderlying();
            }
            if (resultSpan instanceof TextAppearanceSpan) {
                TextAppearanceSpan originalSpan = (TextAppearanceSpan) resultSpan;
                if (originalSpan.getTextColor() != null) {
                    TextAppearanceSpan textAppearanceSpan = new TextAppearanceSpan(originalSpan.getFamily(), originalSpan.getTextStyle(), originalSpan.getTextSize(), null, originalSpan.getLinkTextColor());
                    resultSpan = textAppearanceSpan;
                }
            } else {
                if (!(resultSpan instanceof ForegroundColorSpan) && !(resultSpan instanceof BackgroundColorSpan)) {
                    resultSpan = span;
                }
            }
            builder.setSpan(resultSpan, ss.getSpanStart(span), ss.getSpanEnd(span), ss.getSpanFlags(span));
        }
        return builder;
    }

    private int processColor(int color) {
        return Color.argb(Color.alpha(color), 255 - Color.red(color), 255 - Color.green(color), 255 - Color.blue(color));
    }

    public static int findContrastColor(int color, int other, boolean findFg, double minRatio) {
        int fg = findFg ? color : other;
        int bg = findFg ? other : color;
        if (ColorUtilsFromCompat.calculateContrast(fg, bg) >= minRatio) {
            return color;
        }
        double[] lab = new double[3];
        ColorUtilsFromCompat.colorToLAB(findFg ? fg : bg, lab);
        double low = 0.0d;
        double high = lab[0];
        double a = lab[1];
        double b = lab[2];
        for (int i = 0; i < 15 && high - low > 1.0E-5d; i++) {
            double l = (low + high) / 2.0d;
            if (findFg) {
                fg = ColorUtilsFromCompat.LABToColor(l, a, b);
            } else {
                bg = ColorUtilsFromCompat.LABToColor(l, a, b);
            }
            if (ColorUtilsFromCompat.calculateContrast(fg, bg) > minRatio) {
                low = l;
            } else {
                high = l;
            }
        }
        return ColorUtilsFromCompat.LABToColor(low, a, b);
    }

    public static int findAlphaToMeetContrast(int color, int backgroundColor, double minRatio) {
        int bg = backgroundColor;
        if (ColorUtilsFromCompat.calculateContrast(color, bg) >= minRatio) {
            return color;
        }
        int startAlpha = Color.alpha(color);
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);
        int low = startAlpha;
        int high = 255;
        for (int i = 0; i < 15 && high - low > 0; i++) {
            int alpha = (low + high) / 2;
            if (ColorUtilsFromCompat.calculateContrast(Color.argb(alpha, r, g, b), bg) > minRatio) {
                high = alpha;
            } else {
                low = alpha;
            }
        }
        return Color.argb(high, r, g, b);
    }

    public static int findContrastColorAgainstDark(int color, int other, boolean findFg, double minRatio) {
        int fg = findFg ? color : other;
        int bg = findFg ? other : color;
        if (ColorUtilsFromCompat.calculateContrast(fg, bg) >= minRatio) {
            return color;
        }
        float[] hsl = new float[3];
        ColorUtilsFromCompat.colorToHSL(findFg ? fg : bg, hsl);
        float low = hsl[2];
        float high = 1.0f;
        for (int i = 0; i < 15 && ((double) (high - low)) > 1.0E-5d; i++) {
            float l = (low + high) / 2.0f;
            hsl[2] = l;
            if (findFg) {
                fg = ColorUtilsFromCompat.HSLToColor(hsl);
            } else {
                bg = ColorUtilsFromCompat.HSLToColor(hsl);
            }
            if (ColorUtilsFromCompat.calculateContrast(fg, bg) > minRatio) {
                high = l;
            } else {
                low = l;
            }
        }
        return findFg ? fg : bg;
    }

    public static int ensureTextContrastOnBlack(int color) {
        return findContrastColorAgainstDark(color, Tonal.MAIN_COLOR_DARK, true, 12.0d);
    }

    public static int ensureLargeTextContrast(int color, int bg, boolean isBgDarker) {
        if (isBgDarker) {
            return findContrastColorAgainstDark(color, bg, true, 3.0d);
        }
        return findContrastColor(color, bg, true, 3.0d);
    }

    public static int ensureTextContrast(int color, int bg, boolean isBgDarker) {
        return ensureContrast(color, bg, isBgDarker, 4.5d);
    }

    public static int ensureContrast(int color, int bg, boolean isBgDarker, double minRatio) {
        if (isBgDarker) {
            return findContrastColorAgainstDark(color, bg, true, minRatio);
        }
        return findContrastColor(color, bg, true, minRatio);
    }

    public static int ensureTextBackgroundColor(int color, int textColor, int hintColor) {
        return findContrastColor(findContrastColor(color, hintColor, false, 3.0d), textColor, false, 4.5d);
    }

    private static String contrastChange(int colorOld, int colorNew, int bg) {
        return String.format("from %.2f:1 to %.2f:1", new Object[]{Double.valueOf(ColorUtilsFromCompat.calculateContrast(colorOld, bg)), Double.valueOf(ColorUtilsFromCompat.calculateContrast(colorNew, bg))});
    }

    public static int resolveColor(Context context, int color) {
        if (color == 0) {
            return context.getColor(17170680);
        }
        return color;
    }

    public static int resolveContrastColor(Context context, int notificationColor, int backgroundColor) {
        return resolveContrastColor(context, notificationColor, backgroundColor, false);
    }

    public static int resolveContrastColor(Context context, int notificationColor, int backgroundColor, boolean isDark) {
        return ensureTextContrast(resolveColor(context, notificationColor), backgroundColor, isDark);
    }

    public static int changeColorLightness(int baseColor, int amount) {
        double[] result = ColorUtilsFromCompat.getTempDouble3Array();
        ColorUtilsFromCompat.colorToLAB(baseColor, result);
        result[0] = Math.max(Math.min(100.0d, result[0] + ((double) amount)), 0.0d);
        return ColorUtilsFromCompat.LABToColor(result[0], result[1], result[2]);
    }

    public static int resolveAmbientColor(Context context, int notificationColor) {
        return ensureTextContrastOnBlack(resolveColor(context, notificationColor));
    }

    public static int resolvePrimaryColor(Context context, int backgroundColor) {
        if (shouldUseDark(backgroundColor)) {
            return context.getColor(17170685);
        }
        return context.getColor(17170684);
    }

    public static int resolveSecondaryColor(Context context, int backgroundColor) {
        if (shouldUseDark(backgroundColor)) {
            return context.getColor(17170689);
        }
        return context.getColor(17170688);
    }

    public static int resolveDefaultColor(Context context, int backgroundColor) {
        if (shouldUseDark(backgroundColor)) {
            return context.getColor(17170680);
        }
        return context.getColor(17170679);
    }

    public static int getShiftedColor(int color, int amount) {
        double[] result = ColorUtilsFromCompat.getTempDouble3Array();
        ColorUtilsFromCompat.colorToLAB(color, result);
        if (result[0] >= 4.0d) {
            result[0] = Math.max(0.0d, result[0] - ((double) amount));
        } else {
            result[0] = Math.min(100.0d, result[0] + ((double) amount));
        }
        return ColorUtilsFromCompat.LABToColor(result[0], result[1], result[2]);
    }

    private static boolean shouldUseDark(int backgroundColor) {
        boolean useDark = false;
        boolean useDark2 = backgroundColor == 0;
        if (useDark2) {
            return useDark2;
        }
        if (ColorUtilsFromCompat.calculateLuminance(backgroundColor) > 0.5d) {
            useDark = true;
        }
        return useDark;
    }

    public static double calculateLuminance(int backgroundColor) {
        return ColorUtilsFromCompat.calculateLuminance(backgroundColor);
    }

    public static double calculateContrast(int foregroundColor, int backgroundColor) {
        return ColorUtilsFromCompat.calculateContrast(foregroundColor, backgroundColor);
    }

    public static boolean satisfiesTextContrast(int backgroundColor, int foregroundColor) {
        return calculateContrast(foregroundColor, backgroundColor) >= 4.5d;
    }

    public static int compositeColors(int foreground, int background) {
        return ColorUtilsFromCompat.compositeColors(foreground, background);
    }

    public static boolean isColorLight(int backgroundColor) {
        return calculateLuminance(backgroundColor) > 0.5d;
    }
}
