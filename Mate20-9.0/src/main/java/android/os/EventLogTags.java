package android.os;

import android.util.EventLog;

public class EventLogTags {
    public static final int SERVICE_MANAGER_SLOW = 230001;
    public static final int SERVICE_MANAGER_STATS = 230000;

    private EventLogTags() {
    }

    public static void writeServiceManagerStats(int callCount, int totalTime, int duration) {
        EventLog.writeEvent((int) SERVICE_MANAGER_STATS, Integer.valueOf(callCount), Integer.valueOf(totalTime), Integer.valueOf(duration));
    }

    public static void writeServiceManagerSlow(int time, String service) {
        EventLog.writeEvent((int) SERVICE_MANAGER_SLOW, Integer.valueOf(time), service);
    }
}
