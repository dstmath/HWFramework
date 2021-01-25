package com.android.server.hidata.arbitration;

import com.android.server.hidata.appqoe.HwAPPStateInfo;
import com.android.server.hidata.histream.HwHistreamCHRQoeInfo;

public interface IGameCHRCallback {
    void updataGameExperience(HwAPPStateInfo hwAPPStateInfo);

    void updateGameState(HwAPPStateInfo hwAPPStateInfo, int i);

    void updateHistreamExperience(HwHistreamCHRQoeInfo hwHistreamCHRQoeInfo);
}
