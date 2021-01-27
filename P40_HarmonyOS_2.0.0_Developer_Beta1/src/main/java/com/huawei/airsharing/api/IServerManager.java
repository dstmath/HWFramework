package com.huawei.airsharing.api;

import android.content.Context;

public interface IServerManager {
    void deInit();

    boolean init(Context context);

    void registerListener(IEventListener iEventListener);

    void unregisterListener(IEventListener iEventListener);
}
