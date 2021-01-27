package com.android.internal.util;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.LevelListDrawable;
import android.graphics.drawable.VectorDrawable;
import android.net.wifi.WifiEnterpriseConfig;
import android.util.Log;
import android.util.Pair;
import java.util.WeakHashMap;

public class HwNotificationColorUtil {
    private static final int AVERAGE_BORDER_BETWEEN_SOLID_WHITE_AND_OTHER = 244;
    private static final int INVALID_VARIANCE_VALUE = 0;
    private static final Object SLOCK = new Object();
    public static final int SMALLICON_ASSORTED_COLOR = 1;
    public static final int SMALLICON_NONE = 0;
    public static final int SMALLICON_SOLID_COLOR = 4;
    public static final int SMALLICON_SOLID_WHITE = 2;
    private static final String TAG = "HwNotificationColorUtil";
    private static final int VALID_ALPHA_VALUE = 40;
    private static final int VALID_PIXEL_NUM = 20;
    private static final int VARIANCE_BORDER_BETWEEN_SOLID_AND_ASSORTED = 25;
    private static HwNotificationColorUtil sInstance;
    private final WeakHashMap<Bitmap, Pair<Integer, Integer>> mHWGrayscaleBitmapCache = new WeakHashMap<>();
    private final WeakHashMap<Integer, Integer> mHWGrayscaleDrawableCache = new WeakHashMap<>();

    public static HwNotificationColorUtil getInstance(Context context) {
        HwNotificationColorUtil hwNotificationColorUtil;
        synchronized (SLOCK) {
            if (sInstance == null) {
                sInstance = new HwNotificationColorUtil();
            }
            hwNotificationColorUtil = sInstance;
        }
        return hwNotificationColorUtil;
    }

    public int getSmallIconColorType(Context context, Icon icon) {
        if (icon == null) {
            return 0;
        }
        int type = icon.getType();
        if (type == 1) {
            return getSmallIconColorType(icon.getBitmap());
        }
        if (type != 2) {
            return 0;
        }
        return getSmallIconColorType(context, icon.getResId());
    }

    private int getSmallIconColorType(Bitmap bitmap) {
        if (bitmap == null) {
            return 0;
        }
        synchronized (SLOCK) {
            Pair<Integer, Integer> cached = this.mHWGrayscaleBitmapCache.get(bitmap);
            if (cached != null && cached.second.intValue() == bitmap.getGenerationId()) {
                return cached.first.intValue();
            }
        }
        int generationId = bitmap.getGenerationId();
        Bitmap bmp = compressPicture(bitmap);
        int bitmapAvgColor = getBitmapAvgColor(bmp);
        if (Color.red(bitmapAvgColor) >= 244 && Color.green(bitmapAvgColor) >= 244 && Color.blue(bitmapAvgColor) >= 244) {
            synchronized (SLOCK) {
                this.mHWGrayscaleBitmapCache.put(bitmap, Pair.create(2, Integer.valueOf(generationId)));
            }
            return 2;
        } else if (getColorVariance(bmp, bitmapAvgColor) < 25.0f) {
            synchronized (SLOCK) {
                this.mHWGrayscaleBitmapCache.put(bitmap, Pair.create(4, Integer.valueOf(generationId)));
            }
            return 4;
        } else {
            synchronized (SLOCK) {
                this.mHWGrayscaleBitmapCache.put(bitmap, Pair.create(1, Integer.valueOf(generationId)));
            }
            return 1;
        }
    }

    private int getSmallIconColorType(Context context, int drawableResId) {
        if (drawableResId <= 0) {
            return 0;
        }
        try {
            synchronized (SLOCK) {
                Integer cached = this.mHWGrayscaleDrawableCache.get(Integer.valueOf(drawableResId));
                if (cached != null) {
                    return cached.intValue();
                }
                int result = getSmallIconColorType(context.getDrawable(drawableResId));
                synchronized (SLOCK) {
                    this.mHWGrayscaleDrawableCache.put(Integer.valueOf(drawableResId), Integer.valueOf(result));
                }
                return result;
            }
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Drawable not found: " + drawableResId);
            return 0;
        }
    }

