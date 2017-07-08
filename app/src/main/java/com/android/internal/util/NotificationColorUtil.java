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
import android.text.style.TextAppearanceSpan;
import android.util.Log;
import android.util.Pair;
import com.android.internal.R;
import com.android.internal.logging.MetricsProto.MetricsEvent;
import huawei.cust.HwCfgFilePolicy;
import java.util.Arrays;
import java.util.WeakHashMap;

public class NotificationColorUtil {
    private static final boolean DEBUG = false;
    private static final String TAG = "NotificationColorUtil";
    private static NotificationColorUtil sInstance;
    private static final Object sLock = null;
    private final WeakHashMap<Bitmap, Pair<Boolean, Integer>> mGrayscaleBitmapCache;
    private final int mGrayscaleIconMaxSize;
    private final ImageUtils mImageUtils;

    private static class ColorUtilsFromCompat {
        private static final int MIN_ALPHA_SEARCH_MAX_ITERATIONS = 10;
        private static final int MIN_ALPHA_SEARCH_PRECISION = 1;
        private static final ThreadLocal<double[]> TEMP_ARRAY = null;
        private static final double XYZ_EPSILON = 0.008856d;
        private static final double XYZ_KAPPA = 903.3d;
        private static final double XYZ_WHITE_REFERENCE_X = 95.047d;
        private static final double XYZ_WHITE_REFERENCE_Y = 100.0d;
        private static final double XYZ_WHITE_REFERENCE_Z = 108.883d;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.util.NotificationColorUtil.ColorUtilsFromCompat.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.util.NotificationColorUtil.ColorUtilsFromCompat.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.android.internal.util.NotificationColorUtil.ColorUtilsFromCompat.<clinit>():void");
        }

        private ColorUtilsFromCompat() {
        }

        public static int compositeColors(int foreground, int background) {
            int bgAlpha = Color.alpha(background);
            int fgAlpha = Color.alpha(foreground);
            int a = compositeAlpha(fgAlpha, bgAlpha);
            return Color.argb(a, compositeComponent(Color.red(foreground), fgAlpha, Color.red(background), bgAlpha, a), compositeComponent(Color.green(foreground), fgAlpha, Color.green(background), bgAlpha, a), compositeComponent(Color.blue(foreground), fgAlpha, Color.blue(background), bgAlpha, a));
        }

        private static int compositeAlpha(int foregroundAlpha, int backgroundAlpha) {
            return 255 - (((255 - backgroundAlpha) * (255 - foregroundAlpha)) / MetricsEvent.ACTION_DOUBLE_TAP_POWER_CAMERA_GESTURE);
        }

        private static int compositeComponent(int fgC, int fgA, int bgC, int bgA, int a) {
            if (a == 0) {
                return 0;
            }
            return (((fgC * MetricsEvent.ACTION_DOUBLE_TAP_POWER_CAMERA_GESTURE) * fgA) + ((bgC * bgA) * (255 - fgA))) / (a * MetricsEvent.ACTION_DOUBLE_TAP_POWER_CAMERA_GESTURE);
        }

        public static double calculateLuminance(int color) {
            double[] result = getTempDouble3Array();
            colorToXYZ(color, result);
            return result[MIN_ALPHA_SEARCH_PRECISION] / XYZ_WHITE_REFERENCE_Y;
        }

