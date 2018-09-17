package com.android.server.am;

import android.content.Intent;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import java.util.List;

public class HwBroadcastRecord {
    private BroadcastRecord mBroadcastRecord = null;
    private int mReceiverUid = -1;

    public HwBroadcastRecord(BroadcastRecord br) {
        if (br == null) {
            throw new NullPointerException();
        }
        this.mBroadcastRecord = br;
    }

    public BroadcastRecord getBroadcastRecord() {
        return this.mBroadcastRecord;
    }

    public BroadcastQueue getBroacastQueue() {
        return this.mBroadcastRecord.queue;
    }

    public int getCallingPid() {
        return this.mBroadcastRecord.callingPid;
    }

    public Intent getIntent() {
        return this.mBroadcastRecord.intent;
    }

    public String getAction() {
        if (this.mBroadcastRecord.intent != null) {
            return this.mBroadcastRecord.intent.getAction();
        }
        return null;
    }

    public String getResolvedType() {
        return this.mBroadcastRecord.resolvedType;
    }

    public boolean isBg() {
        return MemoryConstant.MEM_REPAIR_CONSTANT_BG.equals(this.mBroadcastRecord.queue.mQueueName);
    }

    public long getDispatchClockTime() {
        return this.mBroadcastRecord.dispatchClockTime;
    }

    public List getBrReceivers() {
        return this.mBroadcastRecord.receivers;
    }

    public String getReceiverPkg(Object target) {
        if (target instanceof BroadcastFilter) {
            return ((BroadcastFilter) target).packageName;
        }
        return null;
    }

    public void setReceiverUid(int uid) {
        this.mReceiverUid = uid;
    }

    public int getReceiverUid() {
        return this.mReceiverUid;
    }

    public String toString() {
        return this.mBroadcastRecord.toString();
    }
}
