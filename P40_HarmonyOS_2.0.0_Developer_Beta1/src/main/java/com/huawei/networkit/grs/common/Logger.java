package com.huawei.networkit.grs.common;

import android.util.Log;
import java.util.IllegalFormatException;

public class Logger {
    private static final boolean DEBUG = false;
    private static final String TAG = "NetworkKit_Logger";
    private static final String TAG_NETWORKKIT_PRE = "NetworkKit_";

    public static void v(String tag, String format, Object... objects) {
    }

    public static void v(String tag, Object object) {
    }

    public static void d(String tag, Object object) {
    }

    public static void d(String tag, String format, Object... objects) {
    }

    public static void i(String tag, Object object) {
        Log.i(complexTag(tag), object == null ? "null" : object.toString());
    }

    public static void i(String tag, String format, Object... objects) {
        if (format == null) {
            Log.w(TAG, "format is null, not log");
            return;
        }
        try {
            Log.i(complexTag(tag), StringUtils.format(format, objects));
        } catch (IllegalFormatException e) {
            w(TAG, "log format error" + format, e);
        }
    }

    public static void w(String tag, Object object) {
        Log.w(complexTag(tag), object == null ? "null" : object.toString());
    }

    public static void w(String tag, String format, Object... objects) {
        if (format == null) {
            Log.w(TAG, "format is null, not log");
            return;
        }
        try {
            Log.w(complexTag(tag), StringUtils.format(format, objects));
        } catch (IllegalFormatException e) {
            w(TAG, "log format error" + format, e);
        }
    }

    public static void w(String tag, String s, Throwable e) {
        Log.w(complexTag(tag), s, getNewThrowable(e));
    }

    public static void e(String tag, Object object) {
        Log.e(complexTag(tag), object == null ? "null" : object.toString());
    }

    public static void e(String tag, String format, Object... objects) {
        if (format == null) {
            Log.w(TAG, "format is null, not log");
            return;
        }
        try {
            Log.e(complexTag(tag), StringUtils.format(format, objects));
        } catch (IllegalFormatException e) {
            w(TAG, "log format error" + format, e);
        }
    }

    public static void e(String tag, String s, Throwable e) {
        Log.e(complexTag(tag), s, getNewThrowable(e));
    }

    private static Throwable getNewThrowable(Throwable e) {
        if (e == null) {
            return null;
        }
        ThrowableWrapper retWrapper = new ThrowableWrapper(e);
        retWrapper.setStackTrace(e.getStackTrace());
        retWrapper.setMessage(StringUtils.anonymizeMessage(e.getMessage()));
        ThrowableWrapper preWrapper = retWrapper;
        for (Throwable currThrowable = e.getCause(); currThrowable != null; currThrowable = currThrowable.getCause()) {
            ThrowableWrapper currWrapper = new ThrowableWrapper(currThrowable);
            currWrapper.setStackTrace(currThrowable.getStackTrace());
            currWrapper.setMessage(StringUtils.anonymizeMessage(currThrowable.getMessage()));
            preWrapper.setCause(currWrapper);
            preWrapper = currWrapper;
        }
        return retWrapper;
    }

    private static String complexTag(String tag) {
        return TAG_NETWORKKIT_PRE + tag;
    }

    /* access modifiers changed from: private */
    public static class ThrowableWrapper extends Throwable {
        private static final long serialVersionUID = 7129050843360571879L;
        private String message;
        private Throwable ownerThrowable;
        private Throwable thisCause;

        private ThrowableWrapper(Throwable t) {
            this.ownerThrowable = t;
        }

        @Override // java.lang.Throwable
        public Throwable getCause() {
            Throwable th = this.thisCause;
            if (th == this) {
                return null;
            }
            return th;
        }

        /* access modifiers changed from: private */
        /* access modifiers changed from: public */
        private void setCause(Throwable cause) {
            this.thisCause = cause;
        }

        @Override // java.lang.Throwable
        public String getMessage() {
            return this.message;
        }

        public void setMessage(String message2) {
            this.message = message2;
        }

        @Override // java.lang.Throwable, java.lang.Object
        public String toString() {
            Throwable th = this.ownerThrowable;
            if (th == null) {
                return "";
            }
            String throwableClzName = th.getClass().getName();
            if (this.message == null) {
                return throwableClzName;
            }
            String prefix = throwableClzName + ": ";
            if (this.message.startsWith(prefix)) {
                return this.message;
            }
            return prefix + this.message;
        }
    }
}
