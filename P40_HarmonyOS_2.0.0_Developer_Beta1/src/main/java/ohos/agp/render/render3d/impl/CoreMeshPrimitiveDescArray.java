package ohos.agp.render.render3d.impl;

class CoreMeshPrimitiveDescArray {
    private transient long agpCptrMeshPrimitiveDescArray;
    private final Object delLock;
    transient boolean isAgpCmemOwn;

    CoreMeshPrimitiveDescArray(long j, boolean z) {
        this.delLock = new Object();
        this.isAgpCmemOwn = z;
        this.agpCptrMeshPrimitiveDescArray = j;
    }

    static long getCptr(CoreMeshPrimitiveDescArray coreMeshPrimitiveDescArray) {
        if (coreMeshPrimitiveDescArray == null) {
            return 0;
        }
        return coreMeshPrimitiveDescArray.agpCptrMeshPrimitiveDescArray;
    }

    /* access modifiers changed from: protected */
    public void finalize() {
        delete();
    }

    /* access modifiers changed from: package-private */
    public void delete() {
        synchronized (this.delLock) {
            if (this.agpCptrMeshPrimitiveDescArray != 0) {
                if (this.isAgpCmemOwn) {
                    this.isAgpCmemOwn = false;
                    CoreJni.deleteCoreMeshPrimitiveDescArray(this.agpCptrMeshPrimitiveDescArray);
                }
                this.agpCptrMeshPrimitiveDescArray = 0;
            }
        }
    }

    static long getCptrAndSetMemOwn(CoreMeshPrimitiveDescArray coreMeshPrimitiveDescArray, boolean z) {
        if (coreMeshPrimitiveDescArray != null) {
            synchronized (coreMeshPrimitiveDescArray.delLock) {
                coreMeshPrimitiveDescArray.isAgpCmemOwn = z;
            }
        }
        return getCptr(coreMeshPrimitiveDescArray);
    }

    CoreMeshPrimitiveDescArray() {
        this(CoreJni.newCoreMeshPrimitiveDescArray0(), true);
    }

    CoreMeshPrimitiveDescArray(long j) {
        this(CoreJni.newCoreMeshPrimitiveDescArray1(j), true);
    }

    /* access modifiers changed from: package-private */
    public long size() {
        return CoreJni.sizeInCoreMeshPrimitiveDescArray(this.agpCptrMeshPrimitiveDescArray, this);
    }

    /* access modifiers changed from: package-private */
    public long capacity() {
        return CoreJni.capacityInCoreMeshPrimitiveDescArray(this.agpCptrMeshPrimitiveDescArray, this);
    }

    /* access modifiers changed from: package-private */
    public void reserve(long j) {
        CoreJni.reserveInCoreMeshPrimitiveDescArray(this.agpCptrMeshPrimitiveDescArray, this, j);
    }

    /* access modifiers changed from: package-private */
    public boolean isEmpty() {
        return CoreJni.isEmptyInCoreMeshPrimitiveDescArray(this.agpCptrMeshPrimitiveDescArray, this);
    }

    /* access modifiers changed from: package-private */
    public void clear() {
        CoreJni.clearInCoreMeshPrimitiveDescArray(this.agpCptrMeshPrimitiveDescArray, this);
    }

    /* access modifiers changed from: package-private */
    public void add(CoreMeshPrimitiveDesc coreMeshPrimitiveDesc) {
        CoreJni.addInCoreMeshPrimitiveDescArray(this.agpCptrMeshPrimitiveDescArray, this, CoreMeshPrimitiveDesc.getCptr(coreMeshPrimitiveDesc), coreMeshPrimitiveDesc);
    }

    /* access modifiers changed from: package-private */
    public CoreMeshPrimitiveDesc get(int i) {
        return new CoreMeshPrimitiveDesc(CoreJni.getInCoreMeshPrimitiveDescArray(this.agpCptrMeshPrimitiveDescArray, this, i), false);
    }

    /* access modifiers changed from: package-private */
    public void set(int i, CoreMeshPrimitiveDesc coreMeshPrimitiveDesc) {
        CoreJni.setInCoreMeshPrimitiveDescArray(this.agpCptrMeshPrimitiveDescArray, this, i, CoreMeshPrimitiveDesc.getCptr(coreMeshPrimitiveDesc), coreMeshPrimitiveDesc);
    }
}
