package android.media;

public interface IHwMediaMonitor {
    int forceLogSend(int i);

    int writeBigData(int i, String str);

    int writeBigData(int i, String str, int i2, int i3);

    int writeBigData(int i, String str, String str2, int i2);

    int writeKpis(String str);

    int writeLogMsg(int i, int i2, int i3, String str);

    int writeLogMsg(int i, int i2, String str);

    int writeMediaBigData(int i, int i2, String str);

    void writeMediaBigDataByReportInf(int i, int i2, String str);
}