    private int getSmallIconColorType(Drawable d) {
        if (d == null) {
            return 0;
        }
        if (d instanceof BitmapDrawable) {
            BitmapDrawable bd = (BitmapDrawable) d;
            if (bd.getBitmap() == null) {
                return 0;
            }
            return getSmallIconColorType(bd.getBitmap());
        } else if (d instanceof VectorDrawable) {
            return getSmallIconColorType(drawableToBitamp(d));
        } else {
            if (d instanceof AnimationDrawable) {
                AnimationDrawable ad = (AnimationDrawable) d;
                if (ad.getNumberOfFrames() <= 0) {
                    return 0;
                }
                return getSmallIconColorType(ad.getFrame(0));
            } else if ((d instanceof LevelListDrawable) || (d instanceof LayerDrawable)) {
                return getSmallIconColorType(drawableToBitamp(d));
            } else {
                return 0;
            }
        }
    }

    private Bitmap compressPicture(Bitmap bmp) {
        int hr = bmp.getHeight();
        int wr = bmp.getWidth();
        if (wr <= 64 && hr <= 64) {
            return bmp;
        }
        Bitmap compactBitmap = Bitmap.createBitmap(64, 64, Bitmap.Config.ARGB_8888);
        Paint compactBitmapPaint = new Paint(1);
        compactBitmapPaint.setFilterBitmap(true);
        Canvas compactBitmapCanvas = new Canvas(compactBitmap);
        Matrix matrix = new Matrix();
        matrix.reset();
        matrix.setScale(64.0f / ((float) wr), 64.0f / ((float) hr), 0.0f, 0.0f);
        compactBitmapCanvas.drawColor(0, PorterDuff.Mode.SRC);
        compactBitmapCanvas.drawBitmap(bmp, matrix, compactBitmapPaint);
        return compactBitmap;
    }

    private static Bitmap drawableToBitamp(Drawable drawable) {
        Bitmap.Config config;
        int w = drawable.getIntrinsicWidth();
        int h = drawable.getIntrinsicHeight();
        if (w <= 0 || h <= 0) {
            Log.e(TAG, "drawableToBitamp invalide size " + w + WifiEnterpriseConfig.CA_CERT_ALIAS_DELIMITER + h);
            return null;
        }
        if (drawable.getOpacity() != -1) {
            config = Bitmap.Config.ARGB_8888;
        } else {
            config = Bitmap.Config.RGB_565;
        }
        Bitmap bitmap = Bitmap.createBitmap(w, h, config);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, w, h);
        drawable.draw(canvas);
        return bitmap;
    }

    private int getBitmapAvgColor(Bitmap bmp) {
        int hr = bmp.getHeight();
        int wr = bmp.getWidth();
        int[] mBufPixels = new int[(wr * hr)];
        bmp.getPixels(mBufPixels, 0, wr, 0, 0, wr, hr);
        int num = 0;
        int sumr = 0;
        int sumg = 0;
        int sumb = 0;
        for (int y = 0; y < hr; y++) {
            for (int x = 0; x < wr; x++) {
                int a = Color.alpha(mBufPixels[(y * wr) + x]);
                int r = Color.red(mBufPixels[(y * wr) + x]);
                int g = Color.green(mBufPixels[(y * wr) + x]);
                int b = Color.blue(mBufPixels[(y * wr) + x]);
                if (a > 40) {
                    sumr += r;
                    sumg += g;
                    sumb += b;
                    num++;
                }
            }
        }
        if (num <= 20) {
            return -1;
        }
        return ((sumr / num) << 16) | ((sumg / num) << 8) | (sumb / num);
    }

    private float getColorVariance(Bitmap bmp, int avg) {
        int hr = bmp.getHeight();
        int wr = bmp.getWidth();
        int[] mBufPixels = new int[(wr * hr)];
        bmp.getPixels(mBufPixels, 0, wr, 0, 0, wr, hr);
        int num = 0;
        int avgr = Color.red(avg);
        int avgg = Color.green(avg);
        int avgb = Color.blue(avg);
        float v = 0.0f;
        for (int y = 0; y < hr; y++) {
            for (int x = 0; x < wr; x++) {
                if (Color.alpha(mBufPixels[(y * wr) + x]) > 40) {
                    int r = Color.red(mBufPixels[(y * wr) + x]);
                    int g = Color.green(mBufPixels[(y * wr) + x]);
                    int b = Color.blue(mBufPixels[(y * wr) + x]);
                    v += (float) (((r - avgr) * (r - avgr)) + ((g - avgg) * (g - avgg)) + ((b - avgb) * (b - avgb)));
                    num++;
                }
            }
        }
        if (num <= 20) {
            return 0.0f;
        }
        return v / ((float) num);
    }
}
