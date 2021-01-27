package com.android.server.am;

import android.os.Bundle;
import com.huawei.annotation.HwSystemApi;

@HwSystemApi
public class BroadcastQueueEx {
    private BroadcastQueue mBroadcastQueue;

    public BroadcastQueueEx() {
    }

    public BroadcastQueueEx(BroadcastQueue queue) {
        this.mBroadcastQueue = queue;
    }

    public BroadcastQueue getBroadcastQueue() {
        return this.mBroadcastQueue;
    }

    public String getQueueName() {
        return this.mBroadcastQueue.mQueueName;
    }

    public Object getService() {
        return this.mBroadcastQueue.mService;
    }

    public void scheduleBroadcastsLocked() {
        this.mBroadcastQueue.scheduleBroadcastsLocked();
    }

    public boolean getPendingBroadcastTimeoutMessage() {
        return this.mBroadcastQueue.mPendingBroadcastTimeoutMessage;
    }

    public BroadcastDispatcherEx getDispatcher() {
        return new BroadcastDispatcherEx(this.mBroadcastQueue.mDispatcher);
    }

    public void enqueueParallelBroadcasts(int index, BroadcastRecordEx br) {
        this.mBroadcastQueue.mParallelBroadcasts.add(index, br.getBroadcastRecord());
    }

    public boolean finishReceiverLocked(BroadcastRecordEx br, int resultCode, String resultData, Bundle resultExtras, boolean resultAbort, boolean waitForServices) {
        return this.mBroadcastQueue.finishReceiverLocked(br.getBroadcastRecord(), resultCode, resultData, resultExtras, resultAbort, waitForServices);
    }

    public void enqueueOrderedBroadcastLocked(BroadcastRecordEx br) {
        this.mBroadcastQueue.enqueueOrderedBroadcastLocked(br.getBroadcastRecord());
    }

    public boolean isQueueNull() {
        return this.mBroadcastQueue == null;
    }

    public ProcessRecordEx getProcessRecordLocked(String processName, int uid, boolean keepIfLarge) {
        return new ProcessRecordEx(this.mBroadcastQueue.mService.getProcessRecordLocked(processName, uid, keepIfLarge));
    }
}
