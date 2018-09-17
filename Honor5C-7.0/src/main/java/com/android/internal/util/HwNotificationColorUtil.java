package com.android.internal.util;

import android.content.Context;
import android.content.res.Resources.NotFoundException;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.graphics.drawable.LevelListDrawable;
import android.graphics.drawable.VectorDrawable;
import android.util.Log;
import android.util.Pair;
import java.util.WeakHashMap;

public class HwNotificationColorUtil {
    private static final int AVERAGE_BORDER_BETWEEN_SOLID_WHITE_AND_OTHER = 244;
    private static final int INVALID_VARIANCE_VALUE = 0;
    public static final int SMALLICON_ASSORTED_COLOR = 1;
    public static final int SMALLICON_NONE = 0;
    public static final int SMALLICON_SOLID_COLOR = 4;
    public static final int SMALLICON_SOLID_WHITE = 2;
    private static final String TAG = "HwNotificationColorUtil";
    private static final int VALID_ALPHA_VALUE = 40;
    private static final int VALID_PIXEL_NUM = 20;
    private static final int VARIANCE_BORDER_BETWEEN_SOLID_AND_ASSORTED = 25;
    private static HwNotificationColorUtil sInstance;
    private static final Object sLock = null;
    private final WeakHashMap<Bitmap, Pair<Integer, Integer>> mHWGrayscaleBitmapCache;
    private final WeakHashMap<Integer, Integer> mHWGrayscaleDrawableCache;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.util.HwNotificationColorUtil.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.util.HwNotificationColorUtil.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.util.HwNotificationColorUtil.<clinit>():void");
    }

    public HwNotificationColorUtil() {
        this.mHWGrayscaleBitmapCache = new WeakHashMap();
        this.mHWGrayscaleDrawableCache = new WeakHashMap();
    }

    public static HwNotificationColorUtil getInstance(Context context) {
        HwNotificationColorUtil hwNotificationColorUtil;
        synchronized (sLock) {
            if (sInstance == null) {
                sInstance = new HwNotificationColorUtil();
            }
            hwNotificationColorUtil = sInstance;
        }
        return hwNotificationColorUtil;
    }

    public int getSmallIconColorType(Context context, Icon icon) {
        if (icon == null) {
            return SMALLICON_NONE;
        }
        switch (icon.getType()) {
            case SMALLICON_ASSORTED_COLOR /*1*/:
                return getSmallIconColorType(icon.getBitmap());
            case SMALLICON_SOLID_WHITE /*2*/:
                return getSmallIconColorType(context, icon.getResId());
            default:
                return SMALLICON_NONE;
        }
    }

    private int getSmallIconColorType(Bitmap bitmap) {
        if (bitmap == null) {
            return SMALLICON_NONE;
        }
        synchronized (sLock) {
            Pair<Integer, Integer> cached = (Pair) this.mHWGrayscaleBitmapCache.get(bitmap);
            if (cached == null || ((Integer) cached.second).intValue() != bitmap.getGenerationId()) {
                int generationId = bitmap.getGenerationId();
                Bitmap bmp = compressPicture(bitmap);
                int BitmapAvgColor = getBitmapAvgColor(bmp);
                if (Color.red(BitmapAvgColor) >= AVERAGE_BORDER_BETWEEN_SOLID_WHITE_AND_OTHER && Color.green(BitmapAvgColor) >= AVERAGE_BORDER_BETWEEN_SOLID_WHITE_AND_OTHER && Color.blue(BitmapAvgColor) >= AVERAGE_BORDER_BETWEEN_SOLID_WHITE_AND_OTHER) {
                    synchronized (sLock) {
                        this.mHWGrayscaleBitmapCache.put(bitmap, Pair.create(Integer.valueOf(SMALLICON_SOLID_WHITE), Integer.valueOf(generationId)));
                    }
                    return SMALLICON_SOLID_WHITE;
                } else if (getColorVariance(bmp, BitmapAvgColor) < 25.0f) {
                    synchronized (sLock) {
                        this.mHWGrayscaleBitmapCache.put(bitmap, Pair.create(Integer.valueOf(SMALLICON_SOLID_COLOR), Integer.valueOf(generationId)));
                    }
                    return SMALLICON_SOLID_COLOR;
                } else {
                    synchronized (sLock) {
                        this.mHWGrayscaleBitmapCache.put(bitmap, Pair.create(Integer.valueOf(SMALLICON_ASSORTED_COLOR), Integer.valueOf(generationId)));
                    }
                    return SMALLICON_ASSORTED_COLOR;
                }
            }
            int intValue = ((Integer) cached.first).intValue();
            return intValue;
        }
    }

    private int getSmallIconColorType(Context context, int drawableResId) {
        if (drawableResId <= 0) {
            return SMALLICON_NONE;
        }
        try {
            synchronized (sLock) {
                Integer cached = (Integer) this.mHWGrayscaleDrawableCache.get(Integer.valueOf(drawableResId));
                if (cached != null) {
                    int intValue = cached.intValue();
                    return intValue;
                }
                int result = getSmallIconColorType(context.getDrawable(drawableResId));
                synchronized (sLock) {
                    this.mHWGrayscaleDrawableCache.put(Integer.valueOf(drawableResId), Integer.valueOf(result));
                }
                return result;
            }
        } catch (NotFoundException e) {
            Log.e(TAG, "Drawable not found: " + drawableResId);
            return SMALLICON_NONE;
        }
    }

    private int getSmallIconColorType(Drawable d) {
        if (d == null) {
            return SMALLICON_NONE;
        }
        if (d instanceof BitmapDrawable) {
            BitmapDrawable bd = (BitmapDrawable) d;
            if (bd.getBitmap() == null) {
                return SMALLICON_NONE;
            }
            return getSmallIconColorType(bd.getBitmap());
        } else if (d instanceof VectorDrawable) {
            return getSmallIconColorType(drawableToBitamp(d));
        } else {
            if (d instanceof AnimationDrawable) {
                AnimationDrawable ad = (AnimationDrawable) d;
                if (ad.getNumberOfFrames() <= 0) {
                    return SMALLICON_NONE;
                }
                return getSmallIconColorType(ad.getFrame(SMALLICON_NONE));
            } else if (d instanceof LevelListDrawable) {
                return getSmallIconColorType(drawableToBitamp(d));
            } else {
                return SMALLICON_NONE;
            }
        }
    }

    private Bitmap compressPicture(Bitmap bmp) {
        int hr = bmp.getHeight();
        int wr = bmp.getWidth();
        if (wr <= 64 && hr <= 64) {
            return bmp;
        }
        Bitmap compactBitmap = Bitmap.createBitmap(64, 64, Config.ARGB_8888);
        Canvas compactBitmapCanvas = new Canvas(compactBitmap);
        Paint compactBitmapPaint = new Paint(SMALLICON_ASSORTED_COLOR);
        compactBitmapPaint.setFilterBitmap(true);
        Matrix matrix = new Matrix();
        matrix.reset();
        matrix.setScale(64.0f / ((float) wr), 64.0f / ((float) hr), 0.0f, 0.0f);
        compactBitmapCanvas.drawColor(SMALLICON_NONE, Mode.SRC);
        compactBitmapCanvas.drawBitmap(bmp, matrix, compactBitmapPaint);
        return compactBitmap;
    }

    private static Bitmap drawableToBitamp(Drawable drawable) {
        Config config;
        int w = drawable.getIntrinsicWidth();
        int h = drawable.getIntrinsicHeight();
        if (drawable.getOpacity() != -1) {
            config = Config.ARGB_8888;
        } else {
            config = Config.RGB_565;
        }
        Bitmap bitmap = Bitmap.createBitmap(w, h, config);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(SMALLICON_NONE, SMALLICON_NONE, w, h);
        drawable.draw(canvas);
        return bitmap;
    }

    private int getBitmapAvgColor(Bitmap bmp) {
        int hr = bmp.getHeight();
        int wr = bmp.getWidth();
        int[] mBufPixel = new int[(wr * hr)];
        bmp.getPixels(mBufPixel, SMALLICON_NONE, wr, SMALLICON_NONE, SMALLICON_NONE, wr, hr);
        int num = SMALLICON_NONE;
        int sumr = SMALLICON_NONE;
        int sumg = SMALLICON_NONE;
        int sumb = SMALLICON_NONE;
        for (int y = SMALLICON_NONE; y < hr; y += SMALLICON_ASSORTED_COLOR) {
            for (int x = SMALLICON_NONE; x < wr; x += SMALLICON_ASSORTED_COLOR) {
                int a = Color.alpha(mBufPixel[(y * wr) + x]);
                int r = Color.red(mBufPixel[(y * wr) + x]);
                int g = Color.green(mBufPixel[(y * wr) + x]);
                int b = Color.blue(mBufPixel[(y * wr) + x]);
                if (a > VALID_ALPHA_VALUE) {
                    sumr += r;
                    sumg += g;
                    sumb += b;
                    num += SMALLICON_ASSORTED_COLOR;
                }
            }
        }
        if (num <= VALID_PIXEL_NUM) {
            return -1;
        }
        return (((sumr / num) << 16) | ((sumg / num) << 8)) | (sumb / num);
    }

    private float getColorVariance(Bitmap bmp, int avg) {
        int hr = bmp.getHeight();
        int wr = bmp.getWidth();
        int[] mBufPixel = new int[(wr * hr)];
        bmp.getPixels(mBufPixel, SMALLICON_NONE, wr, SMALLICON_NONE, SMALLICON_NONE, wr, hr);
        int num = SMALLICON_NONE;
        int avgr = Color.red(avg);
        int avgg = Color.green(avg);
        int avgb = Color.blue(avg);
        float v = 0.0f;
        for (int y = SMALLICON_NONE; y < hr; y += SMALLICON_ASSORTED_COLOR) {
            for (int x = SMALLICON_NONE; x < wr; x += SMALLICON_ASSORTED_COLOR) {
                if (Color.alpha(mBufPixel[(y * wr) + x]) > VALID_ALPHA_VALUE) {
                    int r = Color.red(mBufPixel[(y * wr) + x]);
                    int g = Color.green(mBufPixel[(y * wr) + x]);
                    int b = Color.blue(mBufPixel[(y * wr) + x]);
                    v += (float) ((((r - avgr) * (r - avgr)) + ((g - avgg) * (g - avgg))) + ((b - avgb) * (b - avgb)));
                    num += SMALLICON_ASSORTED_COLOR;
                }
            }
        }
        if (num <= VALID_PIXEL_NUM) {
            return 0.0f;
        }
        return v / ((float) num);
    }
}
