package android.widget.sr;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.os.SystemClock;
import android.util.Log;

public class SRBitmapTask {
    private static final boolean DB = true;
    private static final String TAG = "SRBitmapTask";
    private static final int WAIT_TIME_OUT = 400;
    private Bitmap mAshmemBitmap;
    private Bitmap mBitmap;
    private volatile boolean mCondition = false;
    private boolean mSRSuccess = false;

    public SRBitmapTask(Bitmap bitmap) {
        this.mBitmap = bitmap;
        this.mAshmemBitmap = bitmap.createAshmemBitmap(Config.ARGB_8888);
    }

    public synchronized void setAshmemBitmap(Bitmap src, Bitmap dst) {
        if (this.mAshmemBitmap.equals(src)) {
            this.mSRSuccess = true;
            this.mAshmemBitmap = dst;
            return;
        }
        Log.w(TAG, "setAshmemBitmap: src changed !!!! " + src + ", " + this.mBitmap);
    }

    public synchronized Bitmap getAshmemBitmap() {
        return this.mAshmemBitmap;
    }

    public synchronized void waitTask(SRBitmapManager manager) {
        boolean z = true;
        synchronized (this) {
            long startTime = SystemClock.elapsedRealtime();
            long startTimeForDebug = SystemClock.elapsedRealtime();
            long endMillis = startTime + 400;
            Log.i(TAG, "mCondition is " + this.mCondition);
            while (!this.mCondition && startTime < endMillis) {
                try {
                    Log.i(TAG, " start wait: " + this.mBitmap);
                    wait(endMillis - startTime);
                } catch (InterruptedException e) {
                    Log.e(TAG, "srBitmap: " + e.toString());
                }
                startTime = SystemClock.elapsedRealtime();
            }
            if (SystemClock.elapsedRealtime() >= endMillis) {
                manager.removeTaskFromQueue(this);
                Bitmap bitmap = this.mBitmap;
                if (manager.getSRStatus() != 3) {
                    z = false;
                }
                DebugUtil.debugTimeout(bitmap, z);
                Log.w(TAG, "time out : " + this.mBitmap);
            } else if (this.mSRSuccess) {
                Canvas canvas = new Canvas(this.mBitmap);
                Paint paint = new Paint();
                canvas.drawColor(0, Mode.CLEAR);
                canvas.drawBitmap(this.mAshmemBitmap, 0.0f, 0.0f, paint);
                DebugUtil.debugDone(this.mBitmap, 3.0f, startTimeForDebug, true);
                Log.i(TAG, " success: " + this.mBitmap);
            } else {
                DebugUtil.debugDone(this.mBitmap, 5.0f, startTimeForDebug, false);
                Log.i(TAG, "quit : " + this.mBitmap);
            }
        }
    }

    public synchronized void notifyTask() {
        this.mCondition = true;
        notifyAll();
    }
}
