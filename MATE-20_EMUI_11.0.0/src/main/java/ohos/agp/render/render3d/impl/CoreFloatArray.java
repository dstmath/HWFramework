package ohos.agp.render.render3d.impl;

class CoreFloatArray {
    private transient long agpCptrCoreFloatArray;
    transient boolean isAgpCmemOwn;
    private final Object lock;

    CoreFloatArray(long j, boolean z) {
        this.lock = new Object();
        this.isAgpCmemOwn = z;
        this.agpCptrCoreFloatArray = j;
    }

    static long getCptr(CoreFloatArray coreFloatArray) {
        if (coreFloatArray == null) {
            return 0;
        }
        return coreFloatArray.agpCptrCoreFloatArray;
    }

    /* access modifiers changed from: protected */
    public void finalize() {
        delete();
    }

    /* access modifiers changed from: package-private */
    public void delete() {
        synchronized (this.lock) {
            if (this.agpCptrCoreFloatArray != 0) {
                if (this.isAgpCmemOwn) {
                    this.isAgpCmemOwn = false;
                    CoreJni.deleteCoreFloatArray(this.agpCptrCoreFloatArray);
                }
                this.agpCptrCoreFloatArray = 0;
            }
        }
    }

    static long getCptrAndSetMemOwn(CoreFloatArray coreFloatArray, boolean z) {
        if (coreFloatArray != null) {
            synchronized (coreFloatArray.lock) {
                coreFloatArray.isAgpCmemOwn = z;
            }
        }
        return getCptr(coreFloatArray);
    }

    CoreFloatArray() {
        this(CoreJni.newCoreFloatArray0(), true);
    }

    CoreFloatArray(long j) {
        this(CoreJni.newCoreFloatArray1(j), true);
    }

    /* access modifiers changed from: package-private */
    public long size() {
        return CoreJni.sizeInCoreFloatArray(this.agpCptrCoreFloatArray, this);
    }

    /* access modifiers changed from: package-private */
    public long capacity() {
        return CoreJni.capacityInCoreFloatArray(this.agpCptrCoreFloatArray, this);
    }

    /* access modifiers changed from: package-private */
    public void reserve(long j) {
        CoreJni.reserveInCoreFloatArray(this.agpCptrCoreFloatArray, this, j);
    }

    /* access modifiers changed from: package-private */
    public boolean isEmpty() {
        return CoreJni.isEmptyInCoreFloatArray(this.agpCptrCoreFloatArray, this);
    }

    /* access modifiers changed from: package-private */
    public void clear() {
        CoreJni.clearInCoreFloatArray(this.agpCptrCoreFloatArray, this);
    }

    /* access modifiers changed from: package-private */
    public void add(float f) {
        CoreJni.addInCoreFloatArray(this.agpCptrCoreFloatArray, this, f);
    }

    /* access modifiers changed from: package-private */
    public float get(int i) {
        return CoreJni.getInCoreFloatArray(this.agpCptrCoreFloatArray, this, i);
    }

    /* access modifiers changed from: package-private */
    public void set(int i, float f) {
        CoreJni.setInCoreFloatArray(this.agpCptrCoreFloatArray, this, i, f);
    }
}
