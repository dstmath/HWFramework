package android.telecom.Logging;

import android.provider.SettingsStringUtil;
import android.telecom.Log;
import android.telecom.Logging.SessionManager.ISessionIdQueryHandler;
import android.text.TextUtils;
import com.android.internal.util.IndentingPrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

public class EventManager {
    public static final int DEFAULT_EVENTS_TO_CACHE = 10;
    public static final String TAG = "Logging.Events";
    private static final Object mSync = new Object();
    private final Map<Loggable, EventRecord> mCallEventRecordMap = new HashMap();
    private List<EventListener> mEventListeners = new ArrayList();
    private LinkedBlockingQueue<EventRecord> mEventRecords = new LinkedBlockingQueue(10);
    private ISessionIdQueryHandler mSessionIdHandler;
    private final Map<String, List<TimedEventPair>> requestResponsePairs = new HashMap();

    public static class Event {
        public Object data;
        public String eventId;
        public String sessionId;
        public long time;

        public Event(String eventId, String sessionId, long time, Object data) {
            this.eventId = eventId;
            this.sessionId = sessionId;
            this.time = time;
            this.data = data;
        }
    }

    public interface EventListener {
        void eventRecordAdded(EventRecord eventRecord);
    }

    public class EventRecord {
        private final List<Event> mEvents = new LinkedList();
        private final Loggable mRecordEntry;
        private final DateFormat sDateFormat = new SimpleDateFormat("HH:mm:ss.SSS");

        public class EventTiming extends TimedEvent<String> {
            public String name;
            public long time;

            public EventTiming(String name, long time) {
                this.name = name;
                this.time = time;
            }

            public String getKey() {
                return this.name;
            }

            public long getTime() {
                return this.time;
            }
        }

        private class PendingResponse {
            String name;
            String requestEventId;
            long requestEventTimeMillis;
            long timeoutMillis;

            public PendingResponse(String requestEventId, long requestEventTimeMillis, long timeoutMillis, String name) {
                this.requestEventId = requestEventId;
                this.requestEventTimeMillis = requestEventTimeMillis;
                this.timeoutMillis = timeoutMillis;
                this.name = name;
            }
        }

        public EventRecord(Loggable recordEntry) {
            this.mRecordEntry = recordEntry;
        }

        public Loggable getRecordEntry() {
            return this.mRecordEntry;
        }

        public void addEvent(String event, String sessionId, Object data) {
            this.mEvents.add(new Event(event, sessionId, System.currentTimeMillis(), data));
            Log.i("Event", "RecordEntry %s: %s, %s", this.mRecordEntry.getId(), event, data);
        }

        public List<Event> getEvents() {
            return this.mEvents;
        }

        public List<EventTiming> extractEventTimings() {
            if (this.mEvents == null) {
                return Collections.emptyList();
            }
            LinkedList<EventTiming> result = new LinkedList();
            Map<String, PendingResponse> pendingResponses = new HashMap();
            for (Event event : this.mEvents) {
                if (EventManager.this.requestResponsePairs.containsKey(event.eventId)) {
                    for (TimedEventPair p : (List) EventManager.this.requestResponsePairs.get(event.eventId)) {
                        Map<String, PendingResponse> map = pendingResponses;
                        String str = p.mResponse;
                        map.put(str, new PendingResponse(event.eventId, event.time, p.mTimeoutMillis, p.mName));
                    }
                }
                PendingResponse pendingResponse = (PendingResponse) pendingResponses.remove(event.eventId);
                if (pendingResponse != null) {
                    long elapsedTime = event.time - pendingResponse.requestEventTimeMillis;
                    if (elapsedTime < pendingResponse.timeoutMillis) {
                        result.add(new EventTiming(pendingResponse.name, elapsedTime));
                    }
                }
            }
            return result;
        }

        public void dump(IndentingPrintWriter pw) {
            pw.print(this.mRecordEntry.getDescription());
            pw.increaseIndent();
            for (Event event : this.mEvents) {
                pw.print(this.sDateFormat.format(new Date(event.time)));
                pw.print(" - ");
                pw.print(event.eventId);
                if (event.data != null) {
                    pw.print(" (");
                    Object data = event.data;
                    if (data instanceof Loggable) {
                        EventRecord record = (EventRecord) EventManager.this.mCallEventRecordMap.get(data);
                        if (record != null) {
                            data = "RecordEntry " + record.mRecordEntry.getId();
                        }
                    }
                    pw.print(data);
                    pw.print(")");
                }
                if (!TextUtils.isEmpty(event.sessionId)) {
                    pw.print(SettingsStringUtil.DELIMITER);
                    pw.print(event.sessionId);
                }
                pw.println();
            }
            pw.println("Timings (average for this call, milliseconds):");
            pw.increaseIndent();
            List<String> eventNames = new ArrayList(TimedEvent.averageTimings(extractEventTimings()).keySet());
            Collections.sort(eventNames);
            for (String eventName : eventNames) {
                pw.printf("%s: %.2f\n", new Object[]{eventName, avgEventTimings.get(eventName)});
            }
            pw.decreaseIndent();
            pw.decreaseIndent();
        }
    }

