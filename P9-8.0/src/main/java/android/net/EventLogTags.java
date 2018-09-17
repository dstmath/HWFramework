package android.net;

import android.util.EventLog;

public class EventLogTags {
    public static final int NTP_FAILURE = 50081;
    public static final int NTP_SUCCESS = 50080;

    private EventLogTags() {
    }

    public static void writeNtpSuccess(String server, long rtt, long offset) {
        EventLog.writeEvent(NTP_SUCCESS, new Object[]{server, Long.valueOf(rtt), Long.valueOf(offset)});
    }

    public static void writeNtpFailure(String server, String msg) {
        EventLog.writeEvent(NTP_FAILURE, new Object[]{server, msg});
    }
}
