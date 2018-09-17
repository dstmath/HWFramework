package com.android.internal.logging;

import android.util.Log;
import com.android.internal.util.FastPrintWriter;
import dalvik.system.DalvikLogHandler;
import dalvik.system.DalvikLogging;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class AndroidHandler extends Handler implements DalvikLogHandler {
    private static final Formatter THE_FORMATTER = new Formatter() {
        public String format(LogRecord r) {
            Throwable thrown = r.getThrown();
            if (thrown == null) {
                return r.getMessage();
            }
            Writer sw = new StringWriter();
            PrintWriter pw = new FastPrintWriter(sw, false, 256);
            sw.write(r.getMessage());
            sw.write("\n");
            thrown.printStackTrace(pw);
            pw.flush();
            return sw.toString();
        }
    };

    public AndroidHandler() {
        setFormatter(THE_FORMATTER);
    }

    public void close() {
    }

    public void flush() {
    }

    public void publish(LogRecord record) {
        int level = getAndroidLevel(record.getLevel());
        String tag = DalvikLogging.loggerNameToTag(record.getLoggerName());
        if (Log.isLoggable(tag, level)) {
            try {
                Log.println(level, tag, getFormatter().format(record));
            } catch (RuntimeException e) {
                Log.e("AndroidHandler", "Error logging message.", e);
            }
        }
    }

    public void publish(Logger source, String tag, Level level, String message) {
        int priority = getAndroidLevel(level);
        if (Log.isLoggable(tag, priority)) {
            try {
                Log.println(priority, tag, message);
            } catch (RuntimeException e) {
                Log.e("AndroidHandler", "Error logging message.", e);
            }
        }
    }

    static int getAndroidLevel(Level level) {
        int value = level.intValue();
        if (value >= 1000) {
            return 6;
        }
        if (value >= 900) {
            return 5;
        }
        if (value >= 800) {
            return 4;
        }
        return 3;
    }
}
