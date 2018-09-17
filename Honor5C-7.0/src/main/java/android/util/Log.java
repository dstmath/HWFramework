package android.util;

import android.os.DeadSystemException;
import android.os.SystemProperties;
import com.android.internal.util.FastPrintWriter;
import com.android.internal.util.LineBreakBufferedWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.UnknownHostException;
import java.util.ArrayList;
import javax.microedition.khronos.opengles.GL10;

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
    private static TerribleFailureHandler sWtfHandler;

    public interface TerribleFailureHandler {
        void onTerribleFailure(String str, TerribleFailure terribleFailure, boolean z);
    }

    private static class ImmediateLogWriter extends Writer {
        private int bufID;
        private int priority;
        private String tag;
        private int written;

        public ImmediateLogWriter(int bufID, int priority, String tag) {
            this.written = Log.LOG_ID_MAIN;
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
        public static final int LOGGER_ENTRY_MAX_PAYLOAD = 0;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.util.Log.NoPreloadHolder.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.util.Log.NoPreloadHolder.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 8 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 9 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: android.util.Log.NoPreloadHolder.<clinit>():void");
        }

        NoPreloadHolder() {
        }
    }

    private static class TerribleFailure extends Exception {
        TerribleFailure(String msg, Throwable cause) {
            super(msg, cause);
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.util.Log.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.util.Log.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.util.Log.<clinit>():void");
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
        return println_native(LOG_ID_MAIN, VERBOSE, tag, msg);
    }

    public static int v(String tag, String msg, Throwable tr) {
        return printlns(LOG_ID_MAIN, VERBOSE, tag, msg, tr);
    }

    public static int d(String tag, String msg) {
        return println_native(LOG_ID_MAIN, LOG_ID_SYSTEM, tag, msg);
    }

    public static int d(String tag, String msg, Throwable tr) {
        return printlns(LOG_ID_MAIN, LOG_ID_SYSTEM, tag, msg, tr);
    }

    public static int i(String tag, String msg) {
        return println_native(LOG_ID_MAIN, LOG_ID_CRASH, tag, msg);
    }

    public static int i(String tag, String msg, Throwable tr) {
        return printlns(LOG_ID_MAIN, LOG_ID_CRASH, tag, msg, tr);
    }

    public static void i(ArrayList<String> applicationInfo, String callingMethod, String description) {
        if (HWINFO) {
            i("ctaifs<" + ((String) applicationInfo.get(LOG_ID_MAIN)) + ">[" + ((String) applicationInfo.get(LOG_ID_RADIO)) + "][" + ((String) applicationInfo.get(VERBOSE)) + "]", "[" + callingMethod + "] " + description);
        }
    }

    public static int w(String tag, String msg) {
        return println_native(LOG_ID_MAIN, WARN, tag, msg);
    }

    public static int w(String tag, String msg, Throwable tr) {
        return printlns(LOG_ID_MAIN, WARN, tag, msg, tr);
    }

    public static int w(String tag, Throwable tr) {
        return printlns(LOG_ID_MAIN, WARN, tag, "", tr);
    }

    public static int e(String tag, String msg) {
        return println_native(LOG_ID_MAIN, ERROR, tag, msg);
    }

    public static int e(String tag, String msg, Throwable tr) {
        return printlns(LOG_ID_MAIN, ERROR, tag, msg, tr);
    }

    public static int wtf(String tag, String msg) {
        return wtf(LOG_ID_MAIN, tag, msg, null, false, false);
    }

    public static int wtfStack(String tag, String msg) {
        return wtf(LOG_ID_MAIN, tag, msg, null, true, false);
    }

    public static int wtf(String tag, Throwable tr) {
        return wtf(LOG_ID_MAIN, tag, tr.getMessage(), tr, false, false);
    }

    public static int wtf(String tag, String msg, Throwable tr) {
        return wtf(LOG_ID_MAIN, tag, msg, tr, false, false);
    }

    static int wtf(int logId, String tag, String msg, Throwable tr, boolean localStack, boolean system) {
        Throwable what = new TerribleFailure(msg, tr);
        if (localStack) {
            tr = what;
        }
        int bytes = printlns(logId, ERROR, tag, msg, tr);
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
            return "";
        }
        for (Throwable t = tr; t != null; t = t.getCause()) {
            if (t instanceof UnknownHostException) {
                return "";
            }
        }
        Writer sw = new StringWriter();
        PrintWriter pw = new FastPrintWriter(sw, false, (int) GL10.GL_DEPTH_BUFFER_BIT);
        tr.printStackTrace(pw);
        pw.flush();
        return sw.toString();
    }

    public static int println(int priority, String tag, String msg) {
        return println_native(LOG_ID_MAIN, priority, tag, msg);
    }

    public static int printlns(int bufID, int priority, String tag, String msg, Throwable tr) {
        ImmediateLogWriter logWriter = new ImmediateLogWriter(bufID, priority, tag);
        LineBreakBufferedWriter lbbw = new LineBreakBufferedWriter(logWriter, Math.max(((NoPreloadHolder.LOGGER_ENTRY_MAX_PAYLOAD - 2) - (tag != null ? tag.length() : LOG_ID_MAIN)) - 32, 100));
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
