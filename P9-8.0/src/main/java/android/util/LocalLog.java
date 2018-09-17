package android.util;

import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.ArrayDeque;
import java.util.Calendar;
import java.util.Deque;
import java.util.Iterator;

public final class LocalLog {
    private final Deque<String> mLog = new ArrayDeque(this.mMaxLines);
    private final int mMaxLines;

    public static class ReadOnlyLocalLog {
        private final LocalLog mLog;

        ReadOnlyLocalLog(LocalLog log) {
            this.mLog = log;
        }

        public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
            this.mLog.dump(fd, pw, args);
        }

        public void reverseDump(FileDescriptor fd, PrintWriter pw, String[] args) {
            this.mLog.reverseDump(fd, pw, args);
        }
    }

    public LocalLog(int maxLines) {
        this.mMaxLines = Math.max(0, maxLines);
    }

    public void log(String msg) {
        if (this.mMaxLines > 0) {
            Calendar.getInstance().setTimeInMillis(System.currentTimeMillis());
            append(String.format("%tm-%td %tH:%tM:%tS.%tL - %s", new Object[]{c, c, c, c, c, c, msg}));
        }
    }

    private synchronized void append(String logLine) {
        while (this.mLog.size() >= this.mMaxLines) {
            this.mLog.remove();
        }
        this.mLog.add(logLine);
    }

    public synchronized void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        for (String println : this.mLog) {
            pw.println(println);
        }
    }

    public synchronized void reverseDump(FileDescriptor fd, PrintWriter pw, String[] args) {
        Iterator<String> itr = this.mLog.descendingIterator();
        while (itr.hasNext()) {
            pw.println((String) itr.next());
        }
    }

    public ReadOnlyLocalLog readOnlyLocalLog() {
        return new ReadOnlyLocalLog(this);
    }
}
