package com.android.internal.util;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Resources.NotFoundException;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.graphics.drawable.VectorDrawable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.CharacterStyle;
import android.text.style.ForegroundColorSpan;
import android.text.style.TextAppearanceSpan;
import android.util.Log;
import android.util.Pair;
import com.android.internal.R;
import java.util.Arrays;
import java.util.WeakHashMap;

public class NotificationColorUtil {
    private static final boolean DEBUG = false;
    private static final String TAG = "NotificationColorUtil";
    private static NotificationColorUtil sInstance;
    private static final Object sLock = new Object();
    private final WeakHashMap<Bitmap, Pair<Boolean, Integer>> mGrayscaleBitmapCache = new WeakHashMap();
    private final int mGrayscaleIconMaxSize;
    private final ImageUtils mImageUtils = new ImageUtils();

    private static class ColorUtilsFromCompat {
        private static final int MIN_ALPHA_SEARCH_MAX_ITERATIONS = 10;
        private static final int MIN_ALPHA_SEARCH_PRECISION = 1;
        private static final ThreadLocal<double[]> TEMP_ARRAY = new ThreadLocal();
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
            return (((fgC * 255) * fgA) + ((bgC * bgA) * (255 - fgA))) / (a * 255);
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
            if (outXyz.length != 3) {
                throw new IllegalArgumentException("outXyz must have a length of 3.");
            }
            double sr = ((double) r) / 255.0d;
            sr = sr < 0.04045d ? sr / 12.92d : Math.pow((0.055d + sr) / 1.055d, 2.4d);
            double sg = ((double) g) / 255.0d;
            sg = sg < 0.04045d ? sg / 12.92d : Math.pow((0.055d + sg) / 1.055d, 2.4d);
            double sb = ((double) b) / 255.0d;
            sb = sb < 0.04045d ? sb / 12.92d : Math.pow((0.055d + sb) / 1.055d, 2.4d);
            outXyz[0] = (((0.4124d * sr) + (0.3576d * sg)) + (0.1805d * sb)) * XYZ_WHITE_REFERENCE_Y;
            outXyz[1] = (((0.2126d * sr) + (0.7152d * sg)) + (0.0722d * sb)) * XYZ_WHITE_REFERENCE_Y;
            outXyz[2] = (((0.0193d * sr) + (0.1192d * sg)) + (0.9505d * sb)) * XYZ_WHITE_REFERENCE_Y;
        }

        public static void XYZToLAB(double x, double y, double z, double[] outLab) {
            if (outLab.length != 3) {
                throw new IllegalArgumentException("outLab must have a length of 3.");
            }
            x = pivotXyzComponent(x / XYZ_WHITE_REFERENCE_X);
            y = pivotXyzComponent(y / XYZ_WHITE_REFERENCE_Y);
            z = pivotXyzComponent(z / XYZ_WHITE_REFERENCE_Z);
            outLab[0] = Math.max(0.0d, (116.0d * y) - 16.0d);
            outLab[1] = (x - y) * 500.0d;
            outLab[2] = (y - z) * 200.0d;
        }

        public static void LABToXYZ(double l, double a, double b, double[] outXyz) {
            double fy = (16.0d + l) / 116.0d;
            double fx = (a / 500.0d) + fy;
            double fz = fy - (b / 200.0d);
            double tmp = Math.pow(fx, 3.0d);
            double xr = tmp > XYZ_EPSILON ? tmp : ((116.0d * fx) - 16.0d) / XYZ_KAPPA;
            double yr = l > 7.9996247999999985d ? Math.pow(fy, 3.0d) : l / XYZ_KAPPA;
            tmp = Math.pow(fz, 3.0d);
            double zr = tmp > XYZ_EPSILON ? tmp : ((116.0d * fz) - 16.0d) / XYZ_KAPPA;
            outXyz[0] = XYZ_WHITE_REFERENCE_X * xr;
            outXyz[1] = XYZ_WHITE_REFERENCE_Y * yr;
            outXyz[2] = XYZ_WHITE_REFERENCE_Z * zr;
        }

