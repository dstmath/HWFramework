package ohos.agp.render.render3d.impl;

class CoreComponentManagerArray {
    private transient long agpCptrCoreComponentManagerArray;
    transient boolean isAgpCmemOwn;
    private final Object lock;

    CoreComponentManagerArray(long j, boolean z) {
        this.lock = new Object();
        this.isAgpCmemOwn = z;
        this.agpCptrCoreComponentManagerArray = j;
    }

    static long getCptr(CoreComponentManagerArray coreComponentManagerArray) {
        if (coreComponentManagerArray == null) {
            return 0;
        }
        return coreComponentManagerArray.agpCptrCoreComponentManagerArray;
    }

    /* access modifiers changed from: protected */
    public void finalize() {
        delete();
    }

    /* access modifiers changed from: package-private */
    public void delete() {
        synchronized (this.lock) {
            if (this.agpCptrCoreComponentManagerArray != 0) {
                if (this.isAgpCmemOwn) {
                    this.isAgpCmemOwn = false;
                    CoreJni.deleteCoreComponentManagerArray(this.agpCptrCoreComponentManagerArray);
                }
                this.agpCptrCoreComponentManagerArray = 0;
            }
        }
    }

    static long getCptrAndSetMemOwn(CoreComponentManagerArray coreComponentManagerArray, boolean z) {
        if (coreComponentManagerArray != null) {
            synchronized (coreComponentManagerArray.lock) {
                coreComponentManagerArray.isAgpCmemOwn = z;
            }
        }
        return getCptr(coreComponentManagerArray);
    }

    CoreComponentManagerArray() {
        this(CoreJni.newCoreComponentManagerArray0(), true);
    }

    CoreComponentManagerArray(long j) {
        this(CoreJni.newCoreComponentManagerArray1(j), true);
    }

    /* access modifiers changed from: package-private */
    public long size() {
        return CoreJni.sizeInCoreComponentManagerArray(this.agpCptrCoreComponentManagerArray, this);
    }

    /* access modifiers changed from: package-private */
    public long capacity() {
        return CoreJni.capacityInCoreComponentManagerArray(this.agpCptrCoreComponentManagerArray, this);
    }

    /* access modifiers changed from: package-private */
    public void reserve(long j) {
        CoreJni.reserveInCoreComponentManagerArray(this.agpCptrCoreComponentManagerArray, this, j);
    }

    /* access modifiers changed from: package-private */
    public boolean isEmpty() {
        return CoreJni.isEmptyInCoreComponentManagerArray(this.agpCptrCoreComponentManagerArray, this);
    }

    /* access modifiers changed from: package-private */
    public void clear() {
        CoreJni.clearInCoreComponentManagerArray(this.agpCptrCoreComponentManagerArray, this);
    }

    /* access modifiers changed from: package-private */
    public void add(CoreComponentManager coreComponentManager) {
        CoreJni.addInCoreComponentManagerArray(this.agpCptrCoreComponentManagerArray, this, CoreComponentManager.getCptr(coreComponentManager), coreComponentManager);
    }

    /* access modifiers changed from: package-private */
    public CoreComponentManager get(int i) {
        long inCoreComponentManagerArray = CoreJni.getInCoreComponentManagerArray(this.agpCptrCoreComponentManagerArray, this, i);
        if (inCoreComponentManagerArray == 0) {
            return null;
        }
        return new CoreComponentManager(inCoreComponentManagerArray, false);
    }

    /* access modifiers changed from: package-private */
    public void set(int i, CoreComponentManager coreComponentManager) {
        CoreJni.setInCoreComponentManagerArray(this.agpCptrCoreComponentManagerArray, this, i, CoreComponentManager.getCptr(coreComponentManager), coreComponentManager);
    }
}
