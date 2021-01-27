package ohos.miscservices.timeutility;

import java.util.Date;
import ohos.app.Context;
import ohos.hiviewdfx.HiLog;
import ohos.hiviewdfx.HiLogLabel;
import ohos.miscservices.timeutility.time.TimeProxy;
import ohos.rpc.RemoteException;

public class Time {
    private static final HiLogLabel TAG = new HiLogLabel(3, 218110976, "Time");
    private static final int TIME_CLOCK_BOOTTIME = 5;
    private static final int TIME_CLOCK_MONOTOTIC = 2;
    private static final int TIME_CLOCK_REALTIME = 1;
    private static final int TIME_CLOCK_RPOCESS_CPUTIME_ID = 3;
    private static final int TIME_CLOCK_THREAD_CPUTIME_ID = 4;
    private static final int TIME_JNI_INIT_ERROR = -10001;
    private static final int TIME_NO_ERROR = 0;
    private static final int TIME_SA_GET_FAIL_ERROR = -10003;
    private static final int TIME_TRANSACT_ERROR = -10002;
    private static final int TIME_TYPE_ERROR = -10000;

    private static long getTime(int i) {
        return TimeProxy.getInstance().getTime(i);
    }

    private static long getTimeNs(int i) {
        return TimeProxy.getInstance().getTimeNs(i);
    }

    public static long getCurrentTime() {
        return getTime(1);
    }

    public static long getCurrentTimeNs() {
        return getTimeNs(1);
    }

    public static long getRealActiveTime() {
        return getTime(2);
    }

    public static long getRealActiveTimeNs() {
        return getTimeNs(2);
    }

    public static long getRealTime() {
        return getTime(5);
    }

    public static long getRealTimeNs() {
        return getTimeNs(5);
    }

    public static long getCurrentThreadTime() {
        return getTime(4);
    }

    public static long getCurrentThreadTimeNs() {
        return getTimeNs(4);
    }

    public static long setTime(Context context, long j) {
        try {
            TimeProxy.getInstance().setTime(context, j);
            return 0;
        } catch (RemoteException unused) {
            HiLog.error(TAG, "setTime system ability is not ready!", new Object[0]);
            return -10003;
        }
    }

    public static long setTime(Context context, Date date) {
        return setTime(context, date.getTime());
    }

    public static String getTimeZone() {
        return TimeProxy.getInstance().getTimeZone();
    }

    public static long setTimeZone(Context context, String str) {
        try {
            TimeProxy.getInstance().setTimeZone(context, str);
            return 0;
        } catch (RemoteException unused) {
            HiLog.error(TAG, "setTimeZone system ability is not ready!", new Object[0]);
            return -10003;
        }
    }

    public static void sleep(long j) {
        HiLog.info(TAG, "sleep: %{public}zu", Long.valueOf(j));
        long realActiveTime = getRealActiveTime();
        boolean z = false;
        long j2 = j;
        do {
            try {
                Thread.sleep(j2);
            } catch (InterruptedException unused) {
                z = true;
            }
            j2 = (realActiveTime + j) - getRealActiveTime();
        } while (j2 > 0);
        if (z) {
            Thread.currentThread().interrupt();
        }
    }
}
