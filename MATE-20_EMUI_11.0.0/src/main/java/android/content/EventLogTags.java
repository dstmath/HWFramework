package android.content;

import android.util.EventLog;

public class EventLogTags {
    public static final int BINDER_SAMPLE = 52004;
    public static final int CONTENT_QUERY_SAMPLE = 52002;
    public static final int CONTENT_UPDATE_SAMPLE = 52003;

    private EventLogTags() {
    }

    public static void writeContentQuerySample(String uri, String projection, String selection, String sortorder, int time, String blockingPackage, int samplePercent) {
        EventLog.writeEvent((int) CONTENT_QUERY_SAMPLE, uri, projection, selection, sortorder, Integer.valueOf(time), blockingPackage, Integer.valueOf(samplePercent));
    }

    public static void writeContentUpdateSample(String uri, String operation, String selection, int time, String blockingPackage, int samplePercent) {
        EventLog.writeEvent((int) CONTENT_UPDATE_SAMPLE, uri, operation, selection, Integer.valueOf(time), blockingPackage, Integer.valueOf(samplePercent));
    }

    public static void writeBinderSample(String descriptor, int methodNum, int time, String blockingPackage, int samplePercent) {
        EventLog.writeEvent((int) BINDER_SAMPLE, descriptor, Integer.valueOf(methodNum), Integer.valueOf(time), blockingPackage, Integer.valueOf(samplePercent));
    }
}
