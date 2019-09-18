package com.android.server.pm;

import android.os.Binder;

class KeySetHandle extends Binder {
    private final long mId;
    private int mRefCount;

    protected KeySetHandle(long id) {
        this.mId = id;
        this.mRefCount = 1;
    }

    protected KeySetHandle(long id, int refCount) {
        this.mId = id;
        this.mRefCount = refCount;
    }

    public long getId() {
        return this.mId;
    }

    /* access modifiers changed from: protected */
    public int getRefCountLPr() {
        return this.mRefCount;
    }

    /* access modifiers changed from: protected */
    public void setRefCountLPw(int newCount) {
        this.mRefCount = newCount;
    }

    /* access modifiers changed from: protected */
    public void incrRefCountLPw() {
        this.mRefCount++;
    }

    /* access modifiers changed from: protected */
    public int decrRefCountLPw() {
        this.mRefCount--;
        return this.mRefCount;
    }
}
