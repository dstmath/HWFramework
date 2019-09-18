package com.android.server.location;

public interface IHwCmccGpsFeature {
    boolean checkSuplInit();

    void setDelAidData();

    int setPostionModeAndAgpsServer(int i, boolean z);

    void setRoaming(boolean z);

    void syncTime(long j);
}
