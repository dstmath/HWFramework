package com.android.server.wifi;

public interface HwSupplicantHeartBeat {
    void enterSupplicantStarted();

    void exitSupplicantStarted();

    void handleHeartBeatAckEvent();
}
