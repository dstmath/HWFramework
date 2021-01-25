package com.android.server.hidata;

import android.net.wifi.RssiPacketCountInfo;

public interface IHidataCallback {
    void OnSetHiLinkAccGameMode(boolean z, String str);

    RssiPacketCountInfo onGetOtaInfo();

    void onPauseABSHandover();

    void onRestartABSHandover();

    int onSetHumanFactor(String str, int i);

    void onSetPMMode(int i);

    void onSetTXPower(int i);
}
