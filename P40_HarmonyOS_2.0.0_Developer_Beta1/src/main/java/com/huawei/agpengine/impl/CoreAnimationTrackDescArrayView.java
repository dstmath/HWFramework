package com.huawei.agpengine.impl;

import java.nio.Buffer;

class CoreAnimationTrackDescArrayView {
    private transient long agpCptr;
    transient boolean isAgpCmemOwn;

    CoreAnimationTrackDescArrayView(long cptr, boolean isCmemoryOwn) {
        this.isAgpCmemOwn = isCmemoryOwn;
        this.agpCptr = cptr;
    }

    static long getCptr(CoreAnimationTrackDescArrayView obj) {
        long j;
        if (obj == null) {
            return 0;
        }
        synchronized (obj) {
            j = obj.agpCptr;
        }
        return j;
    }

    /* access modifiers changed from: protected */
    public void finalize() {
        delete();
    }

    /* access modifiers changed from: package-private */
    public synchronized void delete() {
        if (this.agpCptr != 0) {
            if (this.isAgpCmemOwn) {
                this.isAgpCmemOwn = false;
                CoreJni.deleteCoreAnimationTrackDescArrayView(this.agpCptr);
            }
            this.agpCptr = 0;
        }
    }

    CoreAnimationTrackDescArrayView(Buffer begin) {
        this(CoreJni.newCoreAnimationTrackDescArrayView(begin), true);
    }

    /* access modifiers changed from: package-private */
    public long size() {
        return CoreJni.sizeInCoreAnimationTrackDescArrayView(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public CoreAnimationTrackDesc get(long index) {
        return new CoreAnimationTrackDesc(CoreJni.getInCoreAnimationTrackDescArrayView(this.agpCptr, this, index), true);
    }
}
