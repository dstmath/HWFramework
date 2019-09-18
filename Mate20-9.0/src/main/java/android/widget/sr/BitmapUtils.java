package android.widget.sr;

import android.graphics.Bitmap;
import android.util.Log;
import java.lang.reflect.InvocationTargetException;

public class BitmapUtils {
    private static final long ILLEGAL_PTR = 0;
    private static final int MAX_LEN0 = 960;
    private static final int MAX_LEN1 = 1280;
    private static final int MIN_SIZE = 150;
    public static final String TAG = "BitmapUtils";

    public static Bitmap createAshBitmap(Bitmap bitmap) {
        if (bitmap == null) {
            return null;
        }
        try {
            return (Bitmap) bitmap.getClass().getMethod("createAshmemBitmap", new Class[0]).invoke(bitmap, new Object[0]);
        } catch (NoSuchMethodException e) {
            Log.w(TAG, "BitmapUtils.createAshBitmap NoSuchMethodException");
            return null;
        } catch (IllegalArgumentException e2) {
            Log.w(TAG, "BitmapUtils.createAshBitmap IllegalArgumentException");
            return null;
        } catch (IllegalAccessException e3) {
            Log.w(TAG, "BitmapUtils.createAshBitmap IllegalAccessException");
            return null;
        } catch (InvocationTargetException e4) {
            Log.w(TAG, "BitmapUtils.createAshBitmap InvocationTargetException");
            return null;
        }
    }

    public static long getAshBitmapPtr(Bitmap bitmap) {
        try {
            return ((Long) bitmap.getClass().getMethod("getNativeInstance", new Class[0]).invoke(bitmap, new Object[0])).longValue();
        } catch (NoSuchMethodException e) {
            Log.w(TAG, "BitmapUtils.createAshBitmap NoSuchMethodException");
            return 0;
        } catch (IllegalArgumentException e2) {
            Log.w(TAG, "BitmapUtils.createAshBitmap IllegalArgumentException");
            return 0;
        } catch (IllegalAccessException e3) {
            Log.w(TAG, "BitmapUtils.createAshBitmap IllegalAccessException");
            return 0;
        } catch (InvocationTargetException e4) {
            Log.w(TAG, "BitmapUtils.createAshBitmap InvocationTargetException");
            return 0;
        }
    }

    public static int isBitmapIllegalSize(Bitmap bitmap) {
        if (bitmap == null) {
            return -1;
        }
        int minLen = bitmap.getWidth();
        int maxLen = bitmap.getHeight();
        if (minLen > maxLen) {
            int tmp = minLen;
            minLen = maxLen;
            maxLen = tmp;
        }
        if (minLen < 150 || minLen > MAX_LEN0 || maxLen > MAX_LEN1) {
            return -1;
        }
        return 0;
    }
}
