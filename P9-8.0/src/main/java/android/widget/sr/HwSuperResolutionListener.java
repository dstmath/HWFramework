package android.widget.sr;

import android.graphics.Bitmap;
import android.hwcontrol.HwWidgetFactory;
import android.util.Log;
import java.util.HashMap;

public abstract class HwSuperResolutionListener {
    public static final int FRAMEWORK_REPORT_PROCESS_ID = 0;
    private static final int REPORT_PROCESS_COUNT_LIMIT = 20;
    public static final int REPORT_PROCESS_ID = 907400021;
    private static final int RET_ERROR = -1;
    private static final int RET_OK = 0;
    public static final String TAG = "HwSuperResolutionListener";
    private int mCurrentCount = 0;
    private HashMap<Integer, NativeBitmap> mDesAshBmps = new HashMap();
    private HashMap<Integer, NativeBitmap> mSrcAshBmps = new HashMap();

    public abstract void onError(int i);

    public abstract void onProcessDone(Bitmap bitmap, Bitmap bitmap2);

    public abstract void onServiceDied();

    public abstract void onStartDone();

    public abstract void onStopDone();

    public abstract void onTimeOut(Bitmap bitmap);

    public int getCurrentCount() {
        return this.mCurrentCount;
    }

    public void resetCurrentCount() {
        this.mCurrentCount = 0;
    }

    public int onProcessDoneInt(int fd) {
        if (20 == this.mCurrentCount) {
            HwWidgetFactory.reportSrBigData(REPORT_PROCESS_ID, 0, Integer.valueOf(20));
            this.mCurrentCount = 0;
        }
        this.mCurrentCount++;
        HwSuperResolution.nativeSetReadOnly(fd);
        if (this.mSrcAshBmps.containsKey(Integer.valueOf(fd)) && this.mDesAshBmps.containsKey(Integer.valueOf(fd))) {
            onProcessDone(((NativeBitmap) this.mSrcAshBmps.get(Integer.valueOf(fd))).getBitmap(), ((NativeBitmap) this.mDesAshBmps.get(Integer.valueOf(fd))).getBitmap());
            this.mSrcAshBmps.remove(Integer.valueOf(fd));
            this.mDesAshBmps.remove(Integer.valueOf(fd));
            return 0;
        }
        Log.e(TAG, "onProcessDoneInt failed");
        return -1;
    }

    public void onTimeOutInt(int fd) {
        if (this.mSrcAshBmps != null && this.mSrcAshBmps.containsKey(Integer.valueOf(fd))) {
            onTimeOut(((NativeBitmap) this.mSrcAshBmps.get(Integer.valueOf(fd))).getBitmap());
        }
    }

    public void addSrcFdBitmap(int fd, NativeBitmap snb) {
        this.mSrcAshBmps.put(Integer.valueOf(fd), snb);
    }

    public void addDesFdBitmap(int fd, NativeBitmap dnb) {
        this.mDesAshBmps.put(Integer.valueOf(fd), dnb);
    }
}
