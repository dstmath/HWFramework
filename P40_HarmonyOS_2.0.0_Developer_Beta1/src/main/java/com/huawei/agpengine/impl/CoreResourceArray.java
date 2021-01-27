package com.huawei.agpengine.impl;

/* access modifiers changed from: package-private */
public class CoreResourceArray {
    private transient long agpCptr;
    transient boolean isAgpCmemOwn;

    CoreResourceArray(long cptr, boolean isCmemoryOwn) {
        this.isAgpCmemOwn = isCmemoryOwn;
        this.agpCptr = cptr;
    }

    static long getCptr(CoreResourceArray obj) {
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
                CoreJni.deleteCoreResourceArray(this.agpCptr);
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
    public CoreResourceHandle get(int index) {
        if (index >= 0 && index < doSize()) {
            return doGet(index);
        }
        throw new IndexOutOfBoundsException();
    }

    /* access modifiers changed from: package-private */
    public CoreResourceHandle set(int index, CoreResourceHandle e) {
        if (index >= 0 && index < doSize()) {
            return doSet(index, e);
        }
        throw new IndexOutOfBoundsException();
    }

    /* access modifiers changed from: package-private */
    public boolean add(CoreResourceHandle e) {
        if (e != null) {
            doAdd(e);
            return true;
        }
        throw new NullPointerException();
    }

    /* access modifiers changed from: package-private */
    public void add(int index, CoreResourceHandle e) {
        if (index < 0 || index > doSize()) {
            throw new IndexOutOfBoundsException();
        } else if (e != null) {
            doAdd(index, e);
        } else {
            throw new NullPointerException();
        }
    }

    /* access modifiers changed from: package-private */
    public CoreResourceHandle remove(int index) {
        if (index >= 0 && index < doSize()) {
            return doRemove(index);
        }
        throw new IndexOutOfBoundsException();
    }

    /* access modifiers changed from: package-private */
    public int size() {
        return doSize();
    }

    CoreResourceArray() {
        this(CoreJni.newCoreResourceArray0(), true);
    }

    CoreResourceArray(CoreResourceArray other) {
        this(CoreJni.newCoreResourceArray1(getCptr(other), other), true);
    }

    /* access modifiers changed from: package-private */
    public long capacity() {
        return CoreJni.capacityInCoreResourceArray(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void reserve(long count) {
        CoreJni.reserveInCoreResourceArray(this.agpCptr, this, count);
    }

    CoreResourceArray(int count, CoreResourceHandle value) {
        this(CoreJni.newCoreResourceArray2(count, CoreResourceHandle.getCptr(value), value), true);
    }

    private boolean doIsEmpty() {
        return CoreJni.doIsEmptyInCoreResourceArray(this.agpCptr, this);
    }

    private void doClear() {
        CoreJni.doClearInCoreResourceArray(this.agpCptr, this);
    }

    private int doSize() {
        return CoreJni.doSizeInCoreResourceArray(this.agpCptr, this);
    }

    private void doAdd(CoreResourceHandle x) {
        CoreJni.doAddInCoreResourceArray0(this.agpCptr, this, CoreResourceHandle.getCptr(x), x);
    }

    private void doAdd(int index, CoreResourceHandle x) {
        CoreJni.doAddInCoreResourceArray1(this.agpCptr, this, index, CoreResourceHandle.getCptr(x), x);
    }

    private CoreResourceHandle doRemove(int index) {
        return new CoreResourceHandle(CoreJni.doRemoveInCoreResourceArray(this.agpCptr, this, index), true);
    }

    private CoreResourceHandle doGet(int index) {
        return new CoreResourceHandle(CoreJni.doGetInCoreResourceArray(this.agpCptr, this, index), false);
    }

    private CoreResourceHandle doSet(int index, CoreResourceHandle val) {
        return new CoreResourceHandle(CoreJni.doSetInCoreResourceArray(this.agpCptr, this, index, CoreResourceHandle.getCptr(val), val), true);
    }
}
