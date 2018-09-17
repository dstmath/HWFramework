package android.webkit;

import android.util.EventLog;

public class EventLogTags {
    public static final int BROWSER_DOUBLE_TAP_DURATION = 70102;
    public static final int BROWSER_SNAP_CENTER = 70150;
    public static final int BROWSER_ZOOM_LEVEL_CHANGE = 70101;
    public static final int EXP_DET_ATTEMPT_TO_CALL_OBJECT_GETCLASS = 70151;

    private EventLogTags() {
    }

    public static void writeBrowserZoomLevelChange(int startLevel, int endLevel, long time) {
        EventLog.writeEvent((int) BROWSER_ZOOM_LEVEL_CHANGE, Integer.valueOf(startLevel), Integer.valueOf(endLevel), Long.valueOf(time));
    }

    public static void writeBrowserDoubleTapDuration(int duration, long time) {
        EventLog.writeEvent((int) BROWSER_DOUBLE_TAP_DURATION, Integer.valueOf(duration), Long.valueOf(time));
    }

    public static void writeBrowserSnapCenter() {
        EventLog.writeEvent((int) BROWSER_SNAP_CENTER, new Object[0]);
    }

    public static void writeExpDetAttemptToCallObjectGetclass(String appSignature) {
        EventLog.writeEvent((int) EXP_DET_ATTEMPT_TO_CALL_OBJECT_GETCLASS, appSignature);
    }
}
