package com.huawei.opcollect.utils;

import android.os.HandlerThread;
import android.os.Looper;
import java.io.PrintWriter;
import java.lang.reflect.Method;

public final class OPCollectThreadLooperCheck {
    private static final String CLASS_NAME_HIVEWLOOPERCHECK = "android.util.HiviewLooperCheck";
    private static final Method METHOD_CHECK = ReflectionUtils.getMethod(CLASS_NAME_HIVEWLOOPERCHECK, "check", HandlerThread.class);
    private static final Method METHOD_CHECK2 = ReflectionUtils.getMethod(CLASS_NAME_HIVEWLOOPERCHECK, "check", Looper.class, String.class);
    private static final Method METHOD_DUMPSTRING = ReflectionUtils.getMethod(CLASS_NAME_HIVEWLOOPERCHECK, "dumpString", new Class[0]);
    private static final String TAG = "OPCollectThreadLooper";

    private OPCollectThreadLooperCheck() {
        OPCollectLog.e(TAG, "static class should not initialize.");
    }

    public static void initLoopCheck(HandlerThread thread) {
        try {
            ReflectionUtils.invoke(METHOD_CHECK, null, thread);
        } catch (UnsupportedOperationException e) {
            OPCollectLog.i(TAG, "UnsupportedOperationException :" + e.getMessage());
        }
    }

    public static void initLoopCheck(Looper looper, String threadName) {
        try {
            ReflectionUtils.invoke(METHOD_CHECK2, null, looper, threadName);
        } catch (UnsupportedOperationException e) {
            OPCollectLog.i(TAG, "UnsupportedOperationException :" + e.getMessage());
        }
    }

    public static void writeDumpString(PrintWriter writer) {
        try {
            writer.println(ReflectionUtils.invoke(METHOD_DUMPSTRING, null));
        } catch (UnsupportedOperationException e) {
            OPCollectLog.i(TAG, "UnsupportedOperationException is " + e.getMessage());
        }
    }
}
