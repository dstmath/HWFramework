package ohos.hiviewdfx.xcollie;

import ohos.hiviewdfx.FreezeDetectorUtils;
import ohos.hiviewdfx.HiLog;

public final class XCollie {
    public static final long XCOLLIE_FLAG_DEFAULT = -1;
    public static final long XCOLLIE_FLAG_LOG = 1;
    public static final long XCOLLIE_FLAG_NOOP = 0;
    public static final long XCOLLIE_FLAG_RECOVERY = 2;
    public static final int XCOLLIE_INVALID_ID = -1;
    public static final long XCOLLIE_LOCK = 1;
    public static final long XCOLLIE_THREAD = 2;
    private static volatile XCollie xCollie;

    private native void nativeCancelTimer(int i);

    private native boolean nativeRegisterXCollieChecker(XCollieChecker xCollieChecker, String str, long j);

    private native int nativeSetTimer(XCollieTimer xCollieTimer, String str, long j, long j2);

    private native boolean nativeUpdateTimer(int i, long j);

    private XCollie() {
    }

    public static XCollie getInstance() {
        FreezeDetectorUtils.loadJniLibrary();
        if (xCollie == null) {
            synchronized (XCollie.class) {
                if (xCollie == null) {
                    xCollie = new XCollie();
                }
            }
        }
        return xCollie;
    }

    public boolean registerXCollieChecker(XCollieChecker xCollieChecker, long j) {
        if (xCollieChecker == null) {
            HiLog.error(FreezeDetectorUtils.LOG_TAG, "input checker is null", new Object[0]);
            return false;
        } else if ((1 & j) != 0 || (2 & j) != 0) {
            return nativeRegisterXCollieChecker(xCollieChecker, xCollieChecker.getXCollieCheckerName(), j);
        } else {
            HiLog.error(FreezeDetectorUtils.LOG_TAG, "input checker is null", new Object[0]);
            return false;
        }
    }

    public int setTimeout(String str, int i, Runnable runnable, long j) {
        if (runnable == null) {
            HiLog.warn(FreezeDetectorUtils.LOG_TAG, "input callback is null", new Object[0]);
        }
        XCollieTimer xCollieTimer = new XCollieTimer("", str, runnable, (long) i);
        return nativeSetTimer(xCollieTimer, xCollieTimer.getTimerName(), xCollieTimer.getTimeout(), j);
    }

    public void cancelTimeout(int i) {
        nativeCancelTimer(i);
    }

    public boolean updateTimeout(int i, long j) {
        return nativeUpdateTimer(i, j);
    }
}
