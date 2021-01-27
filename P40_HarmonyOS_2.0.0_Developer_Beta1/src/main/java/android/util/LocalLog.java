package android.util;

import android.annotation.UnsupportedAppUsage;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

public final class LocalLog {
    private final Deque<String> mLog = new ArrayDeque(this.mMaxLines);
    private final int mMaxLines;

    @UnsupportedAppUsage
    public LocalLog(int maxLines) {
        this.mMaxLines = Math.max(0, maxLines);
    }

    @UnsupportedAppUsage
    public void log(String msg) {
        if (this.mMaxLines > 0) {
            append(String.format("%s - %s", LocalDateTime.now(), msg));
        }
    }

    private synchronized void append(String logLine) {
        while (this.mLog.size() >= this.mMaxLines) {
            this.mLog.remove();
        }
        this.mLog.add(logLine);
    }

    @UnsupportedAppUsage
    public synchronized void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        dump(pw);
    }

    public synchronized void dump(PrintWriter pw) {
        for (String str : this.mLog) {
            pw.println(str);
        }
    }

    public synchronized void reverseDump(FileDescriptor fd, PrintWriter pw, String[] args) {
        reverseDump(pw);
    }

    public synchronized void reverseDump(PrintWriter pw) {
        Iterator<String> itr = this.mLog.descendingIterator();
        while (itr.hasNext()) {
            pw.println(itr.next());
        }
    }

    public static class ReadOnlyLocalLog {
        private final LocalLog mLog;

        ReadOnlyLocalLog(LocalLog log) {
            this.mLog = log;
        }

        @UnsupportedAppUsage
        public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
            this.mLog.dump(pw);
        }

        public void dump(PrintWriter pw) {
            this.mLog.dump(pw);
        }

        public void reverseDump(FileDescriptor fd, PrintWriter pw, String[] args) {
            this.mLog.reverseDump(pw);
        }

        public void reverseDump(PrintWriter pw) {
            this.mLog.reverseDump(pw);
        }
    }

    @UnsupportedAppUsage
    public ReadOnlyLocalLog readOnlyLocalLog() {
        return new ReadOnlyLocalLog(this);
    }
}
