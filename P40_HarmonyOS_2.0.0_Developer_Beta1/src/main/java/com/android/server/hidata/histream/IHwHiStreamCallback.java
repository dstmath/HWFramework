package com.android.server.hidata.histream;

import com.android.server.hidata.appqoe.HwAppStateInfo;

public interface IHwHiStreamCallback {
    void onAppStateChangeCallback(HwAppStateInfo hwAppStateInfo, int i);
}