        public static int XYZToColor(double x, double y, double z) {
            double r = (((3.2406d * x) + (-1.5372d * y)) + (-0.4986d * z)) / XYZ_WHITE_REFERENCE_Y;
            double g = (((-0.9689d * x) + (1.8758d * y)) + (0.0415d * z)) / XYZ_WHITE_REFERENCE_Y;
            double b = (((0.0557d * x) + (-0.204d * y)) + (1.057d * z)) / XYZ_WHITE_REFERENCE_Y;
            return Color.rgb(constrain((int) Math.round(255.0d * (r > 0.0031308d ? (Math.pow(r, 0.4166666666666667d) * 1.055d) - 0.055d : r * 12.92d)), 0, 255), constrain((int) Math.round(255.0d * (g > 0.0031308d ? (Math.pow(g, 0.4166666666666667d) * 1.055d) - 0.055d : g * 12.92d)), 0, 255), constrain((int) Math.round(255.0d * (b > 0.0031308d ? (Math.pow(b, 0.4166666666666667d) * 1.055d) - 0.055d : b * 12.92d)), 0, 255));
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
            double[] result = (double[]) TEMP_ARRAY.get();
            if (result != null) {
                return result;
            }
            result = new double[3];
            TEMP_ARRAY.set(result);
            return result;
        }

