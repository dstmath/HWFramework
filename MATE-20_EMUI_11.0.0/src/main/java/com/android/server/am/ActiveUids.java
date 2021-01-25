package com.android.server.am;

import android.util.SparseArray;

/* access modifiers changed from: package-private */
public final class ActiveUids {
    private final SparseArray<UidRecord> mActiveUids = new SparseArray<>();
    private boolean mPostChangesToAtm;
    private ActivityManagerService mService;

    ActiveUids(ActivityManagerService service, boolean postChangesToAtm) {
        this.mService = service;
        this.mPostChangesToAtm = postChangesToAtm;
    }

    /* access modifiers changed from: package-private */
    public void put(int uid, UidRecord value) {
        this.mActiveUids.put(uid, value);
        if (this.mPostChangesToAtm) {
            this.mService.mAtmInternal.onUidActive(uid, value.getCurProcState());
        }
    }

    /* access modifiers changed from: package-private */
    public void remove(int uid) {
        this.mActiveUids.remove(uid);
        if (this.mPostChangesToAtm) {
            this.mService.mAtmInternal.onUidInactive(uid);
        }
    }

    /* access modifiers changed from: package-private */
    public void clear() {
        this.mActiveUids.clear();
        if (this.mPostChangesToAtm) {
            this.mService.mAtmInternal.onActiveUidsCleared();
        }
    }

    /* access modifiers changed from: package-private */
    public UidRecord get(int uid) {
        return this.mActiveUids.get(uid);
    }

    /* access modifiers changed from: package-private */
    public int size() {
        return this.mActiveUids.size();
    }

    /* access modifiers changed from: package-private */
    public UidRecord valueAt(int index) {
        return this.mActiveUids.valueAt(index);
    }

    /* access modifiers changed from: package-private */
    public int keyAt(int index) {
        return this.mActiveUids.keyAt(index);
    }

    /* access modifiers changed from: package-private */
    public int indexOfKey(int uid) {
        return this.mActiveUids.indexOfKey(uid);
    }
}
