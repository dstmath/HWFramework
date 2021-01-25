package com.android.server.hidata.histream;

import com.android.server.hidata.appqoe.HwAPPStateInfo;

public interface IHwHiStreamCallback {
    void onAPPStateChangeCallback(HwAPPStateInfo hwAPPStateInfo, int i);
}