        public static int HSLToColor(float[] hsl) {
            float h = hsl[0];
            float s = hsl[1];
            float l = hsl[2];
            float c = (1.0f - Math.abs((2.0f * l) - 1.0f)) * s;
            float m = l - (0.5f * c);
            float x = c * (1.0f - Math.abs(((h / 60.0f) % 2.0f) - 1.0f));
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
                    b = Math.round((x + m) * 255.0f);
                    break;
                case 3:
                    r = Math.round(255.0f * m);
                    g = Math.round((x + m) * 255.0f);
                    b = Math.round((c + m) * 255.0f);
                    break;
                case 4:
                    r = Math.round((x + m) * 255.0f);
                    g = Math.round(255.0f * m);
                    b = Math.round((c + m) * 255.0f);
                    break;
                case 5:
                case 6:
                    r = Math.round((c + m) * 255.0f);
                    g = Math.round(255.0f * m);
                    b = Math.round((x + m) * 255.0f);
                    break;
            }
            return Color.rgb(constrain(r, 0, 255), constrain(g, 0, 255), constrain(b, 0, 255));
        }

        public static void colorToHSL(int color, float[] outHsl) {
            RGBToHSL(Color.red(color), Color.green(color), Color.blue(color), outHsl);
        }

        public static void RGBToHSL(int r, int g, int b, float[] outHsl) {
            float s;
            float h;
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
            h = (60.0f * h) % 360.0f;
            if (h < 0.0f) {
                h += 360.0f;
            }
            outHsl[0] = constrain(h, 0.0f, 360.0f);
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
        this.mGrayscaleIconMaxSize = context.getResources().getDimensionPixelSize(R.dimen.notification_large_icon_width);
    }

    /* JADX WARNING: Missing block: B:17:0x0038, code:
            r4 = r7.mImageUtils;
     */
    /* JADX WARNING: Missing block: B:18:0x003a, code:
            monitor-enter(r4);
     */
    /* JADX WARNING: Missing block: B:20:?, code:
            r2 = r7.mImageUtils.isGrayscale(r8);
            r1 = r8.getGenerationId();
     */
    /* JADX WARNING: Missing block: B:21:0x0045, code:
            monitor-exit(r4);
     */
    /* JADX WARNING: Missing block: B:22:0x0046, code:
            r4 = sLock;
     */
    /* JADX WARNING: Missing block: B:23:0x0048, code:
            monitor-enter(r4);
     */
    /* JADX WARNING: Missing block: B:25:?, code:
            r7.mGrayscaleBitmapCache.put(r8, android.util.Pair.create(java.lang.Boolean.valueOf(r2), java.lang.Integer.valueOf(r1)));
     */
    /* JADX WARNING: Missing block: B:26:0x005a, code:
            monitor-exit(r4);
     */
    /* JADX WARNING: Missing block: B:27:0x005b, code:
            return r2;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean isGrayscaleIcon(Bitmap bitmap) {
        if (bitmap.getWidth() > this.mGrayscaleIconMaxSize || bitmap.getHeight() > this.mGrayscaleIconMaxSize) {
            return false;
        }
        synchronized (sLock) {
            Pair<Boolean, Integer> cached = (Pair) this.mGrayscaleBitmapCache.get(bitmap);
            if (cached == null || ((Integer) cached.second).intValue() != bitmap.getGenerationId()) {
            } else {
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
            if (bd.getBitmap() != null) {
                z = isGrayscaleIcon(bd.getBitmap());
            }
            return z;
        } else if (d instanceof AnimationDrawable) {
            AnimationDrawable ad = (AnimationDrawable) d;
            if (ad.getNumberOfFrames() > 0) {
                z = isGrayscaleIcon(ad.getFrame(0));
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
        } catch (NotFoundException e) {
            Log.e(TAG, "Drawable not found: " + drawableResId);
            return false;
        }
    }

    public CharSequence invertCharSequenceColors(CharSequence charSequence) {
        if (!(charSequence instanceof Spanned)) {
            return charSequence;
        }
        Spanned ss = (Spanned) charSequence;
        Object[] spans = ss.getSpans(0, ss.length(), Object.class);
        SpannableStringBuilder builder = new SpannableStringBuilder(ss.toString());
        for (CharacterStyle span : spans) {
            Object resultSpan;
            CharacterStyle resultSpan2 = span;
            if (span instanceof CharacterStyle) {
                resultSpan2 = span.getUnderlying();
            }
            if (resultSpan2 instanceof TextAppearanceSpan) {
                CharacterStyle processedSpan = processTextAppearanceSpan((TextAppearanceSpan) span);
                if (processedSpan != resultSpan2) {
                    resultSpan = processedSpan;
                } else {
                    resultSpan2 = span;
                }
            } else if (resultSpan2 instanceof ForegroundColorSpan) {
                resultSpan = new ForegroundColorSpan(processColor(((ForegroundColorSpan) resultSpan2).getForegroundColor()));
            } else {
                resultSpan2 = span;
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
                return new TextAppearanceSpan(span.getFamily(), span.getTextStyle(), span.getTextSize(), new ColorStateList(colorStateList.getStates(), colors), span.getLinkTextColor());
            }
        }
        return span;
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
        int i;
        double[] lab = new double[3];
        if (findFg) {
            i = fg;
        } else {
            i = bg;
        }
        ColorUtilsFromCompat.colorToLAB(i, lab);
        double low = 0.0d;
        double high = lab[0];
        double a = lab[1];
        double b = lab[2];
        for (int i2 = 0; i2 < 15 && high - low > 1.0E-5d; i2++) {
            double l = (low + high) / 2.0d;
            if (findFg) {
                fg = ColorUtilsFromCompat.LABToColor(l, a, b);
            } else {
                bg = ColorUtilsFromCompat.LABToColor(l, a, b);
            }
            if (ColorUtilsFromCompat.calculateContrast(fg, bg) > minRatio) {
            }
            low = l;
        }
        return ColorUtilsFromCompat.LABToColor(low, a, b);
    }

    public static int findAlphaToMeetContrast(int color, int backgroundColor, double minRatio) {
        int fg = color;
        int bg = backgroundColor;
        if (ColorUtilsFromCompat.calculateContrast(color, backgroundColor) >= minRatio) {
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
            if (ColorUtilsFromCompat.calculateContrast(Color.argb(alpha, r, g, b), backgroundColor) > minRatio) {
            }
            high = alpha;
        }
        return Color.argb(high, r, g, b);
    }

    public static int findContrastColorAgainstDark(int color, int other, boolean findFg, double minRatio) {
        int fg = findFg ? color : other;
        int bg = findFg ? other : color;
        if (ColorUtilsFromCompat.calculateContrast(fg, bg) >= minRatio) {
            return color;
        }
        int i;
        float[] hsl = new float[3];
        if (findFg) {
            i = fg;
        } else {
            i = bg;
        }
        ColorUtilsFromCompat.colorToHSL(i, hsl);
        float low = hsl[2];
        float high = 1.0f;
        for (int i2 = 0; i2 < 15 && ((double) (high - low)) > 1.0E-5d; i2++) {
            float l = (low + high) / 2.0f;
            hsl[2] = l;
            if (findFg) {
                fg = ColorUtilsFromCompat.HSLToColor(hsl);
            } else {
                bg = ColorUtilsFromCompat.HSLToColor(hsl);
            }
            if (ColorUtilsFromCompat.calculateContrast(fg, bg) > minRatio) {
            }
            high = l;
        }
        if (!findFg) {
            fg = bg;
        }
        return fg;
    }

    public static int ensureTextContrastOnBlack(int color) {
        return findContrastColorAgainstDark(color, -16777216, true, 12.0d);
    }

    public static int ensureLargeTextContrast(int color, int bg) {
        return findContrastColor(color, bg, true, 3.0d);
    }

    private static int ensureTextContrast(int color, int bg) {
        return findContrastColor(color, bg, true, 4.5d);
    }

    public static int ensureTextBackgroundColor(int color, int textColor, int hintColor) {
        return findContrastColor(findContrastColor(color, hintColor, false, 3.0d), textColor, false, 4.5d);
    }

    private static String contrastChange(int colorOld, int colorNew, int bg) {
        return String.format("from %.2f:1 to %.2f:1", new Object[]{Double.valueOf(ColorUtilsFromCompat.calculateContrast(colorOld, bg)), Double.valueOf(ColorUtilsFromCompat.calculateContrast(colorNew, bg))});
    }

    public static int resolveColor(Context context, int color) {
        if (color == 0) {
            return context.getColor(R.color.notification_icon_default_color);
        }
        return color;
    }

    public static int resolveContrastColor(Context context, int notificationColor, int backgroundColor) {
        if (notificationColor == 0) {
            return context.getColor(33882282);
        }
        int resolvedColor = resolveColor(context, notificationColor);
        int i = resolvedColor;
        return ensureTextContrast(ensureLargeTextContrast(resolvedColor, context.getColor(R.color.notification_action_list)), backgroundColor);
    }

    public static int changeColorLightness(int baseColor, int amount) {
        double[] result = ColorUtilsFromCompat.getTempDouble3Array();
        ColorUtilsFromCompat.colorToLAB(baseColor, result);
        result[0] = Math.max(Math.min(100.0d, result[0] + ((double) amount)), 0.0d);
        return ColorUtilsFromCompat.LABToColor(result[0], result[1], result[2]);
    }

    public static int resolveAmbientColor(Context context, int notificationColor) {
        int resolvedColor = resolveColor(context, notificationColor);
        int i = resolvedColor;
        return ensureTextContrastOnBlack(resolvedColor);
    }

    public static int resolvePrimaryColor(Context context, int backgroundColor) {
        if (shouldUseDark(backgroundColor)) {
            return context.getColor(R.color.notification_primary_text_color_light);
        }
        return context.getColor(R.color.notification_primary_text_color_dark);
    }

    public static int resolveSecondaryColor(Context context, int backgroundColor) {
        if (shouldUseDark(backgroundColor)) {
            return context.getColor(R.color.notification_secondary_text_color_light);
        }
        return context.getColor(R.color.notification_secondary_text_color_dark);
    }

    public static int resolveActionBarColor(Context context, int backgroundColor) {
        if (backgroundColor == 0) {
            return context.getColor(R.color.notification_action_list);
        }
        return getShiftedColor(backgroundColor, 7);
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
        boolean useDark = backgroundColor == 0;
        if (useDark) {
            return useDark;
        }
        return ColorUtilsFromCompat.calculateLuminance(backgroundColor) > 0.5d;
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
