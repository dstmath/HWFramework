package android.media;

import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class DefaultHwMediaMonitor implements IHwMediaMonitor {
    private static final String TAG = "DefaultHwMediaMonitor";
    private static DefaultHwMediaMonitor mHwMediaMonitor = null;

    public static DefaultHwMediaMonitor getDefault() {
        DefaultHwMediaMonitor defaultHwMediaMonitor;
        synchronized (DefaultHwMediaMonitor.class) {
            if (mHwMediaMonitor == null) {
                mHwMediaMonitor = new DefaultHwMediaMonitor();
            }
            defaultHwMediaMonitor = mHwMediaMonitor;
        }
        return defaultHwMediaMonitor;
    }

    @Override // android.media.IHwMediaMonitor
    public int writeLogMsg(int priority, int type, String msg) {
        return 0;
    }

    @Override // android.media.IHwMediaMonitor
    public int writeLogMsg(int eventId, int eventLevel, int subType, String reason) {
        return 0;
    }

    public int writeLogMsg(int eventId, int eventLevel, int subType, String reason, int paraInt, String paraChar, int associatedEeventID) {
        return 0;
    }

    @Override // android.media.IHwMediaMonitor
    public int writeBigData(int eventId, String subType) {
        return 0;
    }

    @Override // android.media.IHwMediaMonitor
    public int writeBigData(int eventId, String subType, int ext1, int ext2) {
        return 0;
    }

    @Override // android.media.IHwMediaMonitor
    public int writeBigData(int eventId, String subType, String sext1, int ext2) {
        return 0;
    }

    @Override // android.media.IHwMediaMonitor
    public int writeBigData(int eventId, String subType, String pkgName, String param) {
        return 0;
    }

    @Override // android.media.IHwMediaMonitor
    public int writeBigData(int eventId, String subType, String pkgName, String param, int streamType) {
        return 0;
    }

    @Override // android.media.IHwMediaMonitor
    public int writeBigData(int eventId, String pkgName, int source, int sampleRate, int btWidth, int channelCount) {
        return 0;
    }

    @Override // android.media.IHwMediaMonitor
    public int forceLogSend(int level) {
        return 0;
    }
}
