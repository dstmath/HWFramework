package android.media;

import android.common.HwFrameworkFactory;

public class HwMediaMonitorManager {
    private static final String TAG = "HwMediaMonitorManager";
    private static int prePid = -1;
    private static int preType = HwMediaMonitorUtils.TYPE_MEDIA_RECORD_STREAM_COUNT;

    public static int writeLogMsg(int priority, int type, String msg) {
        return HwFrameworkFactory.getHwMediaMonitor().writeLogMsg(priority, type, msg);
    }

    public static int writeMediaBigData(int pid, int type, String msg) {
        if (HwMediaMonitorUtils.isMediaBigDataWritedNative(type)) {
            return HwFrameworkFactory.getHwMediaMonitor().writeMediaBigData(pid, type, msg);
        }
        if (pid == prePid && type == preType) {
            return 0;
        }
        HwFrameworkFactory.getHwMediaMonitor().writeMediaBigDataByReportInf(pid, type, msg);
        preType = type;
        prePid = pid;
        return 0;
    }

    public static int writeLogMsg(int eventId, int eventLevel, int subType, String reason) {
        return HwFrameworkFactory.getHwMediaMonitor().writeLogMsg(eventId, eventLevel, subType, reason);
    }

    public static int writeLogMsg(int eventId, int eventLevel, int subType, String reason, int paraInt, String paraChar, int associatedEeventID) {
        return HwFrameworkFactory.getHwMediaMonitor().writeLogMsg(eventId, eventLevel, subType, reason, paraInt, paraChar, associatedEeventID);
    }

    public static int writeKpis(String kpis) {
        return HwFrameworkFactory.getHwMediaMonitor().writeKpis(kpis);
    }

    public static int writeBigData(int eventId, int subType) {
        return HwFrameworkFactory.getHwMediaMonitor().writeBigData(eventId, subType);
    }

    public static int writeBigData(int eventId, int subType, int ext1, int ext2) {
        return HwFrameworkFactory.getHwMediaMonitor().writeBigData(eventId, subType, ext1, ext2);
    }

    public static int forceLogSend(int level) {
        return HwFrameworkFactory.getHwMediaMonitor().forceLogSend(level);
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
            case 8:
                return 20409;
            default:
                return 10000;
        }
    }
}
