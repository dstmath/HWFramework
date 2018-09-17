package android.metrics;

import android.net.ConnectivityManager;
import android.util.EventLog;
import com.android.internal.logging.MetricsLogger;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.TimeUnit;

public class MetricsReader {
    private int[] LOGTAGS = new int[]{ConnectivityManager.CALLBACK_LOST};
    private int mCheckpointTag = -1;
    private Queue<LogMaker> mPendingQueue = new LinkedList();
    private LogReader mReader = new LogReader();
    private Queue<LogMaker> mSeenQueue = new LinkedList();

    public static class Event {
        Object mData;
        int mPid;
        long mTimeMillis;
        int mUid;

        public Event(long timeMillis, int pid, int uid, Object data) {
            this.mTimeMillis = timeMillis;
            this.mPid = pid;
            this.mUid = uid;
            this.mData = data;
        }

        Event(android.util.EventLog.Event nativeEvent) {
            this.mTimeMillis = TimeUnit.MILLISECONDS.convert(nativeEvent.getTimeNanos(), TimeUnit.NANOSECONDS);
            this.mPid = nativeEvent.getProcessId();
            this.mUid = nativeEvent.getUid();
            this.mData = nativeEvent.getData();
        }

        public long getTimeMillis() {
            return this.mTimeMillis;
        }

        public int getProcessId() {
            return this.mPid;
        }

        public int getUid() {
            return this.mUid;
        }

        public Object getData() {
            return this.mData;
        }

        public void setData(Object data) {
            this.mData = data;
        }
    }

    public static class LogReader {
        public void readEvents(int[] tags, long horizonMs, Collection<Event> events) throws IOException {
            ArrayList<android.util.EventLog.Event> nativeEvents = new ArrayList();
            EventLog.readEventsOnWrapping(tags, TimeUnit.NANOSECONDS.convert(horizonMs, TimeUnit.MILLISECONDS), nativeEvents);
            for (android.util.EventLog.Event nativeEvent : nativeEvents) {
                events.add(new Event(nativeEvent));
            }
        }

        public void writeCheckpoint(int tag) {
            new MetricsLogger().action(920, tag);
        }
    }

    public void setLogReader(LogReader reader) {
        this.mReader = reader;
    }

    public void read(long horizonMs) {
        ArrayList<Event> nativeEvents = new ArrayList();
        try {
            this.mReader.readEvents(this.LOGTAGS, horizonMs, nativeEvents);
        } catch (IOException e) {
            e.printStackTrace();
        }
        this.mPendingQueue.clear();
        this.mSeenQueue.clear();
        for (Event event : nativeEvents) {
            long eventTimestampMs = event.getTimeMillis();
            Object data = event.getData();
            LogMaker log = new LogMaker(data instanceof Object[] ? (Object[]) data : new Object[]{data}).setTimestamp(eventTimestampMs).setUid(event.getUid()).setProcessId(event.getProcessId());
            if (log.getCategory() != 920) {
                this.mPendingQueue.offer(log);
            } else if (log.getSubtype() == this.mCheckpointTag) {
                this.mPendingQueue.clear();
            }
        }
    }

    public void checkpoint() {
        this.mCheckpointTag = (int) (System.currentTimeMillis() % 2147483647L);
        this.mReader.writeCheckpoint(this.mCheckpointTag);
        this.mPendingQueue.clear();
        this.mSeenQueue.clear();
    }

    public void reset() {
        this.mSeenQueue.addAll(this.mPendingQueue);
        this.mPendingQueue.clear();
        this.mCheckpointTag = -1;
        Queue<LogMaker> tmp = this.mPendingQueue;
        this.mPendingQueue = this.mSeenQueue;
        this.mSeenQueue = tmp;
    }

    public boolean hasNext() {
        return this.mPendingQueue.isEmpty() ^ 1;
    }

    public LogMaker next() {
        LogMaker next = (LogMaker) this.mPendingQueue.poll();
        if (next != null) {
            this.mSeenQueue.offer(next);
        }
        return next;
    }
}
