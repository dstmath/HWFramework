package android.media;

import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public interface IHwMediaMonitor {
    int forceLogSend(int i);

    int writeBigData(int i, String str);

    int writeBigData(int i, String str, int i2, int i3);

    int writeBigData(int i, String str, String str2, int i2);

    int writeBigData(int i, String str, String str2, String str3);

    int writeBigData(int i, String str, String str2, String str3, int i2);

    int writeLogMsg(int i, int i2, int i3, String str);

    int writeLogMsg(int i, int i2, String str);
}
