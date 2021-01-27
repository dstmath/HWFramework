package com.huawei.internal.telephony.vsim.util;

import com.android.internal.telephony.vsim.HwVSimConstants;
import com.android.internal.telephony.vsim.HwVSimLog;
import com.huawei.android.os.AsyncResultEx;

public final class AsyncResultUtil {
    private static final String TAG = "AsyncResultUtil";

    private AsyncResultUtil() {
    }

    public static <T> T getResult(AsyncResultEx asyncResultEx, Class<T> clz) {
        return (T) getResult(asyncResultEx, clz, null);
    }

    public static <T> T getResult(AsyncResultEx asyncResultEx, Class<T> clz, T def) {
        if (asyncResultEx == null || clz == null) {
            HwVSimLog.error(TAG, "get: return def, AsyncResultEx or class null");
            return def;
        }
        Throwable throwable = asyncResultEx.getException();
        if (throwable != null) {
            StringBuilder sb = new StringBuilder();
            sb.append("get: return def, exception");
            sb.append(HwVSimConstants.DEBUG ? throwable.getMessage() : "***");
            HwVSimLog.error(TAG, sb.toString());
            return def;
        }
        T t = (T) asyncResultEx.getResult();
        if (t == null) {
            HwVSimLog.error(TAG, "get: result null, return def");
            return def;
        } else if (clz.isInstance(t)) {
            return t;
        } else {
            HwVSimLog.error(TAG, "get: return def, Result (:" + t.getClass().getCanonicalName() + ") can not cast to:(" + clz + ")");
            return def;
        }
    }

    public static <T> T[] getArrayResult(AsyncResultEx asyncResultEx) {
        return (T[]) getArrayResult(asyncResultEx, null);
    }

    public static <T> T[] getArrayResult(AsyncResultEx asyncResultEx, T[] def) {
        if (asyncResultEx == null) {
            HwVSimLog.error(TAG, "getArrayResult: asyncResult def");
            return def;
        }
        Throwable throwable = asyncResultEx.getException();
        if (throwable != null) {
            StringBuilder sb = new StringBuilder();
            sb.append("getArrayResult: exception = ");
            sb.append(HwVSimConstants.DEBUG ? throwable.getMessage() : "***");
            HwVSimLog.error(TAG, sb.toString());
            return def;
        }
        try {
            return (T[]) ((Object[]) asyncResultEx.getResult());
        } catch (Exception e) {
            StringBuilder sb2 = new StringBuilder();
            sb2.append("getArrayResult: class case catch exception= ");
            sb2.append(HwVSimConstants.DEBUG ? e.getMessage() : "*** ");
            HwVSimLog.error(TAG, sb2.toString());
            return def;
        }
    }

    public static int[] getIntArrayResult(AsyncResultEx asyncResultEx) {
        return getIntArrayResult(asyncResultEx, null);
    }

    public static int[] getIntArrayResult(AsyncResultEx asyncResultEx, int[] def) {
        if (asyncResultEx == null) {
            return def;
        }
        Throwable throwable = asyncResultEx.getException();
        String str = "***";
        if (throwable != null) {
            StringBuilder sb = new StringBuilder();
            sb.append("getIntArrayResult: exception = ");
            if (HwVSimConstants.DEBUG) {
                str = throwable.getMessage();
            }
            sb.append(str);
            HwVSimLog.error(TAG, sb.toString());
            return def;
        }
        try {
            return (int[]) asyncResultEx.getResult();
        } catch (Exception e) {
            StringBuilder sb2 = new StringBuilder();
            sb2.append("getIntArrayResult: class case catch exception =");
            if (HwVSimConstants.DEBUG) {
                str = e.getMessage();
            }
            sb2.append(str);
            HwVSimLog.error(TAG, sb2.toString());
            return def;
        }
    }
}
