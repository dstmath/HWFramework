package com.android.server.am;

import android.content.Intent;
import com.android.server.rms.iaware.memory.utils.MemoryConstant;
import java.util.List;

public class HwBroadcastRecord {
    private BroadcastRecord mBroadcastRecord = null;
    private int mCurAdj = -10000;
    private int mCurProcState = -1;
    private int mCurrentReceiverPid;
    private boolean mIsSysApp = false;
    private String mPkg = null;
    private int mReceiverPid = -1;
    private int mReceiverUid = -1;

    public HwBroadcastRecord(BroadcastRecord br) {
        if (br != null) {
            this.mBroadcastRecord = br;
            return;
        }
        throw new NullPointerException();
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

    public void setReceiverUid(int uid) {
        this.mReceiverUid = uid;
    }

    public void setReceiverPid(int pid) {
        this.mReceiverPid = pid;
    }

    public void setReceiverCurAdj(int curAdj) {
        this.mCurAdj = curAdj;
    }

    public void setReceiverPkg(String pkg) {
        this.mPkg = pkg;
    }

    public void setSysApp(boolean isSysApp) {
        this.mIsSysApp = isSysApp;
    }

    public void setReceiverCurProcState(int curProcState) {
        this.mCurProcState = curProcState;
    }

    public int getReceiverUid() {
        return this.mReceiverUid;
    }

    public int getReceiverPid() {
        return this.mReceiverPid;
    }

    public int getReceiverCurAdj() {
        return this.mCurAdj;
    }

    public String getReceiverPkg() {
        return this.mPkg;
    }

    public boolean isSysApp() {
        return this.mIsSysApp;
    }

    public int getReceiverCurProcState() {
        return this.mCurProcState;
    }

    public boolean isSameReceiver(HwBroadcastRecord br) {
        if (br != null && getBrReceivers().get(0) == br.getBrReceivers().get(0)) {
            return true;
        }
        return false;
    }

    public String toString() {
        return this.mBroadcastRecord.toString();
    }

    public String getBrQueueName() {
        return this.mBroadcastRecord.queue.mQueueName;
    }

    public void setCurrentReceiverPid(int pid) {
        this.mCurrentReceiverPid = pid;
    }

    public int getCurrentReceiverPid() {
        return this.mCurrentReceiverPid;
    }

    public String getCallerPackage() {
        return this.mBroadcastRecord.callerPackage;
    }
}
