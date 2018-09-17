package com.android.server.wifi;

import android.util.Log;
import com.android.internal.annotations.Immutable;
import com.android.server.wifi.WifiLog.LogMessage;
import javax.annotation.concurrent.ThreadSafe;

@Immutable
@ThreadSafe
class LogcatLog implements WifiLog {
    private static volatile boolean sVerboseLogging = false;
    private final String mTag;

    private static class RealLogMessage implements LogMessage {
        private final String mFormat;
        private final int mLogLevel;
        private int mNextFormatCharPos = 0;
        private final StringBuilder mStringBuilder = new StringBuilder();
        private final String mTag;

        RealLogMessage(int logLevel, String tag, String format) {
            this.mLogLevel = logLevel;
            this.mTag = tag;
            this.mFormat = format;
        }

        public LogMessage r(String value) {
            return c(value);
        }

        public LogMessage c(String value) {
            copyUntilPlaceholder();
            if (this.mNextFormatCharPos < this.mFormat.length()) {
                this.mStringBuilder.append(value);
                this.mNextFormatCharPos++;
            }
            return this;
        }

        public LogMessage c(long value) {
            copyUntilPlaceholder();
            if (this.mNextFormatCharPos < this.mFormat.length()) {
                this.mStringBuilder.append(value);
                this.mNextFormatCharPos++;
            }
            return this;
        }

        public LogMessage c(char value) {
            copyUntilPlaceholder();
            if (this.mNextFormatCharPos < this.mFormat.length()) {
                this.mStringBuilder.append(value);
                this.mNextFormatCharPos++;
            }
            return this;
        }

        public LogMessage c(boolean value) {
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
            if (LogcatLog.sVerboseLogging || this.mLogLevel > 3) {
                Log.println(this.mLogLevel, this.mTag, this.mStringBuilder.toString());
            }
        }

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

    public LogMessage err(String format) {
        return makeLogMessage(6, format);
    }

    public LogMessage warn(String format) {
        return makeLogMessage(5, format);
    }

    public LogMessage info(String format) {
        return makeLogMessage(4, format);
    }

    public LogMessage trace(String format) {
        return makeLogMessage(3, format);
    }

    public LogMessage dump(String format) {
        return makeLogMessage(2, format);
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

    private LogMessage makeLogMessage(int logLevel, String format) {
        return new RealLogMessage(logLevel, this.mTag, format);
    }
}
