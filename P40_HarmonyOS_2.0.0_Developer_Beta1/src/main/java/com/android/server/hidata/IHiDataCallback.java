package com.android.server.hidata;

import android.net.wifi.RssiPacketCountInfo;

public interface IHiDataCallback {
    void OnSetHiLinkAccGameMode(boolean z, String str);

    RssiPacketCountInfo onGetOtaInfo();

    void onPauseAbsHandover();

    void onRestartAbsHandover();

    int onSetHumanFactor(String str, int i);

    void onSetPmMode(int i);

    void onSetTxPower(int i);
}
