package com.huawei.android.app;

import android.app.IUidObserver;

public class UidObserverEx extends IUidObserver.Stub {
    public void onUidStateChanged(int uid, int procState, long procStateSeq) {
    }

    public void onUidGone(int uid, boolean disabled) {
    }

    public void onUidActive(int uid) {
    }

    public void onUidIdle(int uid, boolean disabled) {
    }

    public void onUidCachedChanged(int uid, boolean cached) {
    }
}
