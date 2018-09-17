package android.media;

public class HwMediaMonitorDummy implements IHwMediaMonitor {
    private static final String TAG = "HwMediaMonitorDummy";
    private static IHwMediaMonitor mHwMediaMonitor = null;

    public static IHwMediaMonitor getDefault() {
        IHwMediaMonitor iHwMediaMonitor;
        synchronized (HwMediaMonitorDummy.class) {
            if (mHwMediaMonitor == null) {
                mHwMediaMonitor = new HwMediaMonitorDummy();
            }
            iHwMediaMonitor = mHwMediaMonitor;
        }
        return iHwMediaMonitor;
    }

    public int writeLogMsg(int priority, int type, String msg) {
        return 0;
    }

    public int writeMediaBigData(int pid, int type, String msg) {
        return 0;
    }

    public void writeMediaBigDataByReportInf(int pid, int type, String msg) {
    }

    public int writeLogMsg(int eventId, int eventLevel, int subType, String reason) {
        return 0;
    }

    public int writeLogMsg(int eventId, int eventLevel, int subType, String reason, int paraInt, String paraChar, int associatedEeventID) {
        return 0;
    }

    public int writeKpis(String kpis) {
        return -1;
    }

    public int writeBigData(int eventId, int subType) {
        return 0;
    }

    public int writeBigData(int eventId, int subType, int ext1, int ext2) {
        return 0;
    }

    public int forceLogSend(int level) {
        return 0;
    }
}
