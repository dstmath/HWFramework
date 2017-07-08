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
    private static ApsLdfGameIdentify sInstance;
    private int mBinderFlag;
    private int mDiffFrameCount;
    private IBinder mFlinger;
    private boolean mIsLdfGame;
    private long mLastDownTime;
    private int mLastDownX;
    private int mLastDownY;
    private long mLastTime;
    private int mNotClickByDistance;
    private int mNotClickByTime;
    private int mNotLdfGameCount;
    private int mScreenWidth;
    private int mSuccessStatisticCount;
    private int mTotalDiffFrameCount;
    private int mTouchCount;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.android.hwaps.ApsLdfGameIdentify.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.android.hwaps.ApsLdfGameIdentify.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.android.hwaps.ApsLdfGameIdentify.<clinit>():void");
    }

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
        if (64 == (SystemProperties.getInt("sys.aps.support", ACTION_DOWN) & 64)) {
            return true;
        }
        Log.w(TAG, "APS: Ldf game identify is not supported");
        return false;
    }

    private ApsLdfGameIdentify() {
        this.mDiffFrameCount = ACTION_DOWN;
        this.mTotalDiffFrameCount = ACTION_DOWN;
        this.mSuccessStatisticCount = ACTION_DOWN;
        this.mNotLdfGameCount = ACTION_DOWN;
        this.mNotClickByDistance = ACTION_DOWN;
        this.mNotClickByTime = ACTION_DOWN;
        this.mFlinger = null;
        this.mBinderFlag = STOP_STATISTIC_DIFF_FRAME;
        this.mTouchCount = ACTION_DOWN;
        this.mLastDownX = ACTION_DOWN;
        this.mLastDownY = ACTION_DOWN;
        this.mScreenWidth = ACTION_DOWN;
        this.mLastDownTime = 0;
        this.mLastTime = 0;
        this.mIsLdfGame = false;
        resetIdentifyData();
    }

    public void resetApsLdfGameIdentify() {
        resetIdentifyData();
        resetTouchData();
    }

    private void resetIdentifyData() {
        sendCmdToSurfaceflinger(STOP_STATISTIC_DIFF_FRAME);
        this.mDiffFrameCount = ACTION_DOWN;
        this.mSuccessStatisticCount = ACTION_DOWN;
        this.mTotalDiffFrameCount = ACTION_DOWN;
        this.mLastTime = 0;
        this.mNotLdfGameCount = ACTION_DOWN;
        this.mIsLdfGame = false;
        this.mFlinger = ServiceManager.getService("SurfaceFlinger");
    }

    private void resetTouchData() {
        this.mTouchCount = ACTION_DOWN;
        this.mLastDownX = ACTION_DOWN;
        this.mLastDownY = ACTION_DOWN;
        this.mLastDownTime = 0;
        this.mNotClickByDistance = ACTION_DOWN;
        this.mNotClickByTime = ACTION_DOWN;
    }

    public void setParaForLdfGameIdentify(int width) {
        this.mScreenWidth = width;
    }

    public boolean isLdfGame() {
        if (this.mNotLdfGameCount >= STOP_IDENTIFY_THRESHOLDS) {
            return false;
        }
        if (this.mSuccessStatisticCount != 0 && 0 != this.mLastTime && isDifferentFrameSatisfied() && isTouchDistanceSatisfied() && isTouchTimeSatisfied()) {
            this.mIsLdfGame = true;
            this.mNotLdfGameCount = ACTION_DOWN;
        } else {
            this.mIsLdfGame = false;
            this.mNotLdfGameCount += ACTION_UP;
        }
        this.mTotalDiffFrameCount = ACTION_DOWN;
        this.mSuccessStatisticCount = ACTION_DOWN;
        this.mLastTime = SystemClock.uptimeMillis();
        resetTouchData();
        ApsCommon.logD(TAG, "APS:this is isLdfGame judge by touch regin:  " + this.mIsLdfGame);
        return this.mIsLdfGame;
    }

    private boolean isDifferentFrameSatisfied() {
        long maxDiffFrame = ((long) ((((double) (SystemClock.uptimeMillis() - this.mLastTime)) / 1000.0d) / 60.0d)) * 120;
        int mAverageDiffFrame = this.mTotalDiffFrameCount / this.mSuccessStatisticCount;
        ApsCommon.logD("wiinner", "@@@@ APS: mAverageNumOfNotSameFrame = " + mAverageDiffFrame + "; mTotalDiffFrameCount" + this.mTotalDiffFrameCount + "; mSuccessStatisticCount = " + this.mSuccessStatisticCount + "; maxDiffFrame = " + maxDiffFrame);
        if (((long) this.mTotalDiffFrameCount) > maxDiffFrame || mAverageDiffFrame > DIFF_FRAME_PER_TOUCH_CYCLE) {
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
        if (this.mNotLdfGameCount < STOP_IDENTIFY_THRESHOLDS) {
            statisticInputEvent(action, x, y, eventTime, downTime);
            communicateWithSurfaceflinger(action);
            if (-1 != this.mDiffFrameCount) {
                this.mTotalDiffFrameCount += this.mDiffFrameCount;
                this.mSuccessStatisticCount += ACTION_UP;
                this.mDiffFrameCount = -1;
            }
            if (0 == this.mLastTime) {
                this.mLastTime = SystemClock.uptimeMillis();
            }
        }
    }

    private void statisticInputEvent(int action, int x, int y, long eventTime, long downTime) {
        switch (action) {
            case ACTION_DOWN /*0*/:
                this.mTouchCount += ACTION_UP;
                this.mLastDownX = x;
                this.mLastDownY = y;
                this.mLastDownTime = downTime;
            case ACTION_UP /*1*/:
                if (Math.abs(x - this.mLastDownX) > ((int) (((double) this.mScreenWidth) / CLICK_DISTANCE_THRESHOLDS)) || Math.abs(y - this.mLastDownY) > ((int) (((double) this.mScreenWidth) / CLICK_DISTANCE_THRESHOLDS))) {
                    this.mNotClickByDistance += ACTION_UP;
                }
                if (eventTime - this.mLastDownTime >= 300) {
                    this.mNotClickByTime += ACTION_UP;
                }
            default:
        }
    }

    private void communicateWithSurfaceflinger(int action) {
        switch (action) {
            case ACTION_DOWN /*0*/:
                if (this.mBinderFlag == START_STATISTIC_DIFF_FRAME) {
                    sendCmdToSurfaceflinger(GET_DIFF_FRAME_COUNT);
                }
                sendCmdToSurfaceflinger(STOP_STATISTIC_DIFF_FRAME);
            case ACTION_UP /*1*/:
                sendCmdToSurfaceflinger(START_STATISTIC_DIFF_FRAME);
            default:
        }
    }

    private void sendCmdToSurfaceflinger(int cmd) {
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        try {
            data.writeInt(cmd);
            data.writeInterfaceToken("android.ui.ISurfaceComposer");
            this.mFlinger.transact(STATISTIC_DIFFERENT_FRAME, data, reply, ACTION_DOWN);
            if (GET_DIFF_FRAME_COUNT == cmd) {
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
        }
    }
}
