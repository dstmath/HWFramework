package android.content;

import android.util.EventLog;

public class EventLogTags {
    public static final int BINDER_SAMPLE = 52004;
    public static final int CONTENT_QUERY_SAMPLE = 52002;
    public static final int CONTENT_UPDATE_SAMPLE = 52003;

    private EventLogTags() {
    }

    public static void writeContentQuerySample(String uri, String projection, String selection, String sortorder, int time, String blockingPackage, int samplePercent) {
        EventLog.writeEvent(CONTENT_QUERY_SAMPLE, new Object[]{uri, projection, selection, sortorder, Integer.valueOf(time), blockingPackage, Integer.valueOf(samplePercent)});
    }

    public static void writeContentUpdateSample(String uri, String operation, String selection, int time, String blockingPackage, int samplePercent) {
        EventLog.writeEvent(CONTENT_UPDATE_SAMPLE, new Object[]{uri, operation, selection, Integer.valueOf(time), blockingPackage, Integer.valueOf(samplePercent)});
    }

    public static void writeBinderSample(String descriptor, int methodNum, int time, String blockingPackage, int samplePercent) {
        EventLog.writeEvent(BINDER_SAMPLE, new Object[]{descriptor, Integer.valueOf(methodNum), Integer.valueOf(time), blockingPackage, Integer.valueOf(samplePercent)});
    }
}
