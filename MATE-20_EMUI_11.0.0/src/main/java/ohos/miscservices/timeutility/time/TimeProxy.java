package ohos.miscservices.timeutility.time;

import java.util.TimeZone;
import ohos.app.Context;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.rpc.RemoteException;

public class TimeProxy {
    private static final int JNI_INIT_ERROR = -10001;
    private static final int NO_ERROR = 0;
    private static final long SECOND2NANOSECOND = 1000000000;
    private static final HiLogLabel TAG = new HiLogLabel(3, 0, "Timer");
    private static final int TIME_CLOCK_BOOTTIME = 5;
    private static final int TIME_CLOCK_MONOTOTIC = 2;
    private static final int TIME_CLOCK_REALTIME = 1;
    private static final int TIME_CLOCK_RPOCESS_CPUTIME_ID = 3;
    private static final int TIME_CLOCK_THREAD_CPUTIME_ID = 4;
    private static final int TYPE_ERROR = -10000;
    private static TimeProxy instance = new TimeProxy();
    private static boolean jniInit;
    private TimeProxyImpl mTimeProxyImpl = new TimeProxyImpl();

    private native long getKernelTime(int i);

    private native void getKernelTimeNs(int i, long[] jArr);

    static {
        jniInit = false;
        System.loadLibrary("time_jni.z");
        jniInit = true;
    }

    private TimeProxy() {
    }

    public static TimeProxy getInstance() {
        return instance;
    }

    public long getTime(int i) {
        if (i > 5 || i < 1) {
            HiLog.error(TAG, "getTime error type = %{public}d", Integer.valueOf(i));
            return -10000;
        } else if (jniInit) {
            return getKernelTime(i);
        } else {
            HiLog.error(TAG, "getTime jniInit error ", new Object[0]);
            return -10001;
        }
    }

    public long getTimeNs(int i) {
        if (i > 5 || i < 1) {
            HiLog.error(TAG, "getTimeNs error type = %{public}d", Integer.valueOf(i));
            return -10000;
        }
        long[] jArr = new long[2];
        getKernelTimeNs(i, jArr);
        return (jArr[0] * SECOND2NANOSECOND) + jArr[1];
    }

    public String getTimeZone() {
        return TimeZone.getDefault().getID();
    }

    public void setTime(Context context, long j) throws RemoteException {
        this.mTimeProxyImpl.setTime(j);
    }

    public void setTimeZone(Context context, String str) throws RemoteException {
        this.mTimeProxyImpl.setTimeZone(str);
    }
}