    public interface Loggable {
        String getDescription();

        String getId();
    }

    public static class TimedEventPair {
        private static final long DEFAULT_TIMEOUT = 3000;
        String mName;
        String mRequest;
        String mResponse;
        long mTimeoutMillis = DEFAULT_TIMEOUT;

        public TimedEventPair(String request, String response, String name) {
            this.mRequest = request;
            this.mResponse = response;
            this.mName = name;
        }

        public TimedEventPair(String request, String response, String name, long timeoutMillis) {
            this.mRequest = request;
            this.mResponse = response;
            this.mName = name;
            this.mTimeoutMillis = timeoutMillis;
        }
    }

    public void addRequestResponsePair(TimedEventPair p) {
        if (this.requestResponsePairs.containsKey(p.mRequest)) {
            ((List) this.requestResponsePairs.get(p.mRequest)).add(p);
            return;
        }
        ArrayList<TimedEventPair> responses = new ArrayList();
        responses.add(p);
        this.requestResponsePairs.put(p.mRequest, responses);
    }

    public EventManager(ISessionIdQueryHandler l) {
        this.mSessionIdHandler = l;
    }

    public void event(Loggable recordEntry, String event, Object data) {
        String currentSessionID = this.mSessionIdHandler.getSessionId();
        if (recordEntry == null) {
            Log.i(TAG, "Non-call EVENT: %s, %s", event, data);
            return;
        }
        synchronized (this.mEventRecords) {
            if (!this.mCallEventRecordMap.containsKey(recordEntry)) {
                addEventRecord(new EventRecord(recordEntry));
            }
            ((EventRecord) this.mCallEventRecordMap.get(recordEntry)).addEvent(event, currentSessionID, data);
        }
    }

    public void event(Loggable recordEntry, String event, String format, Object... args) {
        String msg;
        if (args != null) {
            try {
                if (args.length != 0) {
                    msg = String.format(Locale.US, format, args);
                    event(recordEntry, event, msg);
                }
            } catch (Throwable ife) {
                Log.e((Object) this, ife, "IllegalFormatException: formatString='%s' numArgs=%d", format, Integer.valueOf(args.length));
                msg = format + " (An error occurred while formatting the message.)";
            }
        }
        msg = format;
        event(recordEntry, event, msg);
    }

    public void dumpEvents(IndentingPrintWriter pw) {
        pw.println("Historical Events:");
        pw.increaseIndent();
        for (EventRecord eventRecord : this.mEventRecords) {
            eventRecord.dump(pw);
        }
        pw.decreaseIndent();
    }

    public void changeEventCacheSize(int newSize) {
        LinkedBlockingQueue<EventRecord> oldEventLog = this.mEventRecords;
        this.mEventRecords = new LinkedBlockingQueue(newSize);
        this.mCallEventRecordMap.clear();
        oldEventLog.forEach(new -$Lambda$Bho-6fQ_lBTm8N3FcbHLVOfu_sY(this));
    }

    /* synthetic */ void lambda$-android_telecom_Logging_EventManager_11378(EventRecord newRecord) {
        Loggable recordEntry = newRecord.getRecordEntry();
        if (this.mEventRecords.remainingCapacity() == 0) {
            EventRecord record = (EventRecord) this.mEventRecords.poll();
            if (record != null) {
                this.mCallEventRecordMap.remove(record.getRecordEntry());
            }
        }
        this.mEventRecords.add(newRecord);
        this.mCallEventRecordMap.put(recordEntry, newRecord);
    }

    public void registerEventListener(EventListener e) {
        if (e != null) {
            synchronized (mSync) {
                this.mEventListeners.add(e);
            }
        }
    }

    public LinkedBlockingQueue<EventRecord> getEventRecords() {
        return this.mEventRecords;
    }

    public Map<Loggable, EventRecord> getCallEventRecordMap() {
        return this.mCallEventRecordMap;
    }

    private void addEventRecord(EventRecord newRecord) {
        Loggable recordEntry = newRecord.getRecordEntry();
        if (this.mEventRecords.remainingCapacity() == 0) {
            EventRecord record = (EventRecord) this.mEventRecords.poll();
            if (record != null) {
                this.mCallEventRecordMap.remove(record.getRecordEntry());
            }
        }
        this.mEventRecords.add(newRecord);
        this.mCallEventRecordMap.put(recordEntry, newRecord);
        synchronized (mSync) {
            for (EventListener l : this.mEventListeners) {
                l.eventRecordAdded(newRecord);
            }
        }
    }
}
