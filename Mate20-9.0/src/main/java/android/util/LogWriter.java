package android.util;

import java.io.Writer;

public class LogWriter extends Writer {
    private final int mBuffer;
    private StringBuilder mBuilder = new StringBuilder(128);
    private final int mPriority;
    private final String mTag;

    public LogWriter(int priority, String tag) {
        this.mPriority = priority;
        this.mTag = tag;
        this.mBuffer = 0;
    }

    public LogWriter(int priority, String tag, int buffer) {
        this.mPriority = priority;
        this.mTag = tag;
        this.mBuffer = buffer;
    }

    public void close() {
        flushBuilder();
    }

    public void flush() {
        flushBuilder();
    }

    public void write(char[] buf, int offset, int count) {
        for (int i = 0; i < count; i++) {
            char c = buf[offset + i];
            if (c == 10) {
                flushBuilder();
            } else {
                this.mBuilder.append(c);
            }
        }
    }

    private void flushBuilder() {
        if (this.mBuilder.length() > 0) {
            Log.println_native(this.mBuffer, this.mPriority, this.mTag, this.mBuilder.toString());
            this.mBuilder.delete(0, this.mBuilder.length());
        }
    }
}
