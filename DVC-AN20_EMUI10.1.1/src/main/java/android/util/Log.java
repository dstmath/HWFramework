package android.util;

import android.annotation.UnsupportedAppUsage;
import android.os.DeadSystemException;
import android.os.SystemProperties;
import com.android.internal.os.RuntimeInit;
import com.android.internal.util.FastPrintWriter;
import com.android.internal.util.LineBreakBufferedWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.net.UnknownHostException;

public final class Log {
    public static final int ASSERT = 7;
    public static final int DEBUG = 3;
    public static final int ERROR = 6;
    public static boolean HWINFO = false;
    public static boolean HWLog = false;
    public static boolean HWModuleLog = false;
    public static final int INFO = 4;
    public static final int LOG_ID_CRASH = 4;
    public static final int LOG_ID_EVENTS = 2;
    public static final int LOG_ID_MAIN = 0;
    public static final int LOG_ID_RADIO = 1;
    public static final int LOG_ID_SYSTEM = 3;
    public static final int VERBOSE = 2;
    public static final int WARN = 5;
    private static TerribleFailureHandler sWtfHandler = new TerribleFailureHandler() {
        /* class android.util.Log.AnonymousClass1 */

        @Override // android.util.Log.TerribleFailureHandler
        public void onTerribleFailure(String tag, TerribleFailure what, boolean system) {
            RuntimeInit.wtf(tag, what, system);
        }
    };

    @Retention(RetentionPolicy.SOURCE)
    public @interface Level {
    }

    public interface TerribleFailureHandler {
        void onTerribleFailure(String str, TerribleFailure terribleFailure, boolean z);
    }

    public static native boolean isLoggable(String str, int i);

    /* access modifiers changed from: private */
    public static native int logger_entry_max_payload_native();

    public static native int print_powerlog_native(int i, String str, String str2);

    @UnsupportedAppUsage
    public static native int println_native(int i, int i2, String str, String str2);

    public static class TerribleFailure extends Exception {
        TerribleFailure(String msg, Throwable cause) {
            super(msg, cause);
        }
    }

    private Log() {
    }

    public static void initHWLog() {
        boolean z = false;
        HWLog = SystemProperties.getBoolean("ro.config.hw_log", false);
        HWModuleLog = SystemProperties.getBoolean("ro.config.hw_module_log", false);
        if (SystemProperties.getBoolean("ro.debuggable", false) || SystemProperties.getBoolean("persist.sys.huawei.debug.on", false)) {
            z = true;
        }
        HWINFO = z;
    }

    public static int v(String tag, String msg) {
        return println_native(0, 2, tag, msg);
    }

    public static int v(String tag, String msg, Throwable tr) {
        return printlns(0, 2, tag, msg, tr);
    }

    public static int d(String tag, String msg) {
        return println_native(0, 3, tag, msg);
    }

    public static int d(String tag, String msg, Throwable tr) {
        return printlns(0, 3, tag, msg, tr);
    }

    public static int i(String tag, String msg) {
        return println_native(0, 4, tag, msg);
    }

    public static int i(String tag, String msg, Throwable tr) {
        return printlns(0, 4, tag, msg, tr);
    }

    public static int w(String tag, String msg) {
        return println_native(0, 5, tag, msg);
    }

    public static int w(String tag, String msg, Throwable tr) {
        return printlns(0, 5, tag, msg, tr);
    }

    public static int w(String tag, Throwable tr) {
        return printlns(0, 5, tag, "", tr);
    }

    public static int e(String tag, String msg) {
        return println_native(0, 6, tag, msg);
    }

    public static int e(String tag, String msg, Throwable tr) {
        return printlns(0, 6, tag, msg, tr);
    }

    public static int wtf(String tag, String msg) {
        return wtf(0, tag, msg, null, false, false);
    }

    public static int wtfStack(String tag, String msg) {
        return wtf(0, tag, msg, null, true, false);
    }

    public static int wtf(String tag, Throwable tr) {
        return wtf(0, tag, tr.getMessage(), tr, false, false);
    }

    public static int wtf(String tag, String msg, Throwable tr) {
        return wtf(0, tag, msg, tr, false, false);
    }

    @UnsupportedAppUsage
    static int wtf(int logId, String tag, String msg, Throwable tr, boolean localStack, boolean system) {
        TerribleFailure what = new TerribleFailure(msg, tr);
        int bytes = printlns(logId, 6, tag, msg, localStack ? what : tr);
        sWtfHandler.onTerribleFailure(tag, what, system);
        return bytes;
    }

    static void wtfQuiet(int logId, String tag, String msg, boolean system) {
        sWtfHandler.onTerribleFailure(tag, new TerribleFailure(msg, null), system);
    }

    public static TerribleFailureHandler setWtfHandler(TerribleFailureHandler handler) {
        if (handler != null) {
            TerribleFailureHandler oldHandler = sWtfHandler;
            sWtfHandler = handler;
            return oldHandler;
        }
        throw new NullPointerException("handler == null");
    }

    public static String getStackTraceString(Throwable tr) {
        if (tr == null) {
            return "";
        }
        for (Throwable t = tr; t != null; t = t.getCause()) {
            if (t instanceof UnknownHostException) {
                return "";
            }
        }
        StringWriter sw = new StringWriter();
        PrintWriter pw = new FastPrintWriter((Writer) sw, false, 256);
        tr.printStackTrace(pw);
        pw.flush();
        return sw.toString();
    }

    public static int println(int priority, String tag, String msg) {
        return println_native(0, priority, tag, msg);
    }

    public static int printlns(int bufID, int priority, String tag, String msg, Throwable tr) {
        ImmediateLogWriter logWriter = new ImmediateLogWriter(bufID, priority, tag);
        LineBreakBufferedWriter lbbw = new LineBreakBufferedWriter(logWriter, Math.max(((PreloadHolder.LOGGER_ENTRY_MAX_PAYLOAD - 2) - (tag != null ? tag.length() : 0)) - 32, 100));
        lbbw.println(msg);
        if (tr != null) {
            Throwable t = tr;
            while (true) {
                if (t == null || (t instanceof UnknownHostException)) {
                    break;
                } else if (t instanceof DeadSystemException) {
                    lbbw.println("DeadSystemException: The system died; earlier logs will point to the root cause");
                    break;
                } else {
                    t = t.getCause();
                }
            }
            if (t == null) {
                tr.printStackTrace(lbbw);
            }
        }
        lbbw.flush();
        return logWriter.getWritten();
    }

    /* access modifiers changed from: package-private */
    public static class PreloadHolder {
        public static final int LOGGER_ENTRY_MAX_PAYLOAD = Log.logger_entry_max_payload_native();

        PreloadHolder() {
        }
    }

    /* access modifiers changed from: private */
    public static class ImmediateLogWriter extends Writer {
        private int bufID;
        private int priority;
        private String tag;
        private int written = 0;

        public ImmediateLogWriter(int bufID2, int priority2, String tag2) {
            this.bufID = bufID2;
            this.priority = priority2;
            this.tag = tag2;
        }

        public int getWritten() {
            return this.written;
        }

        @Override // java.io.Writer
        public void write(char[] cbuf, int off, int len) {
            this.written += Log.println_native(this.bufID, this.priority, this.tag, new String(cbuf, off, len));
        }

        @Override // java.io.Writer, java.io.Flushable
        public void flush() {
        }

        @Override // java.io.Closeable, java.io.Writer, java.lang.AutoCloseable
        public void close() {
        }
    }
}
