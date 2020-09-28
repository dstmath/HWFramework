package com.huawei.android.hwaps;

import android.content.Context;

public interface IEventAnalyzed {
    int getCustScreenDimDurationLocked(int i);

    void initAPS(Context context, int i);

    void processAnalyze(int i);

    void setHasOnPaused(boolean z);
}
