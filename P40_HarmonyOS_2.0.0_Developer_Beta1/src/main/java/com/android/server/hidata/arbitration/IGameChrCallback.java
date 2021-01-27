package com.android.server.hidata.arbitration;

import com.android.server.hidata.appqoe.HwAppStateInfo;

public interface IGameChrCallback {
    void updateGameExperience(HwAppStateInfo hwAppStateInfo);

    void updateGameState(HwAppStateInfo hwAppStateInfo, int i);
}
