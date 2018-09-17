package android.media;

public interface IHwMediaMonitor {
    int forceLogSend(int i);

    int writeLogMsg(int i, int i2, String str);

    int writeMediaBigData(int i, int i2, String str);

    void writeMediaBigDataByReportInf(int i, int i2, String str);
}
