package ohos.agp.render.render3d.impl;

class CoreVec4Array {
    private transient long agpCptrCoreVec4Array;
    private final Object delLock;
    transient boolean isAgpCmemOwn;

    CoreVec4Array(long j, boolean z) {
        this.delLock = new Object();
        this.isAgpCmemOwn = z;
        this.agpCptrCoreVec4Array = j;
    }

    static long getCptr(CoreVec4Array coreVec4Array) {
        if (coreVec4Array == null) {
            return 0;
        }
        return coreVec4Array.agpCptrCoreVec4Array;
    }

    /* access modifiers changed from: protected */
    public void finalize() {
        delete();
    }

    /* access modifiers changed from: package-private */
    public void delete() {
        synchronized (this.delLock) {
            if (this.agpCptrCoreVec4Array != 0) {
                if (this.isAgpCmemOwn) {
                    this.isAgpCmemOwn = false;
                    CoreJni.deleteCoreVec4Array(this.agpCptrCoreVec4Array);
                }
                this.agpCptrCoreVec4Array = 0;
            }
        }
    }

    static long getCptrAndSetMemOwn(CoreVec4Array coreVec4Array, boolean z) {
        if (coreVec4Array != null) {
            synchronized (coreVec4Array.delLock) {
                coreVec4Array.isAgpCmemOwn = z;
            }
        }
        return getCptr(coreVec4Array);
    }

    CoreVec4Array() {
        this(CoreJni.newCoreVec4Array0(), true);
    }

    CoreVec4Array(long j) {
        this(CoreJni.newCoreVec4Array1(j), true);
    }

    /* access modifiers changed from: package-private */
    public long size() {
        return CoreJni.sizeInCoreVec4Array(this.agpCptrCoreVec4Array, this);
    }

    /* access modifiers changed from: package-private */
    public long capacity() {
        return CoreJni.capacityInCoreVec4Array(this.agpCptrCoreVec4Array, this);
    }

    /* access modifiers changed from: package-private */
    public void reserve(long j) {
        CoreJni.reserveInCoreVec4Array(this.agpCptrCoreVec4Array, this, j);
    }

    /* access modifiers changed from: package-private */
    public boolean isEmpty() {
        return CoreJni.isEmptyInCoreVec4Array(this.agpCptrCoreVec4Array, this);
    }

    /* access modifiers changed from: package-private */
    public void clear() {
        CoreJni.clearInCoreVec4Array(this.agpCptrCoreVec4Array, this);
    }

    /* access modifiers changed from: package-private */
    public void add(CoreVec4 coreVec4) {
        CoreJni.addInCoreVec4Array(this.agpCptrCoreVec4Array, this, CoreVec4.getCptr(coreVec4), coreVec4);
    }

    /* access modifiers changed from: package-private */
    public CoreVec4 get(int i) {
        return new CoreVec4(CoreJni.getInCoreVec4Array(this.agpCptrCoreVec4Array, this, i), false);
    }

    /* access modifiers changed from: package-private */
    public void set(int i, CoreVec4 coreVec4) {
        CoreJni.setInCoreVec4Array(this.agpCptrCoreVec4Array, this, i, CoreVec4.getCptr(coreVec4), coreVec4);
    }
}
