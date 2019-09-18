package com.huawei.nb.utils.error;

import com.huawei.nb.exception.UndeliverableException;

public final class ErrorHelper {
    public static void onError(Throwable error) {
        if (error == null) {
            error = new NullPointerException("onError called with null. Null values are generally not allowed in 2.x operators and sources.");
        } else if (!isBug(error)) {
            error = new UndeliverableException(error);
        }
        uncaught(error);
    }

    private static boolean isBug(Throwable error) {
        if (!(error instanceof IllegalStateException) && !(error instanceof NullPointerException) && !(error instanceof IllegalArgumentException)) {
            return false;
        }
        return true;
    }

    private static void uncaught(Throwable error) {
        Thread currentThread = Thread.currentThread();
        currentThread.getUncaughtExceptionHandler().uncaughtException(currentThread, error);
    }
}
