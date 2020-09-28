package huawei.android.hwutil;

import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import com.huawei.android.util.DisplayMetricsEx;
import java.io.IOException;
import java.io.InputStream;

public class AssetsFileCache {
    public static Drawable getDrawableEntry(AssetManager assets, Resources res, TypedValue value, String fileName, BitmapFactory.Options opts) {
        if (assets == null) {
            Log.w("AssetsFileCache ", "getDrawableEntry fileName : " + fileName + "  fail , assets null");
            return null;
        }
        Drawable dr = null;
        InputStream inputStream = null;
        try {
            inputStream = assets.open(fileName);
            dr = Drawable.createFromResourceStream(res, value, inputStream, fileName, opts);
        } catch (IOException e) {
        } catch (Throwable th) {
            closeInputStream(inputStream);
            throw th;
        }
        closeInputStream(inputStream);
        return dr;
    }

    public static InputStream getInputStreamEntry(AssetManager assets, String fileName) {
        if (assets == null) {
            Log.w("AssetsFileCache ", "getDrawableEntry fileName : " + fileName + "  fail , mAssets null");
            return null;
        }
        try {
            return assets.open(fileName);
        } catch (IOException e) {
            return null;
        }
    }

    public static Bitmap getBitmapEntry(AssetManager assets, Resources res, TypedValue value, String fileName, Rect padding) {
        if (assets == null) {
            Log.w("AssetsFileCache ", "getBitmapEntry fileName:" + fileName + " fail , assets null");
            return null;
        }
        InputStream inputStream = null;
        if (padding == null) {
            padding = new Rect();
        }
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inScreenDensity = res != null ? DisplayMetricsEx.getNoncompatDensityDpi(res) : DisplayMetrics.DENSITY_DEVICE_STABLE;
        try {
            inputStream = assets.open(fileName);
            Bitmap bmp = BitmapFactory.decodeResourceStream(res, value, inputStream, padding, opts);
            if (bmp != null) {
                bmp.setDensity(res != null ? res.getDisplayMetrics().densityDpi : DisplayMetrics.DENSITY_DEVICE_STABLE);
            }
            return bmp;
        } catch (Exception e) {
            return null;
        } finally {
            closeInputStream(inputStream);
        }
    }

    private static void closeInputStream(InputStream is) {
        if (is != null) {
            try {
                is.close();
            } catch (IOException e) {
                Log.e("AssetsFileCache ", "closeInputStream IO Error");
            }
        }
    }
}
