package com.huawei.agpengine.impl;

/* access modifiers changed from: package-private */
public class CoreMeshBuilderPtr {
    private transient long agpCptr;
    transient boolean isAgpCmemOwn;

    CoreMeshBuilderPtr(long cptr, boolean isCmemoryOwn) {
        this.isAgpCmemOwn = isCmemoryOwn;
        this.agpCptr = cptr;
    }

    static long getCptr(CoreMeshBuilderPtr obj) {
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
                CoreJni.deleteCoreMeshBuilderPtr(this.agpCptr);
            }
            this.agpCptr = 0;
        }
    }

    CoreMeshBuilderPtr(CoreMeshBuilder ptr) {
        this(CoreJni.newCoreMeshBuilderPtr0(CoreMeshBuilder.getCptr(ptr), ptr), true);
    }

    CoreMeshBuilderPtr(CoreMeshBuilderPtr ptr) {
        this(CoreJni.newCoreMeshBuilderPtr1(getCptr(ptr), ptr), true);
    }

    /* access modifiers changed from: package-private */
    public CoreMeshBuilder get() {
        long cptr = CoreJni.getInCoreMeshBuilderPtr(this.agpCptr, this);
        if (cptr == 0) {
            return null;
        }
        return new CoreMeshBuilder(cptr, false);
    }

    /* access modifiers changed from: package-private */
    public void reset(CoreMeshBuilder ptr) {
        CoreJni.resetInCoreMeshBuilderPtr0(this.agpCptr, this, CoreMeshBuilder.getCptr(ptr), ptr);
    }

    /* access modifiers changed from: package-private */
    public void reset() {
        CoreJni.resetInCoreMeshBuilderPtr1(this.agpCptr, this);
    }
}
