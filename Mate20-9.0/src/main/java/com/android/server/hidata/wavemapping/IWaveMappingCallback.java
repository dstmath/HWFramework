package com.android.server.hidata.wavemapping;

public interface IWaveMappingCallback {
    void onWaveMappingReportCallback(int i, String str, int i2);

    void onWaveMappingRespond4BackCallback(int i, int i2, int i3, boolean z, boolean z2);

    void onWaveMappingRespondCallback(int i, int i2, int i3, boolean z, boolean z2);
}
