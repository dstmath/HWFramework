package huawei.android.hwpicaveragenoises;

import android.app.ActivityThread;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.FreezeScreenScene;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.IWindowManager;
import android.view.WindowManager;
import java.math.BigDecimal;

public class HwPicAverageNoises {
    private static final int KEEP_DECIMAL_PLACES = 2;
    private static final Object LOCK = new Object();
    private static final double PAD_SCREEN_SIZE_MIN = 6.5d;
    private static final int PYTHAGOREAN_THEOREM_CALCULATION_FACTOR = 2;
    private static final String TAG = "HwPicAverageNoises";
    private static double sDeviceSize;
    private static IWindowManager sIWindowManager = IWindowManager.Stub.asInterface(ServiceManager.checkService(FreezeScreenScene.WINDOW_PARAM));
    private static boolean sIsJniAverageLibExist;
    private static WindowManager sWindowManager = ((WindowManager) ActivityThread.currentApplication().getApplicationContext().getSystemService(FreezeScreenScene.WINDOW_PARAM));

    public native Bitmap jniFromNoiseBitmap(Bitmap bitmap);

    static {
        sIsJniAverageLibExist = true;
        try {
            System.loadLibrary("jniaverage");
        } catch (UnsatisfiedLinkError e) {
            sIsJniAverageLibExist = false;
            Log.e(TAG, "libjniaverage.so couldn't be found.");
        }
    }

    public Bitmap jniNoiseBitmap(Bitmap bitmap) {
        if (!isAverageNoiseSupported()) {
            return bitmap;
        }
        try {
            return jniFromNoiseBitmap(bitmap);
        } catch (UnsatisfiedLinkError e) {
            return bitmap;
        }
    }

    public static boolean isAverageNoiseSupported() {
        if (!sIsJniAverageLibExist) {
            return false;
        }
        if (sDeviceSize > 0.0d) {
            Log.d(TAG, "sDeviceSize = " + sDeviceSize);
            if (sDeviceSize > PAD_SCREEN_SIZE_MIN) {
                return true;
            }
            return false;
        }
        DisplayMetrics displayMetrics = new DisplayMetrics();
        sWindowManager.getDefaultDisplay().getRealMetrics(displayMetrics);
        Log.i(TAG, "getRealMetrics: displayMetrics.x = " + displayMetrics.xdpi + ", displayMetrics.y = " + displayMetrics.ydpi);
        if (sIWindowManager != null) {
            Point point = new Point();
            try {
                sIWindowManager.getInitialDisplaySize(0, point);
                sDeviceSize = new BigDecimal(Math.sqrt(Math.pow((double) (((float) point.x) / displayMetrics.xdpi), 2.0d) + Math.pow((double) (((float) point.y) / displayMetrics.ydpi), 2.0d))).setScale(2, 4).doubleValue();
                if (sDeviceSize > PAD_SCREEN_SIZE_MIN) {
                    return true;
                }
                return false;
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException while calculate device size");
            }
        }
        Log.i(TAG, "displayMetrics.widthPixels = " + displayMetrics.widthPixels + ", displayMetrics.heightPixels = " + displayMetrics.heightPixels);
        sDeviceSize = new BigDecimal(Math.sqrt(Math.pow((double) (((float) displayMetrics.widthPixels) / displayMetrics.xdpi), 2.0d) + Math.pow((double) (((float) displayMetrics.heightPixels) / displayMetrics.ydpi), 2.0d))).setScale(2, 4).doubleValue();
        if (sDeviceSize > PAD_SCREEN_SIZE_MIN) {
            return true;
        }
        return false;
    }

    public Bitmap addNoiseWithBlackBoard(Bitmap bitmap, int color) {
        Bitmap scaleBitmap = scaleBitmapToScreenSize(bitmap);
        Bitmap tmpBitmap = jniNoiseBitmap(scaleBitmap);
        if (tmpBitmap == null) {
            Log.e(TAG, "addNoiseWithBlackBoard tmpBitmap is null!");
            return null;
        }
        Bitmap newBitmap = addBlackBoard(tmpBitmap, color);
        if (scaleBitmap != null) {
            scaleBitmap.recycle();
        }
        return newBitmap;
    }

    private static Bitmap addBlackBoard(Bitmap bitmap, int color) {
        Bitmap newBitmap;
        synchronized (LOCK) {
            Canvas canvas = new Canvas();
            Paint paint = new Paint();
            newBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
            canvas.setBitmap(newBitmap);
            canvas.drawBitmap(bitmap, 0.0f, 0.0f, paint);
            canvas.drawColor(color);
        }
        return newBitmap;
    }

    private static Bitmap scaleBitmapToScreenSize(Bitmap bitmap) {
        Context context = ActivityThread.currentApplication().getApplicationContext();
        if (context == null || bitmap == null) {
            return null;
        }
        int screenWidth = context.getResources().getDisplayMetrics().widthPixels;
        int screenHeight = context.getResources().getDisplayMetrics().heightPixels;
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Matrix matrix = new Matrix();
        int scaleHeight = screenHeight / height;
        int scaleWidth = screenWidth / width;
        float scale = scaleHeight > scaleWidth ? (float) scaleHeight : (float) scaleWidth;
        matrix.postScale(scale, scale);
        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
        Log.i(TAG, "width:" + width + ",height:" + height + ",screenWidth:" + screenWidth + ",screenHeight:" + screenHeight + ",scale:" + scale);
        return resizedBitmap;
    }
}
