package com.huawei.agpengine.impl;

/* access modifiers changed from: package-private */
public class CoreGpuResourceArray {
    private transient long agpCptr;
    transient boolean isAgpCmemOwn;

    CoreGpuResourceArray(long cptr, boolean isCmemoryOwn) {
        this.isAgpCmemOwn = isCmemoryOwn;
        this.agpCptr = cptr;
    }

    static long getCptr(CoreGpuResourceArray obj) {
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
                CoreJni.deleteCoreGpuResourceArray(this.agpCptr);
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
    public Long get(int index) {
        if (index >= 0 && index < doSize()) {
            return Long.valueOf(doGet(index));
        }
        throw new IndexOutOfBoundsException();
    }

    /* access modifiers changed from: package-private */
    public Long set(int index, Long e) {
        if (index >= 0 && index < doSize()) {
            return Long.valueOf(doSet(index, e.longValue()));
        }
        throw new IndexOutOfBoundsException();
    }

    /* access modifiers changed from: package-private */
    public boolean add(Long e) {
        if (e != null) {
            doAdd(e.longValue());
            return true;
        }
        throw new NullPointerException();
    }

    /* access modifiers changed from: package-private */
    public void add(int index, Long e) {
        if (index < 0 || index > doSize()) {
            throw new IndexOutOfBoundsException();
        } else if (e != null) {
            doAdd(index, e.longValue());
        } else {
            throw new NullPointerException();
        }
    }

    /* access modifiers changed from: package-private */
    public Long remove(int index) {
        if (index >= 0 && index < doSize()) {
            return Long.valueOf(doRemove(index));
        }
        throw new IndexOutOfBoundsException();
    }

    /* access modifiers changed from: package-private */
    public int size() {
        return doSize();
    }

    CoreGpuResourceArray() {
        this(CoreJni.newCoreGpuResourceArray0(), true);
    }

    CoreGpuResourceArray(CoreGpuResourceArray other) {
        this(CoreJni.newCoreGpuResourceArray1(getCptr(other), other), true);
    }

    /* access modifiers changed from: package-private */
    public long capacity() {
        return CoreJni.capacityInCoreGpuResourceArray(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void reserve(long count) {
        CoreJni.reserveInCoreGpuResourceArray(this.agpCptr, this, count);
    }

    CoreGpuResourceArray(int count, long value) {
        this(CoreJni.newCoreGpuResourceArray2(count, value), true);
    }

    private boolean doIsEmpty() {
        return CoreJni.doIsEmptyInCoreGpuResourceArray(this.agpCptr, this);
    }

    private void doClear() {
        CoreJni.doClearInCoreGpuResourceArray(this.agpCptr, this);
    }

    private int doSize() {
        return CoreJni.doSizeInCoreGpuResourceArray(this.agpCptr, this);
    }

    private void doAdd(long x) {
        CoreJni.doAddInCoreGpuResourceArray0(this.agpCptr, this, x);
    }

    private void doAdd(int index, long x) {
        CoreJni.doAddInCoreGpuResourceArray1(this.agpCptr, this, index, x);
    }

    private long doRemove(int index) {
        return CoreJni.doRemoveInCoreGpuResourceArray(this.agpCptr, this, index);
    }

    private long doGet(int index) {
        return CoreJni.doGetInCoreGpuResourceArray(this.agpCptr, this, index);
    }

    private long doSet(int index, long val) {
        return CoreJni.doSetInCoreGpuResourceArray(this.agpCptr, this, index, val);
    }
}
