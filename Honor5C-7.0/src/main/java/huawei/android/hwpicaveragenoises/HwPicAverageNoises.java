package huawei.android.hwpicaveragenoises;

import android.app.ActivityThread;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.RemoteException;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.IWindowManager;
import android.view.WindowManager;
import java.math.BigDecimal;

public class HwPicAverageNoises {
    private static final double PAD_SCREEN_SIZE_MIN = 6.5d;
    private static final String TAG = "HwPicAverageNoises";
    private static boolean isJniAverageLibExist;
    private static double mDeviceSize;
    private static IWindowManager mIWindowManager;
    private static WindowManager mWindowManager;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: huawei.android.hwpicaveragenoises.HwPicAverageNoises.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: huawei.android.hwpicaveragenoises.HwPicAverageNoises.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: huawei.android.hwpicaveragenoises.HwPicAverageNoises.<clinit>():void");
    }

    public native Bitmap jniFromNoiseBitmap(Bitmap bitmap);

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
        float scale = (float) Math.max(screenH / height, screenW / width);
        matrix.postScale(scale, scale);
        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
        Log.i(TAG, "width:" + width + ",height:" + height + ",screenW:" + screenW + ",screenH:" + screenH + ",scale:" + scale);
        return resizedBitmap;
    }
}
