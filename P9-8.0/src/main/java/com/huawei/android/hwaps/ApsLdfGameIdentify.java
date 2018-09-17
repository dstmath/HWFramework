package com.huawei.android.hwaps;

import android.os.IBinder;
import android.os.Parcel;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.util.Log;

public class ApsLdfGameIdentify {
    public static final int ACTION_DOWN = 0;
    public static final int ACTION_MOVE = 2;
    public static final int ACTION_UP = 1;
    private static final double CLICK_DISTANCE_THRESHOLDS = 10.8d;
    private static final int CLICK_TIME_THRESHOLDS = 300;
    private static final int DIFF_FRAME_PER_MINUTE = 120;
    private static final int DIFF_FRAME_PER_TOUCH_CYCLE = 10;
    private static final int GET_DIFF_FRAME_COUNT = 5;
    private static final double MIN_CLICK_DISTANCE_RATE = 0.8d;
    private static final double MIN_CLICK_TIME_RATE = 0.8d;
    private static final int SECOND_PER_MINUTE = 60;
    private static final int START_STATISTIC_DIFF_FRAME = 4;
    private static final int STATISTIC_DIFFERENT_FRAME = 1196;
    private static final int STOP_IDENTIFY_THRESHOLDS = 3;
    private static final int STOP_STATISTIC_DIFF_FRAME = 6;
    private static final String TAG = "ApsLdfGameIdentify";
    private static ApsLdfGameIdentify sInstance = null;
    private int mBinderFlag = 6;
    private int mDiffFrameCount = 0;
    private IBinder mFlinger = null;
    private boolean mIsLdfGame = false;
    private long mLastDownTime = 0;
    private int mLastDownX = 0;
    private int mLastDownY = 0;
    private long mLastTime = 0;
    private int mNotClickByDistance = 0;
    private int mNotClickByTime = 0;
    private int mNotLdfGameCount = 0;
    private int mScreenWidth = 0;
    private int mSuccessStatisticCount = 0;
    private int mTotalDiffFrameCount = 0;
    private int mTouchCount = 0;

    public static synchronized ApsLdfGameIdentify getInstance() {
        ApsLdfGameIdentify apsLdfGameIdentify;
        synchronized (ApsLdfGameIdentify.class) {
            if (sInstance == null) {
                sInstance = new ApsLdfGameIdentify();
            }
            apsLdfGameIdentify = sInstance;
        }
        return apsLdfGameIdentify;
    }

    public static boolean isSupportApsLdfGameIdentify() {
        if (64 == (SystemProperties.getInt("sys.aps.support", 0) & 64)) {
            return true;
        }
        Log.w(TAG, "APS: Ldf game identify is not supported");
        return false;
    }

    private ApsLdfGameIdentify() {
        resetIdentifyData();
    }

    public void resetApsLdfGameIdentify() {
        resetIdentifyData();
        resetTouchData();
    }

    private void resetIdentifyData() {
        sendCmdToSurfaceflinger(6);
        this.mDiffFrameCount = 0;
        this.mSuccessStatisticCount = 0;
        this.mTotalDiffFrameCount = 0;
        this.mLastTime = 0;
        this.mNotLdfGameCount = 0;
        this.mIsLdfGame = false;
        this.mFlinger = ServiceManager.getService("SurfaceFlinger");
    }

    private void resetTouchData() {
        this.mTouchCount = 0;
        this.mLastDownX = 0;
        this.mLastDownY = 0;
        this.mLastDownTime = 0;
        this.mNotClickByDistance = 0;
        this.mNotClickByTime = 0;
    }

    public void setParaForLdfGameIdentify(int width) {
        this.mScreenWidth = width;
    }

    public boolean isLdfGame() {
        if (this.mNotLdfGameCount >= 3) {
            return false;
        }
        if (this.mSuccessStatisticCount != 0 && 0 != this.mLastTime && isDifferentFrameSatisfied() && isTouchDistanceSatisfied() && isTouchTimeSatisfied()) {
            this.mIsLdfGame = true;
            this.mNotLdfGameCount = 0;
        } else {
            this.mIsLdfGame = false;
            this.mNotLdfGameCount++;
        }
        this.mTotalDiffFrameCount = 0;
        this.mSuccessStatisticCount = 0;
        this.mLastTime = SystemClock.uptimeMillis();
        resetTouchData();
        ApsCommon.logD(TAG, "APS:this is isLdfGame judge by touch regin:  " + this.mIsLdfGame);
        return this.mIsLdfGame;
    }

