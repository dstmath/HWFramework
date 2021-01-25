package com.android.server.wm;

import android.util.SparseIntArray;

/* access modifiers changed from: package-private */
public class MirrorActiveUids {
    private SparseIntArray mUidStates = new SparseIntArray();

    MirrorActiveUids() {
    }

    /* access modifiers changed from: package-private */
    public synchronized void onUidActive(int uid, int procState) {
        this.mUidStates.put(uid, procState);
    }

    /* access modifiers changed from: package-private */
    public synchronized void onUidInactive(int uid) {
        this.mUidStates.delete(uid);
    }

    /* access modifiers changed from: package-private */
    public synchronized void onActiveUidsCleared() {
        this.mUidStates.clear();
    }

    /* access modifiers changed from: package-private */
    public synchronized void onUidProcStateChanged(int uid, int procState) {
        int index = this.mUidStates.indexOfKey(uid);
        if (index >= 0) {
            this.mUidStates.setValueAt(index, procState);
        }
    }

    /* access modifiers changed from: package-private */
    public synchronized int getUidState(int uid) {
        return this.mUidStates.get(uid, 21);
    }
}