        public static double calculateContrast(int foreground, int background) {
            if (Color.alpha(background) != MetricsEvent.ACTION_DOUBLE_TAP_POWER_CAMERA_GESTURE) {
                throw new IllegalArgumentException("background can not be translucent: #" + Integer.toHexString(background));
            }
            if (Color.alpha(foreground) < MetricsEvent.ACTION_DOUBLE_TAP_POWER_CAMERA_GESTURE) {
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
            XYZToLAB(outLab[0], outLab[MIN_ALPHA_SEARCH_PRECISION], outLab[2], outLab);
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
            outXyz[MIN_ALPHA_SEARCH_PRECISION] = (((0.2126d * sr) + (0.7152d * sg)) + (0.0722d * sb)) * XYZ_WHITE_REFERENCE_Y;
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
            outLab[MIN_ALPHA_SEARCH_PRECISION] = (x - y) * 500.0d;
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
            outXyz[MIN_ALPHA_SEARCH_PRECISION] = XYZ_WHITE_REFERENCE_Y * yr;
            outXyz[2] = XYZ_WHITE_REFERENCE_Z * zr;
        }

        public static int XYZToColor(double x, double y, double z) {
            double r = (((3.2406d * x) + (-1.5372d * y)) + (-0.4986d * z)) / XYZ_WHITE_REFERENCE_Y;
            double g = (((-0.9689d * x) + (1.8758d * y)) + (0.0415d * z)) / XYZ_WHITE_REFERENCE_Y;
            double b = (((0.0557d * x) + (-0.204d * y)) + (1.057d * z)) / XYZ_WHITE_REFERENCE_Y;
            return Color.rgb(constrain((int) Math.round(255.0d * (r > 0.0031308d ? (Math.pow(r, 0.4166666666666667d) * 1.055d) - 0.055d : r * 12.92d)), 0, MetricsEvent.ACTION_DOUBLE_TAP_POWER_CAMERA_GESTURE), constrain((int) Math.round(255.0d * (g > 0.0031308d ? (Math.pow(g, 0.4166666666666667d) * 1.055d) - 0.055d : g * 12.92d)), 0, MetricsEvent.ACTION_DOUBLE_TAP_POWER_CAMERA_GESTURE), constrain((int) Math.round(255.0d * (b > 0.0031308d ? (Math.pow(b, 0.4166666666666667d) * 1.055d) - 0.055d : b * 12.92d)), 0, MetricsEvent.ACTION_DOUBLE_TAP_POWER_CAMERA_GESTURE));
        }

        public static int LABToColor(double l, double a, double b) {
            double[] result = getTempDouble3Array();
            LABToXYZ(l, a, b, result);
            return XYZToColor(result[0], result[MIN_ALPHA_SEARCH_PRECISION], result[2]);
        }

        private static int constrain(int amount, int low, int high) {
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

        private static double[] getTempDouble3Array() {
            double[] result = (double[]) TEMP_ARRAY.get();
            if (result != null) {
                return result;
            }
            result = new double[3];
            TEMP_ARRAY.set(result);
            return result;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.util.NotificationColorUtil.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.util.NotificationColorUtil.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.util.NotificationColorUtil.<clinit>():void");
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
        this.mImageUtils = new ImageUtils();
        this.mGrayscaleBitmapCache = new WeakHashMap();
        this.mGrayscaleIconMaxSize = context.getResources().getDimensionPixelSize(R.dimen.notification_large_icon_width);
    }

    public boolean isGrayscaleIcon(Bitmap bitmap) {
        if (bitmap.getWidth() > this.mGrayscaleIconMaxSize || bitmap.getHeight() > this.mGrayscaleIconMaxSize) {
            return DEBUG;
        }
        synchronized (sLock) {
            Pair<Boolean, Integer> cached = (Pair) this.mGrayscaleBitmapCache.get(bitmap);
            if (cached == null || ((Integer) cached.second).intValue() != bitmap.getGenerationId()) {
                boolean result;
                int generationId;
                synchronized (this.mImageUtils) {
                    result = this.mImageUtils.isGrayscale(bitmap);
                    generationId = bitmap.getGenerationId();
                }
                synchronized (sLock) {
                    this.mGrayscaleBitmapCache.put(bitmap, Pair.create(Boolean.valueOf(result), Integer.valueOf(generationId)));
                }
                return result;
            }
            boolean booleanValue = ((Boolean) cached.first).booleanValue();
            return booleanValue;
        }
    }

    public boolean isGrayscaleIcon(Drawable d) {
        boolean z = DEBUG;
        if (d == null) {
            return DEBUG;
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
            return DEBUG;
        }
    }

    public boolean isGrayscaleIcon(Context context, Icon icon) {
        if (icon == null) {
            return DEBUG;
        }
        switch (icon.getType()) {
            case HwCfgFilePolicy.EMUI /*1*/:
                return isGrayscaleIcon(icon.getBitmap());
            case HwCfgFilePolicy.PC /*2*/:
                return isGrayscaleIcon(context, icon.getResId());
            default:
                return DEBUG;
        }
    }

    public boolean isGrayscaleIcon(Context context, int drawableResId) {
        if (drawableResId == 0) {
            return DEBUG;
        }
        try {
            return isGrayscaleIcon(context.getDrawable(drawableResId));
        } catch (NotFoundException e) {
            Log.e(TAG, "Drawable not found: " + drawableResId);
            return DEBUG;
        }
    }

    public CharSequence invertCharSequenceColors(CharSequence charSequence) {
        if (!(charSequence instanceof Spanned)) {
            return charSequence;
        }
        Spanned ss = (Spanned) charSequence;
        Object[] spans = ss.getSpans(0, ss.length(), Object.class);
        SpannableStringBuilder builder = new SpannableStringBuilder(ss.toString());
        for (Object span : spans) {
            Object resultSpan = span;
            if (span instanceof TextAppearanceSpan) {
                resultSpan = processTextAppearanceSpan((TextAppearanceSpan) span);
            }
            builder.setSpan(resultSpan, ss.getSpanStart(span), ss.getSpanEnd(span), ss.getSpanFlags(span));
        }
        return builder;
    }

    private TextAppearanceSpan processTextAppearanceSpan(TextAppearanceSpan span) {
        ColorStateList colorStateList = span.getTextColor();
        if (colorStateList != null) {
            int[] colors = colorStateList.getColors();
            boolean changed = DEBUG;
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

    private static int findContrastColor(int color, int other, boolean findFg, double minRatio) {
        int bg;
        int fg = findFg ? color : other;
        if (findFg) {
            bg = other;
        } else {
            bg = color;
        }
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
                low = l;
            } else {
                high = l;
            }
        }
        return ColorUtilsFromCompat.LABToColor(low, a, b);
    }

    private static int ensureLargeTextContrast(int color, int bg) {
        return findContrastColor(color, bg, true, 3.0d);
    }

    private static int ensureTextContrast(int color, int bg) {
        return findContrastColor(color, bg, true, 4.5d);
    }

    public static int ensureTextBackgroundColor(int color, int textColor, int hintColor) {
        return findContrastColor(findContrastColor(color, hintColor, DEBUG, 3.0d), textColor, DEBUG, 4.5d);
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

    public static int resolveContrastColor(Context context, int notificationColor) {
        if (notificationColor == 0) {
            return context.getColor(androidhwext.R.color.accent_emui);
        }
        int resolvedColor = resolveColor(context, notificationColor);
        int i = resolvedColor;
        i = ensureTextContrast(ensureLargeTextContrast(resolvedColor, context.getColor(R.color.notification_action_list)), context.getColor(R.color.notification_material_background_color));
        return i != resolvedColor ? i : i;
    }
}
