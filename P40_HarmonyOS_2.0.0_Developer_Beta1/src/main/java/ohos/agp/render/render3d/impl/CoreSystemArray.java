package ohos.agp.render.render3d.impl;

class CoreSystemArray {
    private transient long agpCptrCoreSystemArray;
    private final Object delLock;
    transient boolean isAgpCmemOwn;

    CoreSystemArray(long j, boolean z) {
        this.delLock = new Object();
        this.isAgpCmemOwn = z;
        this.agpCptrCoreSystemArray = j;
    }

    static long getCptr(CoreSystemArray coreSystemArray) {
        if (coreSystemArray == null) {
            return 0;
        }
        return coreSystemArray.agpCptrCoreSystemArray;
    }

    /* access modifiers changed from: protected */
    public void finalize() {
        delete();
    }

    /* access modifiers changed from: package-private */
    public void delete() {
        synchronized (this.delLock) {
            if (this.agpCptrCoreSystemArray != 0) {
                if (this.isAgpCmemOwn) {
                    this.isAgpCmemOwn = false;
                    CoreJni.deleteCoreSystemArray(this.agpCptrCoreSystemArray);
                }
                this.agpCptrCoreSystemArray = 0;
            }
        }
    }

    static long getCptrAndSetMemOwn(CoreSystemArray coreSystemArray, boolean z) {
        if (coreSystemArray != null) {
            synchronized (coreSystemArray.delLock) {
                coreSystemArray.isAgpCmemOwn = z;
            }
        }
        return getCptr(coreSystemArray);
    }

    CoreSystemArray() {
        this(CoreJni.newCoreSystemArray0(), true);
    }

    CoreSystemArray(long j) {
        this(CoreJni.newCoreSystemArray1(j), true);
    }

    /* access modifiers changed from: package-private */
    public long size() {
        return CoreJni.sizeInCoreSystemArray(this.agpCptrCoreSystemArray, this);
    }

    /* access modifiers changed from: package-private */
    public long capacity() {
        return CoreJni.capacityInCoreSystemArray(this.agpCptrCoreSystemArray, this);
    }

    /* access modifiers changed from: package-private */
    public void reserve(long j) {
        CoreJni.reserveInCoreSystemArray(this.agpCptrCoreSystemArray, this, j);
    }

    /* access modifiers changed from: package-private */
    public boolean isEmpty() {
        return CoreJni.isEmptyInCoreSystemArray(this.agpCptrCoreSystemArray, this);
    }

    /* access modifiers changed from: package-private */
    public void clear() {
        CoreJni.clearInCoreSystemArray(this.agpCptrCoreSystemArray, this);
    }

    /* access modifiers changed from: package-private */
    public void add(CoreSystem coreSystem) {
        CoreJni.addInCoreSystemArray(this.agpCptrCoreSystemArray, this, CoreSystem.getCptr(coreSystem), coreSystem);
    }

    /* access modifiers changed from: package-private */
    public CoreSystem get(int i) {
        long inCoreSystemArray = CoreJni.getInCoreSystemArray(this.agpCptrCoreSystemArray, this, i);
        if (inCoreSystemArray == 0) {
            return null;
        }
        return new CoreSystem(inCoreSystemArray, false);
    }

    /* access modifiers changed from: package-private */
    public void set(int i, CoreSystem coreSystem) {
        CoreJni.setInCoreSystemArray(this.agpCptrCoreSystemArray, this, i, CoreSystem.getCptr(coreSystem), coreSystem);
    }
}
