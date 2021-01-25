package com.android.server.wifi.p2p;

import com.android.internal.util.State;

public interface IHwWifiP2pServiceInner {
    State getAfterUserAuthorizingJoinState();

    boolean hasMessages(int i);
}
