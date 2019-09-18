package com.android.server.wifi;

import android.util.Log;
import com.android.internal.annotations.Immutable;
import com.android.internal.annotations.VisibleForTesting;
import com.android.server.wifi.WifiLog;
import javax.annotation.concurrent.ThreadSafe;

@Immutable
@ThreadSafe
class LogcatLog implements WifiLog {
    private static final String[] TRACE_FRAMES_TO_IGNORE = {"getNameOfCallingMethod()", "trace()"};
    private static final DummyLogMessage sDummyLogMessage = new DummyLogMessage();
    private static volatile boolean sVerboseLogging = false;
    private final String mTag;

    private static class RealLogMessage implements WifiLog.LogMessage {
        private final String mFormat;
        private final int mLogLevel;
        private int mNextFormatCharPos;
        private final StringBuilder mStringBuilder;
        private final String mTag;

        RealLogMessage(int logLevel, String tag, String format) {
            this(logLevel, tag, format, null);
        }

        RealLogMessage(int logLevel, String tag, String format, String prefix) {
            this.mLogLevel = logLevel;
            this.mTag = tag;
            this.mFormat = format;
            this.mStringBuilder = new StringBuilder();
            this.mNextFormatCharPos = 0;
            if (prefix != null) {
                StringBuilder sb = this.mStringBuilder;
                sb.append(prefix);
                sb.append(" ");
            }
        }

        public WifiLog.LogMessage r(String value) {
            return c(value);
        }

        public WifiLog.LogMessage c(String value) {
            copyUntilPlaceholder();
            if (this.mNextFormatCharPos < this.mFormat.length()) {
                this.mStringBuilder.append(value);
                this.mNextFormatCharPos++;
            }
            return this;
        }

        public WifiLog.LogMessage c(long value) {
            copyUntilPlaceholder();
            if (this.mNextFormatCharPos < this.mFormat.length()) {
                this.mStringBuilder.append(value);
                this.mNextFormatCharPos++;
            }
            return this;
        }

        public WifiLog.LogMessage c(char value) {
            copyUntilPlaceholder();
            if (this.mNextFormatCharPos < this.mFormat.length()) {
                this.mStringBuilder.append(value);
                this.mNextFormatCharPos++;
            }
            return this;
        }

        public WifiLog.LogMessage c(boolean value) {
            copyUntilPlaceholder();
            if (this.mNextFormatCharPos < this.mFormat.length()) {
                this.mStringBuilder.append(value);
                this.mNextFormatCharPos++;
            }
            return this;
        }

        public void flush() {
            if (this.mNextFormatCharPos < this.mFormat.length()) {
                this.mStringBuilder.append(this.mFormat, this.mNextFormatCharPos, this.mFormat.length());
            }
            Log.println(this.mLogLevel, this.mTag, this.mStringBuilder.toString());
        }

        @VisibleForTesting
        public String toString() {
            return this.mStringBuilder.toString();
        }

        private void copyUntilPlaceholder() {
            if (this.mNextFormatCharPos < this.mFormat.length()) {
                int placeholderPos = this.mFormat.indexOf(37, this.mNextFormatCharPos);
                if (placeholderPos == -1) {
                    placeholderPos = this.mFormat.length();
                }
                this.mStringBuilder.append(this.mFormat, this.mNextFormatCharPos, placeholderPos);
                this.mNextFormatCharPos = placeholderPos;
            }
        }
    }

    LogcatLog(String tag) {
        this.mTag = tag;
    }

    public static void enableVerboseLogging(int verboseMode) {
        if (verboseMode > 0) {
            sVerboseLogging = true;
        } else {
            sVerboseLogging = false;
        }
    }

    public WifiLog.LogMessage err(String format) {
        return new RealLogMessage(6, this.mTag, format);
    }

    public WifiLog.LogMessage warn(String format) {
        return new RealLogMessage(5, this.mTag, format);
    }

    public WifiLog.LogMessage info(String format) {
        return new RealLogMessage(4, this.mTag, format);
    }

    public WifiLog.LogMessage trace(String format) {
        if (sVerboseLogging) {
            return new RealLogMessage(3, this.mTag, format, getNameOfCallingMethod(0));
        }
        return sDummyLogMessage;
    }

    public WifiLog.LogMessage trace(String format, int numFramesToIgnore) {
        if (sVerboseLogging) {
            return new RealLogMessage(3, this.mTag, format, getNameOfCallingMethod(numFramesToIgnore));
        }
        return sDummyLogMessage;
    }

    public WifiLog.LogMessage dump(String format) {
        if (sVerboseLogging) {
            return new RealLogMessage(2, this.mTag, format);
        }
        return sDummyLogMessage;
    }

    public void eC(String msg) {
        Log.e(this.mTag, msg);
    }

    public void wC(String msg) {
        Log.w(this.mTag, msg);
    }

    public void iC(String msg) {
        Log.i(this.mTag, msg);
    }

    public void tC(String msg) {
        Log.d(this.mTag, msg);
    }

    public void e(String msg) {
        Log.e(this.mTag, msg);
    }

    public void w(String msg) {
        Log.w(this.mTag, msg);
    }

    public void i(String msg) {
        Log.i(this.mTag, msg);
    }

    public void d(String msg) {
        Log.d(this.mTag, msg);
    }

    public void v(String msg) {
        Log.v(this.mTag, msg);
    }

    private String getNameOfCallingMethod(int callerFramesToIgnore) {
        try {
            return new Throwable().getStackTrace()[TRACE_FRAMES_TO_IGNORE.length + callerFramesToIgnore].getMethodName();
        } catch (ArrayIndexOutOfBoundsException e) {
            return "<unknown>";
        }
    }
}
