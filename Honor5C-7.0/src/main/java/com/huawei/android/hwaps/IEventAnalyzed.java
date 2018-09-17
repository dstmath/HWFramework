package com.huawei.android.hwaps;

import android.content.Context;

public interface IEventAnalyzed {
    boolean StopSdrForSpecial(String str, int i);

    int checkAd(String str);

    String[] getCustAppList(Context context, int i);

    int getCustScreenDimDurationLocked(int i);

    String[] getQueryResultGameList(Context context, int i);

    void initAPS(Context context, int i, int i2);

    boolean isAPSReady();

    boolean isAdCheckEnable(String str);

    boolean isGameProcess(String str);

    void processAnalyze(Context context, int i, long j, int i2, int i3, int i4, long j2);

    void setHasOnPaused(boolean z);
}
