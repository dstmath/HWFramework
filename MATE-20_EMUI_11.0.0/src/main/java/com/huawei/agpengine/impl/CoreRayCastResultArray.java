package com.huawei.agpengine.impl;

class CoreRayCastResultArray {
    private transient long agpCptr;
    transient boolean isAgpCmemOwn;

    CoreRayCastResultArray(long cptr, boolean isCmemoryOwn) {
        this.isAgpCmemOwn = isCmemoryOwn;
        this.agpCptr = cptr;
    }

    static long getCptr(CoreRayCastResultArray obj) {
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
                CoreJni.deleteCoreRayCastResultArray(this.agpCptr);
            }
            this.agpCptr = 0;
        }
    }

    /* access modifiers changed from: package-private */
    public void clear() {
        doClear();
    }

    /* access modifiers changed from: package-private */
    public boolean isEmpty() {
        return doIsEmpty();
    }

    /* access modifiers changed from: package-private */
    public CoreRayCastResult get(int index) {
        if (index >= 0 && index < doSize()) {
            return doGet(index);
        }
        throw new IndexOutOfBoundsException();
    }

    /* access modifiers changed from: package-private */
    public CoreRayCastResult set(int index, CoreRayCastResult e) {
        if (index >= 0 && index < doSize()) {
            return doSet(index, e);
        }
        throw new IndexOutOfBoundsException();
    }

    /* access modifiers changed from: package-private */
    public boolean add(CoreRayCastResult e) {
        if (e != null) {
            doAdd(e);
            return true;
        }
        throw new NullPointerException();
    }

    /* access modifiers changed from: package-private */
    public void add(int index, CoreRayCastResult e) {
        if (index < 0 || index > doSize()) {
            throw new IndexOutOfBoundsException();
        } else if (e != null) {
            doAdd(index, e);
        } else {
            throw new NullPointerException();
        }
    }

    /* access modifiers changed from: package-private */
    public CoreRayCastResult remove(int index) {
        if (index >= 0 && index < doSize()) {
            return doRemove(index);
        }
        throw new IndexOutOfBoundsException();
    }

    /* access modifiers changed from: package-private */
    public int size() {
        return doSize();
    }

    CoreRayCastResultArray() {
        this(CoreJni.newCoreRayCastResultArray0(), true);
    }

    CoreRayCastResultArray(CoreRayCastResultArray other) {
        this(CoreJni.newCoreRayCastResultArray1(getCptr(other), other), true);
    }

    /* access modifiers changed from: package-private */
    public long capacity() {
        return CoreJni.capacityInCoreRayCastResultArray(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void reserve(long count) {
        CoreJni.reserveInCoreRayCastResultArray(this.agpCptr, this, count);
    }

    CoreRayCastResultArray(int count, CoreRayCastResult value) {
        this(CoreJni.newCoreRayCastResultArray2(count, CoreRayCastResult.getCptr(value), value), true);
    }

    private boolean doIsEmpty() {
        return CoreJni.doIsEmptyInCoreRayCastResultArray(this.agpCptr, this);
    }

    private void doClear() {
        CoreJni.doClearInCoreRayCastResultArray(this.agpCptr, this);
    }

    private int doSize() {
        return CoreJni.doSizeInCoreRayCastResultArray(this.agpCptr, this);
    }

    private void doAdd(CoreRayCastResult x) {
        CoreJni.doAddInCoreRayCastResultArray0(this.agpCptr, this, CoreRayCastResult.getCptr(x), x);
    }

    private void doAdd(int index, CoreRayCastResult x) {
        CoreJni.doAddInCoreRayCastResultArray1(this.agpCptr, this, index, CoreRayCastResult.getCptr(x), x);
    }

    private CoreRayCastResult doRemove(int index) {
        return new CoreRayCastResult(CoreJni.doRemoveInCoreRayCastResultArray(this.agpCptr, this, index), true);
    }

    private CoreRayCastResult doGet(int index) {
        return new CoreRayCastResult(CoreJni.doGetInCoreRayCastResultArray(this.agpCptr, this, index), false);
    }

    private CoreRayCastResult doSet(int index, CoreRayCastResult val) {
        return new CoreRayCastResult(CoreJni.doSetInCoreRayCastResultArray(this.agpCptr, this, index, CoreRayCastResult.getCptr(val), val), true);
    }
}
