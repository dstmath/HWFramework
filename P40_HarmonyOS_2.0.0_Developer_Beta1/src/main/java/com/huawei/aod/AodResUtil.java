package com.huawei.aod;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Slog;
import com.huawei.cust.HwCfgFilePolicy;
import huawei.com.android.server.policy.HwGlobalActionsData;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Locale;

public class AodResUtil {
    private static final String ART_COLOR_BITMAP_DIR = "color_bitmap";
    private static final float ART_PATTERN_SCALE = 0.75f;
    private static final float ART_PATTERN_WHOLE_SCALE = 1.0f;
    private static final String BITMAP_DIR = "bitmap";
    private static final String BITMAP_FILE_NAME = "personality_bitmap";
    private static final int DENSITY_COMMON = 3;
    private static final int DESIRED_DENSITY_FIXED = 160;
    private static final int DESIRED_DPI = (REAL_DENSITY * DESIRED_DENSITY_FIXED);
    private static final int DPI_CONST = 360;
    public static final int HALF_LENGTH_DIVIDER = 2;
    public static final float NEW_DENSITY;
    public static final int REAL_DENSITY;
    public static final int REAL_DPI = getLcdDensity();
    private static final String STRING_DUAL = "_dual";
    private static final String TAG = "AodResUtil";
    private static String sAodThemeFiles = "aodThemes";
    private static String sRealThemePath = null;
    private static String sVmallAodThemeFiles = "vmallAodThemes";

    static {
        int i = REAL_DPI;
        REAL_DENSITY = i / DESIRED_DENSITY_FIXED;
        NEW_DENSITY = ((float) i) / 160.0f;
    }

    private AodResUtil() {
    }

    public static boolean getDualAnalogClockSame(int[] ids) {
        if (ids != null && ids.length == 6 && ids[0] == ids[2] && ids[1] == ids[3]) {
            return true;
        }
        return false;
    }

    public static int getLcdDensity() {
        return SystemProperties.getInt("ro.sf.real_lcd_density", SystemProperties.getInt("ro.sf.lcd_density", 0));
    }

    public static int getDesiredWidth() {
        return 1080;
    }

    public static boolean isLowDensity() {
        return getLcdDensity() < 480;
    }

    public static Drawable getDrawableByPath(Context context, String resName, int resId, boolean isOnlineRes) {
        if (context == null || resName == null) {
            Slog.e(TAG, "decodeSingleAnalogClockImageResource(): context or resName is null!!!");
            return null;
        }
        Drawable drawable = getDrawableFromPath(getDrawablePath(resName, isOnlineRes), context);
        if (drawable != null || !isOnlineRes) {
            return drawable;
        }
        Slog.e(TAG, "decodeSingleAnalogClockImageResource(): drawable is null from getDrawableByPath()");
        return context.getResources().getDrawableForDensity(resId, DESIRED_DPI, null);
    }

    private static String getRealThemePath() {
        File file;
        if (sRealThemePath == null) {
            ArrayList<File> files = HwCfgFilePolicy.getCfgFileList(sAodThemeFiles, 0);
            if (files == null || files.size() <= 0 || (file = files.get(files.size() - 1)) == null || !file.exists()) {
                sRealThemePath = "";
            } else {
                sRealThemePath = file.getPath() + File.separator;
                return sRealThemePath;
            }
        }
        return sRealThemePath;
    }

