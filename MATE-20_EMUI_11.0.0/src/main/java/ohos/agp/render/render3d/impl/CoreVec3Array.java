package ohos.agp.render.render3d.impl;

class CoreVec3Array {
    private transient long agpCptrCoreVec3Array;
    private final Object delLock;
    transient boolean isAgpCmemOwn;

    CoreVec3Array(long j, boolean z) {
        this.delLock = new Object();
        this.isAgpCmemOwn = z;
        this.agpCptrCoreVec3Array = j;
    }

    static long getCptr(CoreVec3Array coreVec3Array) {
        if (coreVec3Array == null) {
            return 0;
        }
        return coreVec3Array.agpCptrCoreVec3Array;
    }

    /* access modifiers changed from: protected */
    public void finalize() {
        delete();
    }

    /* access modifiers changed from: package-private */
    public void delete() {
        synchronized (this.delLock) {
            if (this.agpCptrCoreVec3Array != 0) {
                if (this.isAgpCmemOwn) {
                    this.isAgpCmemOwn = false;
                    CoreJni.deleteCoreVec3Array(this.agpCptrCoreVec3Array);
                }
                this.agpCptrCoreVec3Array = 0;
            }
        }
    }

    static long getCptrAndSetMemOwn(CoreVec3Array coreVec3Array, boolean z) {
        if (coreVec3Array != null) {
            synchronized (coreVec3Array.delLock) {
                coreVec3Array.isAgpCmemOwn = z;
            }
        }
        return getCptr(coreVec3Array);
    }

    CoreVec3Array() {
        this(CoreJni.newCoreVec3Array0(), true);
    }

    CoreVec3Array(long j) {
        this(CoreJni.newCoreVec3Array1(j), true);
    }

    /* access modifiers changed from: package-private */
    public long size() {
        return CoreJni.sizeInCoreVec3Array(this.agpCptrCoreVec3Array, this);
    }

    /* access modifiers changed from: package-private */
    public long capacity() {
        return CoreJni.capacityInCoreVec3Array(this.agpCptrCoreVec3Array, this);
    }

    /* access modifiers changed from: package-private */
    public void reserve(long j) {
        CoreJni.reserveInCoreVec3Array(this.agpCptrCoreVec3Array, this, j);
    }

    /* access modifiers changed from: package-private */
    public boolean isEmpty() {
        return CoreJni.isEmptyInCoreVec3Array(this.agpCptrCoreVec3Array, this);
    }

    /* access modifiers changed from: package-private */
    public void clear() {
        CoreJni.clearInCoreVec3Array(this.agpCptrCoreVec3Array, this);
    }

    /* access modifiers changed from: package-private */
    public void add(CoreVec3 coreVec3) {
        CoreJni.addInCoreVec3Array(this.agpCptrCoreVec3Array, this, CoreVec3.getCptr(coreVec3), coreVec3);
    }

    /* access modifiers changed from: package-private */
    public CoreVec3 get(int i) {
        return new CoreVec3(CoreJni.getInCoreVec3Array(this.agpCptrCoreVec3Array, this, i), false);
    }

    /* access modifiers changed from: package-private */
    public void set(int i, CoreVec3 coreVec3) {
        CoreJni.setInCoreVec3Array(this.agpCptrCoreVec3Array, this, i, CoreVec3.getCptr(coreVec3), coreVec3);
    }
}
