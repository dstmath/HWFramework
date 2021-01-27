package com.huawei.agpengine.impl;

/* access modifiers changed from: package-private */
public class CoreComponentManagerArray {
    private transient long agpCptr;
    transient boolean isAgpCmemOwn;

    CoreComponentManagerArray(long cptr, boolean isCmemoryOwn) {
        this.isAgpCmemOwn = isCmemoryOwn;
        this.agpCptr = cptr;
    }

    static long getCptr(CoreComponentManagerArray obj) {
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
                CoreJni.deleteCoreComponentManagerArray(this.agpCptr);
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
    public CoreComponentManager get(int index) {
        if (index >= 0 && index < doSize()) {
            return doGet(index);
        }
        throw new IndexOutOfBoundsException();
    }

    /* access modifiers changed from: package-private */
    public CoreComponentManager set(int index, CoreComponentManager e) {
        if (index >= 0 && index < doSize()) {
            return doSet(index, e);
        }
        throw new IndexOutOfBoundsException();
    }

    /* access modifiers changed from: package-private */
    public boolean add(CoreComponentManager e) {
        if (e != null) {
            doAdd(e);
            return true;
        }
        throw new NullPointerException();
    }

    /* access modifiers changed from: package-private */
    public void add(int index, CoreComponentManager e) {
        if (index < 0 || index > doSize()) {
            throw new IndexOutOfBoundsException();
        } else if (e != null) {
            doAdd(index, e);
        } else {
            throw new NullPointerException();
        }
    }

    /* access modifiers changed from: package-private */
    public CoreComponentManager remove(int index) {
        if (index >= 0 && index < doSize()) {
            return doRemove(index);
        }
        throw new IndexOutOfBoundsException();
    }

    /* access modifiers changed from: package-private */
    public int size() {
        return doSize();
    }

    CoreComponentManagerArray() {
        this(CoreJni.newCoreComponentManagerArray0(), true);
    }

    CoreComponentManagerArray(CoreComponentManagerArray other) {
        this(CoreJni.newCoreComponentManagerArray1(getCptr(other), other), true);
    }

    /* access modifiers changed from: package-private */
    public long capacity() {
        return CoreJni.capacityInCoreComponentManagerArray(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void reserve(long count) {
        CoreJni.reserveInCoreComponentManagerArray(this.agpCptr, this, count);
    }

    CoreComponentManagerArray(int count, CoreComponentManager value) {
        this(CoreJni.newCoreComponentManagerArray2(count, CoreComponentManager.getCptr(value), value), true);
    }

    private boolean doIsEmpty() {
        return CoreJni.doIsEmptyInCoreComponentManagerArray(this.agpCptr, this);
    }

    private void doClear() {
        CoreJni.doClearInCoreComponentManagerArray(this.agpCptr, this);
    }

    private int doSize() {
        return CoreJni.doSizeInCoreComponentManagerArray(this.agpCptr, this);
    }

    private void doAdd(CoreComponentManager x) {
        CoreJni.doAddInCoreComponentManagerArray0(this.agpCptr, this, CoreComponentManager.getCptr(x), x);
    }

    private void doAdd(int index, CoreComponentManager x) {
        CoreJni.doAddInCoreComponentManagerArray1(this.agpCptr, this, index, CoreComponentManager.getCptr(x), x);
    }

    private CoreComponentManager doRemove(int index) {
        long cptr = CoreJni.doRemoveInCoreComponentManagerArray(this.agpCptr, this, index);
        if (cptr == 0) {
            return null;
        }
        return new CoreComponentManager(cptr, false);
    }

    private CoreComponentManager doGet(int index) {
        long cptr = CoreJni.doGetInCoreComponentManagerArray(this.agpCptr, this, index);
        if (cptr == 0) {
            return null;
        }
        return new CoreComponentManager(cptr, false);
    }

    private CoreComponentManager doSet(int index, CoreComponentManager val) {
        long cptr = CoreJni.doSetInCoreComponentManagerArray(this.agpCptr, this, index, CoreComponentManager.getCptr(val), val);
        if (cptr == 0) {
            return null;
        }
        return new CoreComponentManager(cptr, false);
    }
}
