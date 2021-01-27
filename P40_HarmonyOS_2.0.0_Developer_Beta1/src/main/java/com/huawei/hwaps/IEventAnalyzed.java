package com.huawei.hwaps;

import android.content.Context;

public interface IEventAnalyzed {
    int getCustScreenDimDurationLocked(int i);

    void initAps(Context context, int i);

    void processAnalyze(int i);

    void setHasOnPaused(boolean z);
}
