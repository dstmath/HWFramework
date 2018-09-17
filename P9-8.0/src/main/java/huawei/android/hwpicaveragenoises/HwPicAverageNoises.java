package huawei.android.hwpicaveragenoises;

import android.app.ActivityThread;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
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
import android.view.IWindowManager.Stub;
import android.view.WindowManager;
import java.math.BigDecimal;

public class HwPicAverageNoises {
    private static final double PAD_SCREEN_SIZE_MIN = 6.5d;
    private static final String TAG = "HwPicAverageNoises";
    private static boolean isJniAverageLibExist;
    private static double mDeviceSize;
    private static IWindowManager mIWindowManager = Stub.asInterface(ServiceManager.checkService(FreezeScreenScene.WINDOW_PARAM));
    private static WindowManager mWindowManager = ((WindowManager) ActivityThread.currentApplication().getApplicationContext().getSystemService(FreezeScreenScene.WINDOW_PARAM));

    public native Bitmap jniFromNoiseBitmap(Bitmap bitmap);

    static {
        isJniAverageLibExist = true;
        try {
            System.loadLibrary("jniaverage");
        } catch (UnsatisfiedLinkError e) {
            isJniAverageLibExist = false;
            Log.e(TAG, "libjniaverage.so couldn't be found.");
        }
    }

    public Bitmap jniNoiseBitmap(Bitmap bitmapOld) {
        if (!isAverageNoiseSupported()) {
            return bitmapOld;
        }
        try {
            return jniFromNoiseBitmap(bitmapOld);
        } catch (UnsatisfiedLinkError e) {
            return bitmapOld;
        }
    }

    public static boolean isAverageNoiseSupported() {
        if (!isJniAverageLibExist) {
            return false;
        }
        if (mDeviceSize > 0.0d) {
            Log.d(TAG, "mDeviceSize = " + mDeviceSize);
            return mDeviceSize > PAD_SCREEN_SIZE_MIN;
        }
        DisplayMetrics dm = new DisplayMetrics();
        mWindowManager.getDefaultDisplay().getRealMetrics(dm);
        Log.i(TAG, "getRealMetrics: dm.x = " + dm.xdpi + ", dm.y = " + dm.ydpi);
        if (mIWindowManager != null) {
            Point point = new Point();
            try {
                mIWindowManager.getInitialDisplaySize(0, point);
                mDeviceSize = new BigDecimal(Math.sqrt(Math.pow((double) (((float) point.x) / dm.xdpi), 2.0d) + Math.pow((double) (((float) point.y) / dm.ydpi), 2.0d))).setScale(2, 4).doubleValue();
                return mDeviceSize > PAD_SCREEN_SIZE_MIN;
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException while calculate device size", e);
            }
        }
        Log.i(TAG, "dm.widthPixels = " + dm.widthPixels + ", dm.heightPixels = " + dm.heightPixels);
        mDeviceSize = new BigDecimal(Math.sqrt(Math.pow((double) (((float) dm.widthPixels) / dm.xdpi), 2.0d) + Math.pow((double) (((float) dm.heightPixels) / dm.ydpi), 2.0d))).setScale(2, 4).doubleValue();
        return mDeviceSize > PAD_SCREEN_SIZE_MIN;
    }

    public Bitmap addNoiseWithBlackBoard(Bitmap bitmap, int color) {
        Bitmap scaleBitmap = scaleBitmapToScreenSize(bitmap);
        Bitmap tmp = addBlackBoard(jniNoiseBitmap(scaleBitmap), color);
        if (scaleBitmap != null) {
            scaleBitmap.recycle();
        }
        return tmp;
    }

    private static Bitmap addBlackBoard(Bitmap bmp, int color) {
        Bitmap newBitmap;
        Canvas canvas = new Canvas();
        Paint paint = new Paint();
        synchronized (canvas) {
            newBitmap = Bitmap.createBitmap(bmp.getWidth(), bmp.getHeight(), Config.ARGB_8888);
            canvas.setBitmap(newBitmap);
            canvas.drawBitmap(bmp, 0.0f, 0.0f, paint);
            canvas.drawColor(color);
        }
        return newBitmap;
    }

    private static Bitmap scaleBitmapToScreenSize(Bitmap bitmap) {
        Context context = ActivityThread.currentApplication().getApplicationContext();
        if (context == null || bitmap == null) {
            return null;
        }
        int screenW = context.getResources().getDisplayMetrics().widthPixels;
        int screenH = context.getResources().getDisplayMetrics().heightPixels;
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Matrix matrix = new Matrix();
        int scaleH = screenH / height;
        int scaleW = screenW / width;
        if (scaleH <= scaleW) {
            scaleH = scaleW;
        }
        float scale = (float) scaleH;
        matrix.postScale(scale, scale);
        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
        Log.i(TAG, "width:" + width + ",height:" + height + ",screenW:" + screenW + ",screenH:" + screenH + ",scale:" + scale);
        return resizedBitmap;
    }
}
