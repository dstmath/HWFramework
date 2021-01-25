package com.android.server.hidata.arbitration;

public interface IGameReportCallback {
    void onReportGameDelay(int i);

    void onReportGameState(boolean z, boolean z2, int i);
}
