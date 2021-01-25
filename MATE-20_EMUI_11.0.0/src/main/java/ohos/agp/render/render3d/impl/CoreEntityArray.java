package ohos.agp.render.render3d.impl;

class CoreEntityArray {
    private transient long agpCptrCoreEntityArray;
    transient boolean isAgpCmemOwn;
    private final Object lock;

    CoreEntityArray(long j, boolean z) {
        this.lock = new Object();
        this.isAgpCmemOwn = z;
        this.agpCptrCoreEntityArray = j;
    }

    static long getCptr(CoreEntityArray coreEntityArray) {
        if (coreEntityArray == null) {
            return 0;
        }
        return coreEntityArray.agpCptrCoreEntityArray;
    }

    /* access modifiers changed from: protected */
    public void finalize() {
        delete();
    }

    /* access modifiers changed from: package-private */
    public void delete() {
        synchronized (this.lock) {
            if (this.agpCptrCoreEntityArray != 0) {
                if (this.isAgpCmemOwn) {
                    this.isAgpCmemOwn = false;
                    CoreJni.deleteCoreEntityArray(this.agpCptrCoreEntityArray);
                }
                this.agpCptrCoreEntityArray = 0;
            }
        }
    }

    static long getCptrAndSetMemOwn(CoreEntityArray coreEntityArray, boolean z) {
        if (coreEntityArray != null) {
            synchronized (coreEntityArray.lock) {
                coreEntityArray.isAgpCmemOwn = z;
            }
        }
        return getCptr(coreEntityArray);
    }

    CoreEntityArray() {
        this(CoreJni.newCoreEntityArray0(), true);
    }

    CoreEntityArray(long j) {
        this(CoreJni.newCoreEntityArray1(j), true);
    }

    /* access modifiers changed from: package-private */
    public long size() {
        return CoreJni.sizeInCoreEntityArray(this.agpCptrCoreEntityArray, this);
    }

    /* access modifiers changed from: package-private */
    public long capacity() {
        return CoreJni.capacityInCoreEntityArray(this.agpCptrCoreEntityArray, this);
    }

    /* access modifiers changed from: package-private */
    public void reserve(long j) {
        CoreJni.reserveInCoreEntityArray(this.agpCptrCoreEntityArray, this, j);
    }

    /* access modifiers changed from: package-private */
    public boolean isEmpty() {
        return CoreJni.isEmptyInCoreEntityArray(this.agpCptrCoreEntityArray, this);
    }

    /* access modifiers changed from: package-private */
    public void clear() {
        CoreJni.clearInCoreEntityArray(this.agpCptrCoreEntityArray, this);
    }

    /* access modifiers changed from: package-private */
    public void add(CoreEntity coreEntity) {
        CoreJni.addInCoreEntityArray(this.agpCptrCoreEntityArray, this, CoreEntity.getCptr(coreEntity), coreEntity);
    }

    /* access modifiers changed from: package-private */
    public CoreEntity get(int i) {
        return new CoreEntity(CoreJni.getInCoreEntityArray(this.agpCptrCoreEntityArray, this, i), false);
    }

    /* access modifiers changed from: package-private */
    public void set(int i, CoreEntity coreEntity) {
        CoreJni.setInCoreEntityArray(this.agpCptrCoreEntityArray, this, i, CoreEntity.getCptr(coreEntity), coreEntity);
    }
}