    private static Drawable getDrawableFromPath(String[] paths, Context context) {
        Drawable drawable = null;
        InputStream inputStream = null;
        try {
            File file = new File(paths[0]);
            if (!file.exists()) {
                int count = paths.length;
                int i = 1;
                while (true) {
                    if (i >= count) {
                        break;
                    } else if (file.exists()) {
                        break;
                    } else {
                        i++;
                    }
                }
            }
            if (!file.exists()) {
                Slog.e(TAG, "getDrawableByPath(): file is not exist");
                if (0 != 0) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        Slog.e(TAG, "close inputstream error!!!!");
                    }
                }
                return null;
            }
            InputStream inputStream2 = new FileInputStream(file);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inScaled = false;
            drawable = Drawable.createFromResourceStream(context.getResources(), null, inputStream2, null, options);
            try {
                inputStream2.close();
            } catch (IOException e2) {
                Slog.e(TAG, "close inputstream error!!!!");
            }
            return drawable;
        } catch (IOException e3) {
            Slog.e(TAG, "getDrawableFromPath(): IO exception occured");
            if (0 != 0) {
                inputStream.close();
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    inputStream.close();
                } catch (IOException e4) {
                    Slog.e(TAG, "close inputstream error!!!!");
                }
            }
            throw th;
        }
    }

    private static String[] getDrawablePath(String fileName, boolean isOnlineRes) {
        int count = AodThemeConst.PNG_TYPES.length;
        String[] paths = new String[count];
        for (int i = 0; i < count; i++) {
            if (isOnlineRes) {
                paths[i] = AodThemeConst.USER_DOWNLOAD_DIR + fileName + AodThemeConst.PNG_TYPES[i];
            } else {
                paths[i] = getRealThemePath() + fileName + AodThemeConst.PNG_TYPES[i];
            }
        }
        return paths;
    }

    public static Drawable getUserCustomBg(Context context, int dstWidth, int dstHeight, String picName) {
        if (context == null || dstWidth <= 0 || dstHeight <= 0) {
            return null;
        }
        Bitmap bitmap = BitmapFactory.decodeFile(getDataFilePath(context, BITMAP_DIR, TextUtils.isEmpty(picName) ? BITMAP_FILE_NAME : picName));
        Bitmap result = Bitmap.createBitmap(dstWidth, dstHeight, Bitmap.Config.RGB_565);
        new Canvas(result).drawBitmap(bitmap, (float) getAlignWidth((dstWidth - bitmap.getWidth()) / 2), (float) getAlignWidth((dstHeight - bitmap.getHeight()) / 2), (Paint) null);
        return new BitmapDrawable(result);
    }

    public static Drawable getArtSignatureBg(Context context, int dstWidth, int dstHeight, String picName, boolean isTetonProduct) {
        if (context == null || dstWidth <= 0 || dstHeight <= 0) {
            return null;
        }
        Bitmap bitmap = BitmapFactory.decodeFile(getDataFilePath(context, BITMAP_DIR, picName));
        Bitmap result = Bitmap.createBitmap(dstWidth, dstHeight, Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(result);
        int offsetX = (dstWidth - bitmap.getWidth()) / 2;
        if (!isTetonProduct) {
            offsetX = getAlignWidth(offsetX);
        }
        canvas.drawBitmap(bitmap, (float) offsetX, (float) getAlignWidth((dstHeight - bitmap.getHeight()) / 2), (Paint) null);
        return new BitmapDrawable(result);
    }

    private static int getAlignWidth(int width) {
        return width % 8 == 0 ? width : ((width / 8) * 8) + 8;
    }

    private static String getDataFilePath(Context context, String subDir, String fileName) {
        File dir = new File(context.getDataDir().getPath(), subDir);
        if (dir.exists() || dir.mkdir()) {
            return new File(dir, fileName).getPath();
        }
        return "";
    }

    public static Drawable getArtCustomBg(Context context, int dstWidth, int dstHeight, String picName) {
        if (context == null || dstWidth <= 0 || dstHeight <= 0) {
            return null;
        }
        Slog.e(TAG, "getArtCustomBg getDataFilePath picName=" + picName);
        Bitmap bitmap = BitmapFactory.decodeFile(getDataFilePath(context, ART_COLOR_BITMAP_DIR, picName));
        bitmap.setDensity(getLcdDensity());
        Bitmap bitmap2 = getZoomImage(bitmap, (double) dstWidth, (double) dstHeight);
        Slog.e(TAG, "getArtCustomBg bitmap width=" + bitmap2.getWidth() + ", bitmap.height=" + bitmap2.getHeight() + ",bitmap=" + bitmap2 + ", dstWidth=" + dstWidth + ", dstHeight=" + dstHeight);
        Bitmap result = Bitmap.createBitmap(dstWidth, dstHeight, Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(result);
        canvas.setDensity(getLcdDensity());
        canvas.drawBitmap(bitmap2, (float) ((dstWidth - bitmap2.getWidth()) / 2), (float) ((dstHeight - bitmap2.getHeight()) / 2), (Paint) null);
        return new BitmapDrawable(result);
    }

    private static Bitmap getZoomImage(Bitmap orgBitmap, double dstWidth, double dstHeight) {
        if (orgBitmap == null) {
            return null;
        }
        Slog.e(TAG, "getZoomImage");
        float width = (float) orgBitmap.getWidth();
        float height = (float) orgBitmap.getHeight();
        Matrix matrix = new Matrix();
        float f = ((float) dstWidth) / width;
        float scaleHeight = ((float) dstHeight) / height;
        matrix.postScale(scaleHeight, scaleHeight);
        return Bitmap.createBitmap(orgBitmap, 0, 0, (int) width, (int) height, matrix, true);
    }

    public static boolean isRtl(Context context) {
        if (context == null) {
            return false;
        }
        boolean hasRtlSupport = (context.getApplicationInfo().flags & HwGlobalActionsData.FLAG_KEYCOMBINATION_INIT_STATE) == 4194304;
        boolean isRtl = TextUtils.getLayoutDirectionFromLocale(Locale.getDefault()) == 1;
        if (!hasRtlSupport || !isRtl) {
            return false;
        }
        return true;
    }

    private static String getCustThemePath(String fillName) {
        File file = HwCfgFilePolicy.getCfgFile(fillName, 0);
        if (file == null || !file.exists()) {
            return "";
        }
        return file.getPath();
    }

    private static String[] getVmallThemeDrawablePath(String fileName) {
        int count = AodThemeConst.PNG_TYPES.length;
        String[] paths = new String[count];
        String vmallThemeFilePath = getCustThemePath(sVmallAodThemeFiles);
        if (TextUtils.isEmpty(fileName) || TextUtils.isEmpty(vmallThemeFilePath)) {
            return paths;
        }
        for (int i = 0; i < count; i++) {
            paths[i] = vmallThemeFilePath + File.separator + fileName + AodThemeConst.PNG_TYPES[i];
        }
        return paths;
    }

    public static Drawable getVmallThemeDrawable(Context context, String resName) {
        if (context != null && resName != null) {
            return getDrawableFromPath(getVmallThemeDrawablePath(resName), context);
        }
        Slog.e(TAG, "getVmallThemeDrawable(): context or resName is null!!!");
        return null;
    }
}
