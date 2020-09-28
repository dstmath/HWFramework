package android.media;

import android.app.ActivityThread;
import android.util.Log;

public class HwMediaMonitorManager {
    private static final String TAG = "HwMediaMonitorManager";
    private static int prePid = -1;
    private static int preType = HwMediaMonitorUtils.TYPE_MEDIA_RECORD_STREAM_COUNT;

    public static int writeLogMsg(int priority, int type, String msg) {
        return HwMediaFactory.getHwMediaMonitor().writeLogMsg(priority, type, msg);
    }

    public static int writeLogMsg(int eventId, int eventLevel, int subType, String reason) {
        return HwMediaFactory.getHwMediaMonitor().writeLogMsg(eventId, eventLevel, subType, reason);
    }

    public static int writeBigData(int eventId, String subType) {
        return HwMediaFactory.getHwMediaMonitor().writeBigData(eventId, subType);
    }

    public static int writeBigData(int eventId, String subType, int ext1, int ext2) {
        return HwMediaFactory.getHwMediaMonitor().writeBigData(eventId, subType, ext1, ext2);
    }

    public static int writeBigData(int eventId, String subType, String sext, int ext2) {
        return HwMediaFactory.getHwMediaMonitor().writeBigData(eventId, subType, sext, ext2);
    }

    public static int writeBigData(int eventId, String subType, String pkgName, String param) {
        IHwMediaMonitor hwHwMediaMonitor = HwMediaFactory.getHwMediaMonitor();
        if (pkgName == null) {
            pkgName = "none";
            Log.w(TAG, "could not get pkgName for big data");
        }
        return hwHwMediaMonitor.writeBigData(eventId, subType, pkgName, param);
    }

    public static int writeBigData(int eventId, String subType, String pkgName, String param, int type) {
        IHwMediaMonitor hwHwMediaMonitor = HwMediaFactory.getHwMediaMonitor();
        if (pkgName == null) {
            pkgName = "none";
            Log.w(TAG, "could not get pkgName for big data");
        }
        return hwHwMediaMonitor.writeBigData(eventId, subType, pkgName, param, type);
    }

    public static int forceLogSend(int level) {
        return HwMediaFactory.getHwMediaMonitor().forceLogSend(level);
    }

    public static int getStreamBigDataType(int streamType) {
        switch (streamType) {
            case 0:
                return HwMediaMonitorUtils.TYPE_MEDIA_RECORD_VOICE_CALL_COUNT;
            case 1:
                return HwMediaMonitorUtils.TYPE_MEDIA_RECORD_SYSTEM_COUNT;
            case 2:
                return HwMediaMonitorUtils.TYPE_MEDIA_RECORD_RING_COUNT;
            case 3:
                return HwMediaMonitorUtils.TYPE_MEDIA_RECORD_MUSIC_COUNT;
            case 4:
                return HwMediaMonitorUtils.TYPE_MEDIA_RECORD_ALARM_COUNT;
            case 5:
                return HwMediaMonitorUtils.TYPE_MEDIA_RECORD_NOTIFICATION_COUNT;
            case 6:
                return HwMediaMonitorUtils.TYPE_MEDIA_RECORD_STREAM_BLUETOOTH_SCO_COUNT;
            case 7:
            default:
                return 10000;
            case 8:
                return 20409;
        }
    }

    public static void readyForWriteBigData(int eventId, String subType, String param) {
        String pkgName = "none";
        if (ActivityThread.currentApplication() != null) {
            pkgName = ActivityThread.currentApplication().getPackageName();
        }
        writeBigData(eventId, subType, pkgName, param);
    }

    public static void readyForWriteBigData(int eventId, String subType, String param, int type) {
        String pkgName = "none";
        if (ActivityThread.currentApplication() != null) {
            pkgName = ActivityThread.currentApplication().getPackageName();
        }
        writeBigData(eventId, subType, pkgName, param, type);
    }
}
