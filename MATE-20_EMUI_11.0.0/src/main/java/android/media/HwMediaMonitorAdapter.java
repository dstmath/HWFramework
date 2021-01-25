package android.media;

public class HwMediaMonitorAdapter {
    private static native int checkAudioFlinger();

    private static native int forceLogSendNative(int i);

    private static native int systemReady();

    private static native int writeBigDataNative(int i, String str);

    private static native int writeBigDataNative(int i, String str, int i2, int i3);

    private static native int writeBigDataNative(int i, String str, int i2, int i3, int i4, int i5);

    private static native int writeBigDataNative(int i, String str, String str2, int i2);

    private static native int writeBigDataNative(int i, String str, String str2, String str3);

    private static native int writeBigDataNative(int i, String str, String str2, String str3, int i2);

    private static native int writeKpisNative(String str);

    private static native int writeLogMsgNative(int i, int i2, int i3, String str);

    private static native int writeLogMsgNative(int i, int i2, String str);

    private static native int writeMediaBigDataNative(int i, int i2, String str);

    static {
        System.loadLibrary("mediamonitor_jni");
    }

    public static int writeLogMsg(int priority, int type, String msg) {
        return writeLogMsgNative(priority, type, msg);
    }

    public static int writeLogMsg(int eventId, int eventLevel, int subType, String reason) {
        return writeLogMsgNative(eventId, eventLevel, subType, reason);
    }

    public static int writeKpis(String kpis) {
        return writeKpisNative(kpis);
    }

    public static int writeBigData(int eventId, String subType) {
        return writeBigDataNative(eventId, subType);
    }

    public static int writeBigData(int eventId, String subType, int ext1, int ext2) {
        return writeBigDataNative(eventId, subType, ext1, ext2);
    }

    public static int writeBigData(int eventId, String subType, String sext1, int ext2) {
        return writeBigDataNative(eventId, subType, sext1, ext2);
    }

    public static int writeBigData(int eventId, String subType, String pkgName, String param) {
        return writeBigDataNative(eventId, subType, pkgName, param);
    }

    public static int writeBigData(int eventId, String subType, String pkgName, String param, int streamType) {
        return writeBigDataNative(eventId, subType, pkgName, param, streamType);
    }

    public static int writeBigData(int eventId, String pkgName, int source, int sampleRate, int btWidth, int channelCount) {
        return writeBigDataNative(eventId, pkgName, source, sampleRate, btWidth, channelCount);
    }

    public static int writeMediaBigData(int pid, int type, String msg) {
        return writeMediaBigDataNative(pid, type, msg);
    }

    public static int forceLogSend(int level) {
        return forceLogSendNative(level);
    }

    public static int checkAudioFlingerAdapter() {
        return checkAudioFlinger();
    }

    public static int systemReadyAdapter() {
        return systemReady();
    }
}
