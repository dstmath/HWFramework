package com.huawei.android.view;

import android.view.IRotationWatcher;

public class IRotationWatcherEx extends IRotationWatcher.Stub {
    public Class getIRotationWatcherClass() {
        return IRotationWatcher.class;
    }

    public void onRotationChanged(int rotation) {
    }
}
