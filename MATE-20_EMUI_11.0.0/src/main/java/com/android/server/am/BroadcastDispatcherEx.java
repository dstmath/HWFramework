package com.android.server.am;

import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class BroadcastDispatcherEx {
    private BroadcastDispatcher mBroadcastDispatcher;

    public BroadcastDispatcherEx() {
    }

    public BroadcastDispatcherEx(BroadcastDispatcher dispatcher) {
        this.mBroadcastDispatcher = dispatcher;
    }

    public void enqueueOrderedBroadcastLocked(int index, BroadcastRecordEx br) {
        this.mBroadcastDispatcher.enqueueOrderedBroadcastLocked(index, br.getBroadcastRecord());
    }

    public BroadcastRecordEx getActiveBroadcastLocked() {
        return new BroadcastRecordEx(this.mBroadcastDispatcher.getActiveBroadcastLocked());
    }

    public int getOrderedBroadcastsSize() {
        return this.mBroadcastDispatcher.getOrderedBroadcastsSize();
    }
}
