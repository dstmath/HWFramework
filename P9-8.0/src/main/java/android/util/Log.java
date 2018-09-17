package android.util;

import android.os.DeadSystemException;
import android.os.SystemProperties;
import com.android.internal.os.RuntimeInit;
import com.android.internal.util.FastPrintWriter;
import com.android.internal.util.LineBreakBufferedWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.UnknownHostException;
import java.util.ArrayList;

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
        public void onTerribleFailure(String tag, TerribleFailure what, boolean system) {
            RuntimeInit.wtf(tag, what, system);
        }
    };

    public interface TerribleFailureHandler {
        void onTerribleFailure(String str, TerribleFailure terribleFailure, boolean z);
    }

    private static class ImmediateLogWriter extends Writer {
        private int bufID;
        private int priority;
        private String tag;
        private int written = 0;

        public ImmediateLogWriter(int bufID, int priority, String tag) {
            this.bufID = bufID;
            this.priority = priority;
            this.tag = tag;
        }

        public int getWritten() {
            return this.written;
        }

        public void write(char[] cbuf, int off, int len) {
            this.written += Log.println_native(this.bufID, this.priority, this.tag, new String(cbuf, off, len));
        }

        public void flush() {
        }

        public void close() {
        }
    }

    static class NoPreloadHolder {
        public static final int LOGGER_ENTRY_MAX_PAYLOAD = Log.logger_entry_max_payload_native();

        NoPreloadHolder() {
        }
    }

    private static class TerribleFailure extends Exception {
        TerribleFailure(String msg, Throwable cause) {
            super(msg, cause);
        }
    }

    public static native boolean isLoggable(String str, int i);

    private static native int logger_entry_max_payload_native();

    public static native int print_powerlog_native(int i, String str, String str2);

    public static native int println_native(int i, int i2, String str, String str2);

    private Log() {
    }

    public static void initHWLog() {
        boolean z;
        HWLog = SystemProperties.getBoolean("ro.config.hw_log", false);
        HWModuleLog = SystemProperties.getBoolean("ro.config.hw_module_log", false);
        if (SystemProperties.getBoolean("ro.debuggable", false)) {
            z = true;
        } else {
            z = SystemProperties.getBoolean("persist.sys.huawei.debug.on", false);
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

    public static void i(ArrayList<String> applicationInfo, String callingMethod, String description) {
        if (HWINFO) {
            i("ctaifs<" + ((String) applicationInfo.get(0)) + ">[" + ((String) applicationInfo.get(1)) + "][" + ((String) applicationInfo.get(2)) + "]", "[" + callingMethod + "] " + description);
        }
    }

    public static int w(String tag, String msg) {
        return println_native(0, 5, tag, msg);
    }

    public static int w(String tag, String msg, Throwable tr) {
        return printlns(0, 5, tag, msg, tr);
    }

    public static int w(String tag, Throwable tr) {
        return printlns(0, 5, tag, LogException.NO_VALUE, tr);
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

    static int wtf(int logId, String tag, String msg, Throwable tr, boolean localStack, boolean system) {
        Throwable what = new TerribleFailure(msg, tr);
        if (localStack) {
            tr = what;
        }
        int bytes = printlns(logId, 6, tag, msg, tr);
        sWtfHandler.onTerribleFailure(tag, what, system);
        return bytes;
    }

    static void wtfQuiet(int logId, String tag, String msg, boolean system) {
        sWtfHandler.onTerribleFailure(tag, new TerribleFailure(msg, null), system);
    }

    public static TerribleFailureHandler setWtfHandler(TerribleFailureHandler handler) {
        if (handler == null) {
            throw new NullPointerException("handler == null");
        }
        TerribleFailureHandler oldHandler = sWtfHandler;
        sWtfHandler = handler;
        return oldHandler;
    }

    public static String getStackTraceString(Throwable tr) {
        if (tr == null) {
            return LogException.NO_VALUE;
        }
        for (Throwable t = tr; t != null; t = t.getCause()) {
            if (t instanceof UnknownHostException) {
                return LogException.NO_VALUE;
            }
        }
        Writer sw = new StringWriter();
        PrintWriter pw = new FastPrintWriter(sw, false, 256);
        tr.printStackTrace(pw);
        pw.flush();
        return sw.toString();
    }

    public static int println(int priority, String tag, String msg) {
        return println_native(0, priority, tag, msg);
    }

    public static int printlns(int bufID, int priority, String tag, String msg, Throwable tr) {
        ImmediateLogWriter logWriter = new ImmediateLogWriter(bufID, priority, tag);
        LineBreakBufferedWriter lbbw = new LineBreakBufferedWriter(logWriter, Math.max(((NoPreloadHolder.LOGGER_ENTRY_MAX_PAYLOAD - 2) - (tag != null ? tag.length() : 0)) - 32, 100));
        lbbw.println(msg);
        if (tr != null) {
            Throwable t = tr;
            while (t != null && !(t instanceof UnknownHostException)) {
                if (t instanceof DeadSystemException) {
                    lbbw.println("DeadSystemException: The system died; earlier logs will point to the root cause");
                    break;
                }
                t = t.getCause();
            }
            if (t == null) {
                tr.printStackTrace(lbbw);
            }
        }
        lbbw.flush();
        return logWriter.getWritten();
    }
}
