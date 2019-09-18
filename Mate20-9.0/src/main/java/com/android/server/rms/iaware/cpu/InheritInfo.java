package com.android.server.rms.iaware.cpu;

import android.util.SparseIntArray;

public class InheritInfo {
    private int mComputeCount = 0;
    private SparseIntArray mPidList = new SparseIntArray();

    public void addToPidList(int pid, int ppid) {
        this.mPidList.put(pid, ppid);
    }

    public int getPidFromList(int index) {
        return this.mPidList.keyAt(index);
    }

    public int getPPidFromList(int index) {
        return this.mPidList.valueAt(index);
    }

    public int getListSize() {
        return this.mPidList.size();
    }

    public void clearPidList() {
        this.mPidList.clear();
    }

    public void removeFromPidList(int pid) {
        this.mPidList.delete(pid);
    }

    public int getComputeCount() {
        return this.mComputeCount;
    }

    public void setComputeCount(int computeCount) {
        this.mComputeCount = computeCount;
    }

    public void addComputeCount() {
        this.mComputeCount++;
    }
}
