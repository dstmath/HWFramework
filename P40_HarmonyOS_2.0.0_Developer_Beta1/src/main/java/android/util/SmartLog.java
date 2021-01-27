package android.util;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import java.io.FileDescriptor;
import java.io.PrintWriter;
import java.util.Calendar;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Locale;

public final class SmartLog {
    private static final Singleton<SmartLog> DEFAULT = new Singleton<SmartLog>() {
        /* class android.util.SmartLog.AnonymousClass1 */

        /* access modifiers changed from: protected */
        public SmartLog create() {
            return new SmartLog();
        }
    };
    private static final int MAX_CACHE_LINES = 20;
    private static final int MAX_LOG_LINES = 1000;
    private static final int MAX_MESSAGE_ID = 100000;
    private static final String TAG = "SmartLog";
    private LinkedList<String> mCacheLogList;
    private Handler mHandler;
    private LinkedList<String> mLogList;
    private int mMessageId;

    private SmartLog() {
        this.mMessageId = -1;
        this.mLogList = new LinkedList<>();
        this.mCacheLogList = new LinkedList<>();
        HandlerThread handlerThread = new HandlerThread(TAG);
        handlerThread.start();
        this.mHandler = new LogHandler(handlerThread.getLooper());
    }

    public static SmartLog getInstance() {
        return (SmartLog) DEFAULT.get();
    }

    public int startRecord(String log, int waitMillis) {
        int messageId = getMessageId();
        String msgLog = getLog(messageId, log);
        Message msg = Message.obtain();
        msg.what = messageId;
        msg.obj = msgLog;
        addToCache("cache:" + msgLog);
        this.mHandler.sendMessageDelayed(msg, (long) waitMillis);
        return messageId;
    }

    public void endRecord(int messageId, String log) {
        String msgLog = getLog(messageId, log);
        if (this.mHandler.hasMessages(messageId)) {
            this.mHandler.removeMessages(messageId);
            addToCache("cache:" + msgLog);
            return;
        }
        addToLog("timeout:" + msgLog);
    }

    public synchronized void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        Iterator<String> itr = this.mLogList.listIterator(0);
        while (itr.hasNext()) {
            pw.println(itr.next());
        }
    }

    public synchronized void reverseDump(FileDescriptor fd, PrintWriter pw, String[] args) {
        for (int i = this.mLogList.size() - 1; i >= 0; i--) {
            pw.println(this.mLogList.get(i));
        }
    }

    public static class ReadOnlyLocalLog {
        private final SmartLog smartLog;

        ReadOnlyLocalLog(SmartLog log) {
            this.smartLog = log;
        }

        public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
            this.smartLog.dump(fd, pw, args);
        }
    }

    public ReadOnlyLocalLog readOnlyLocalLog() {
        return new ReadOnlyLocalLog(this);
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private synchronized void addToLog(String msg) {
        this.mLogList.add(msg);
        while (this.mLogList.size() > 1000) {
            Log.e(TAG, this.mLogList.remove());
        }
    }

    private synchronized void addToCache(String msg) {
        this.mCacheLogList.add(msg);
        while (this.mCacheLogList.size() > 20) {
            this.mCacheLogList.remove();
        }
    }

    /* access modifiers changed from: private */
    /* access modifiers changed from: public */
    private synchronized void moveCacheToLog() {
        while (this.mCacheLogList.size() > 0) {
            addToLog(this.mCacheLogList.remove());
        }
    }

    private synchronized String getLog(int msgId, String msg) {
        StringBuilder sb;
        long now = System.currentTimeMillis();
        sb = new StringBuilder();
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(now);
        sb.append(String.format(Locale.ROOT, "%tm-%td %tH:%tM:%tS.%tL", calendar, calendar, calendar, calendar, calendar, calendar));
        return msgId + ":" + sb.toString() + msg;
    }

    private synchronized int getMessageId() {
        int i;
        i = this.mMessageId + 1;
        this.mMessageId = i;
        return i / MAX_MESSAGE_ID;
    }

    private class LogHandler extends Handler {
        LogHandler(Looper looper) {
            super(looper);
        }

        @Override // android.os.Handler
        public void handleMessage(Message msg) {
            SmartLog.this.moveCacheToLog();
            SmartLog smartLog = SmartLog.this;
            smartLog.addToLog("timeout(>1s):" + String.valueOf(msg.obj));
        }
    }
}
