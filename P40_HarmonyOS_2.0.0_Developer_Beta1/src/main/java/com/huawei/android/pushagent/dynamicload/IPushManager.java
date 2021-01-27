package com.huawei.android.pushagent.dynamicload;

import android.content.Context;
import android.content.Intent;
import android.os.IBinder;

public interface IPushManager {
    void destroyPushService();

    IBinder onBind(Intent intent);

    void startPushService(Context context);
}
