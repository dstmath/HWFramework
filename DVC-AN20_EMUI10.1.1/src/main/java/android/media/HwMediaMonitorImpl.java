package android.media;

public class HwMediaMonitorImpl extends DefaultHwMediaMonitor {
    public static final int STATUS_ERROR = 1;
    public static final int STATUS_OK = 0;
    public static final int STATUS_SERVER_DIED = 100;
    private static final String TAG = "HwMediaMonitorImpl";
    private static HwMediaMonitorImpl sHwMediaMonitor = null;
    private ErrorCallback mErrorCallback;

    public interface ErrorCallback {
        void onError(int i);
    }

    private HwMediaMonitorImpl() {
    }

    public static HwMediaMonitorImpl getDefault() {
        HwMediaMonitorImpl hwMediaMonitorImpl;
        synchronized (HwMediaMonitorImpl.class) {
            if (sHwMediaMonitor == null) {
                sHwMediaMonitor = new HwMediaMonitorImpl();
            }
            hwMediaMonitorImpl = sHwMediaMonitor;
        }
        return hwMediaMonitorImpl;
    }

    public int writeLogMsg(int priority, int type, String msg) {
        return HwMediaMonitorAdapter.writeLogMsg(priority, type, msg);
    }

    public int writeLogMsg(int eventId, int eventLevel, int subType, String reason) {
        return HwMediaMonitorAdapter.writeLogMsg(eventId, eventLevel, subType, reason);
    }

    public int writeBigData(int eventId, String subType) {
        return HwMediaMonitorAdapter.writeBigData(eventId, subType);
    }

    public int writeBigData(int eventId, String subType, int ext1, int ext2) {
        return HwMediaMonitorAdapter.writeBigData(eventId, subType, ext1, ext2);
    }

    public int writeBigData(int eventId, String subType, String sext1, int ext2) {
        return 0;
    }

    public int writeBigData(int eventId, String subType, String pkgName, String param) {
        return HwMediaMonitorAdapter.writeBigData(eventId, subType, pkgName, param);
    }

    public int writeBigData(int eventId, String subType, String pkgName, String param, int streamType) {
        return HwMediaMonitorAdapter.writeBigData(eventId, subType, pkgName, param, streamType);
    }

    public int forceLogSend(int level) {
        return HwMediaMonitorAdapter.forceLogSend(level);
    }

    private boolean checkMediaLogPermission() {
        return true;
    }

    public void setErrorCallback(ErrorCallback cb) {
        synchronized (HwMediaMonitorImpl.class) {
            this.mErrorCallback = cb;
            if (cb != null) {
                cb.onError(HwMediaMonitorAdapter.checkAudioFlingerAdapter());
            }
        }
    }

    private void errorCallbackFromNative(int error) {
        ErrorCallback errorCallback = null;
        synchronized (HwMediaMonitorImpl.class) {
            if (this.mErrorCallback != null) {
                errorCallback = this.mErrorCallback;
            }
        }
        if (errorCallback != null) {
            errorCallback.onError(error);
        }
    }
}
