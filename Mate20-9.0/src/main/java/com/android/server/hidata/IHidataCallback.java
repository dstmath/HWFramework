package com.android.server.hidata;

import android.net.wifi.RssiPacketCountInfo;

public interface IHidataCallback {
    void OnSetHiLinkAccGameMode(boolean z, String str);

    RssiPacketCountInfo onGetOTAInfo();

    void onPauseABSHandover();

    void onRestartABSHandover();

    void onSetPMMode(int i);

    void onSetTXPower(int i);
}
