package com.huawei.android.pushagent.dynamicload;

import android.content.Context;

public interface IPushManager {
    void destroyPushService();

    void startPushService(Context context);
}
