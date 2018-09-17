package android.media;

public interface IHwMediaMonitor {
    int forceLogSend(int i);

    int writeBigData(int i, int i2);

    int writeBigData(int i, int i2, int i3, int i4);

    int writeKpis(String str);

    int writeLogMsg(int i, int i2, int i3, String str);

    int writeLogMsg(int i, int i2, int i3, String str, int i4, String str2, int i5);

    int writeLogMsg(int i, int i2, String str);

    int writeMediaBigData(int i, int i2, String str);

    void writeMediaBigDataByReportInf(int i, int i2, String str);
}
