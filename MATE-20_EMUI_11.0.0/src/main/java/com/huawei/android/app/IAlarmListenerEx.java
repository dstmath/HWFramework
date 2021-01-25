package com.huawei.android.app;

import android.app.IAlarmListener;

public class IAlarmListenerEx {
    private IAlarmListener mIAlarmListener;

    public void setIAlarmListener(IAlarmListener listener) {
        this.mIAlarmListener = listener;
    }

    public IAlarmListener getIAlarmListener() {
        return this.mIAlarmListener;
    }
}
