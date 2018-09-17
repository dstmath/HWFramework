package com.android.server.wifi;

import com.android.internal.util.StateMachine;

public interface HwWifiMonitor {
    void parsingSupplicantHeartBeatEvent(String str, StateMachine stateMachine);

    void parsingWAPIEvent(String str, StateMachine stateMachine);
}