    private boolean isDifferentFrameSatisfied() {
        long maxDiffFrame = ((long) ((((double) (SystemClock.uptimeMillis() - this.mLastTime)) / 1000.0d) / 60.0d)) * 120;
        int mAverageDiffFrame = this.mTotalDiffFrameCount / this.mSuccessStatisticCount;
        ApsCommon.logD("wiinner", "@@@@ APS: mAverageNumOfNotSameFrame = " + mAverageDiffFrame + "; mTotalDiffFrameCount" + this.mTotalDiffFrameCount + "; mSuccessStatisticCount = " + this.mSuccessStatisticCount + "; maxDiffFrame = " + maxDiffFrame);
        if (((long) this.mTotalDiffFrameCount) > maxDiffFrame || mAverageDiffFrame > 10) {
            return false;
        }
        return true;
    }

    private boolean isTouchDistanceSatisfied() {
        ApsCommon.logD("winner", "@@@@ isTouchDistanceStisfied mNotClickByDistance=" + this.mNotClickByDistance + "  mTouchCount=" + this.mTouchCount);
        return ((double) this.mNotClickByDistance) <= ((double) this.mTouchCount) * 0.19999999999999996d;
    }

    private boolean isTouchTimeSatisfied() {
        ApsCommon.logD("winner", "@@@@ isTouchTimeSatisfied mNotClickByDistance=" + this.mNotClickByTime + "  mTouchCount=" + this.mTouchCount);
        return ((double) this.mNotClickByTime) <= ((double) this.mTouchCount) * 0.19999999999999996d;
    }

    public void collectInputEvent(int action, int x, int y, long eventTime, long downTime) {
        if (this.mNotLdfGameCount < 3) {
            statisticInputEvent(action, x, y, eventTime, downTime);
            communicateWithSurfaceflinger(action);
            if (-1 != this.mDiffFrameCount) {
                this.mTotalDiffFrameCount += this.mDiffFrameCount;
                this.mSuccessStatisticCount++;
                this.mDiffFrameCount = -1;
            }
            if (0 == this.mLastTime) {
                this.mLastTime = SystemClock.uptimeMillis();
            }
        }
    }

    private void statisticInputEvent(int action, int x, int y, long eventTime, long downTime) {
        switch (action) {
            case 0:
                this.mTouchCount++;
                this.mLastDownX = x;
                this.mLastDownY = y;
                this.mLastDownTime = downTime;
                return;
            case 1:
                if (Math.abs(x - this.mLastDownX) > ((int) (((double) this.mScreenWidth) / CLICK_DISTANCE_THRESHOLDS)) || Math.abs(y - this.mLastDownY) > ((int) (((double) this.mScreenWidth) / CLICK_DISTANCE_THRESHOLDS))) {
                    this.mNotClickByDistance++;
                }
                if (eventTime - this.mLastDownTime >= 300) {
                    this.mNotClickByTime++;
                    return;
                }
                return;
            default:
                return;
        }
    }

    private void communicateWithSurfaceflinger(int action) {
        switch (action) {
            case 0:
                if (this.mBinderFlag == 4) {
                    sendCmdToSurfaceflinger(5);
                }
                sendCmdToSurfaceflinger(6);
                return;
            case 1:
                sendCmdToSurfaceflinger(4);
                return;
            default:
                return;
        }
    }

    /* JADX WARNING: Failed to extract finally block: empty outs */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void sendCmdToSurfaceflinger(int cmd) {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInt(cmd);
            data.writeInterfaceToken("android.ui.ISurfaceComposer");
            this.mFlinger.transact(STATISTIC_DIFFERENT_FRAME, data, reply, 0);
            if (5 == cmd) {
                this.mDiffFrameCount = reply.readInt();
            }
            this.mBinderFlag = cmd;
            if (data != null) {
                data.recycle();
            }
            if (reply != null) {
                reply.recycle();
            }
        } catch (Exception e) {
            Log.e(TAG, "binder error");
            if (data != null) {
                data.recycle();
            }
            if (reply != null) {
                reply.recycle();
            }
        } catch (Throwable th) {
            if (data != null) {
                data.recycle();
            }
            if (reply != null) {
                reply.recycle();
            }
            throw th;
        }
    }
}
