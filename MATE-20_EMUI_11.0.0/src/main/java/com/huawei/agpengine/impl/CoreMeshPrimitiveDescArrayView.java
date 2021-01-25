package com.huawei.agpengine.impl;

import java.nio.Buffer;

/* access modifiers changed from: package-private */
public class CoreMeshPrimitiveDescArrayView {
    private transient long agpCptr;
    transient boolean isAgpCmemOwn;

    CoreMeshPrimitiveDescArrayView(long cptr, boolean isCmemoryOwn) {
        this.isAgpCmemOwn = isCmemoryOwn;
        this.agpCptr = cptr;
    }

    static long getCptr(CoreMeshPrimitiveDescArrayView obj) {
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
                CoreJni.deleteCoreMeshPrimitiveDescArrayView(this.agpCptr);
            }
            this.agpCptr = 0;
        }
    }

    CoreMeshPrimitiveDescArrayView(Buffer begin) {
        this(CoreJni.newCoreMeshPrimitiveDescArrayView(begin), true);
    }

    /* access modifiers changed from: package-private */
    public long size() {
        return CoreJni.sizeInCoreMeshPrimitiveDescArrayView(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public CoreMeshPrimitiveDesc get(long index) {
        return new CoreMeshPrimitiveDesc(CoreJni.getInCoreMeshPrimitiveDescArrayView(this.agpCptr, this, index), true);
    }
}
