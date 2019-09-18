package com.huawei.android.hwaps;

public interface ISmartLowpowerBrowser {
    boolean doProcessDrawSLB(long j, boolean z, boolean z2);

    boolean initSLB(String str);

    void setFrameScheduledSLB();

    void setPlayingVideoSLB(boolean z);

    void setTouchState(int i);
}
