package com.huawei.android.app;

import android.common.HwFrameworkFactory;
import android.util.Log;
import android.util.Singleton;

public final class HiTrace {
    public static final int HITRACE_CM_DEFAULT = 0;
    public static final int HITRACE_CM_DEVICE = 3;
    public static final int HITRACE_CM_MAX = 3;
    public static final int HITRACE_CM_MIN = 0;
    public static final int HITRACE_CM_PROCESS = 2;
    public static final int HITRACE_CM_THREAD = 1;
    public static final int HITRACE_FLAG_D2D_TP_INFO = 64;
    public static final int HITRACE_FLAG_DEFAULT = 0;
    public static final int HITRACE_FLAG_DONOT_CREATE_SPAN = 2;
    public static final int HITRACE_FLAG_DONOT_ENABLE_LOG = 16;
    public static final int HITRACE_FLAG_FAILURE_TRIGGER = 32;
    public static final int HITRACE_FLAG_INCLUDE_ASYNC = 1;
    public static final int HITRACE_FLAG_MAX = 127;
    public static final int HITRACE_FLAG_MIN = 0;
    public static final int HITRACE_FLAG_NO_BE_INFO = 8;
    public static final int HITRACE_FLAG_TP_INFO = 4;
    public static final int HITRACE_ID_INVALID = 0;
    public static final int HITRACE_ID_VALID = 1;
    public static final int HITRACE_TP_CR = 1;
    public static final int HITRACE_TP_CS = 0;
    public static final int HITRACE_TP_GENERAL = 4;
    public static final int HITRACE_TP_MAX = 4;
    public static final int HITRACE_TP_MIN = 0;
    public static final int HITRACE_TP_SR = 3;
    public static final int HITRACE_TP_SS = 2;
    private static final Singleton<Boolean> SINGLETON_HITRACE = new Singleton<Boolean>() {
        /* class com.huawei.android.app.HiTrace.AnonymousClass1 */

        /* access modifiers changed from: protected */
        public Boolean create() {
            boolean hiTraceDefined = true;
            try {
                Class.forName("huawei.hiview.HiTrace");
            } catch (ClassNotFoundException e) {
                hiTraceDefined = false;
                Log.e(HiTrace.TAG, "ClassNotFoundException: huawei.hiview.HiTrace not exsist.");
            }
            return new Boolean(hiTraceDefined);
        }
    };
    private static final String TAG = "HiTrace";

    protected static boolean isHiTraceSdkExist() {
        return ((Boolean) SINGLETON_HITRACE.get()).booleanValue();
    }

    private HiTrace() {
    }

    public static HiTraceId begin(String name, int flags) {
        if (!isHiTraceSdkExist()) {
            return new HiTraceId();
        }
        return new HiTraceId(HwFrameworkFactory.getHiTrace().begin(name, flags));
    }

    public static void end(HiTraceId id) {
        if (isHiTraceSdkExist() && id != null) {
            HwFrameworkFactory.getHiTrace().end(id.hitraceIdImpl);
        }
    }

    public static HiTraceId getId() {
        if (!isHiTraceSdkExist()) {
            return new HiTraceId();
        }
        return new HiTraceId(HwFrameworkFactory.getHiTrace().getId());
    }

    public static void setId(HiTraceId id) {
        if (isHiTraceSdkExist() && id != null) {
            HwFrameworkFactory.getHiTrace().setId(id.hitraceIdImpl);
        }
    }

    public static void clearId() {
        if (isHiTraceSdkExist()) {
            HwFrameworkFactory.getHiTrace().clearId();
        }
    }

    public static HiTraceId createSpan() {
        if (!isHiTraceSdkExist()) {
            return new HiTraceId();
        }
        return new HiTraceId(HwFrameworkFactory.getHiTrace().createSpan());
    }

    public static void tracePoint(int type, HiTraceId id, String fmt, Object... args) {
        if (isHiTraceSdkExist() && id != null) {
            HwFrameworkFactory.getHiTrace().tracePoint(type, id.hitraceIdImpl, fmt, args);
        }
    }

    public static void tracePoint(int mode, int type, HiTraceId id, String fmt, Object... args) {
        if (isHiTraceSdkExist() && id != null) {
            HwFrameworkFactory.getHiTrace().tracePoint(mode, type, id.hitraceIdImpl, fmt, args);
        }
    }
}
