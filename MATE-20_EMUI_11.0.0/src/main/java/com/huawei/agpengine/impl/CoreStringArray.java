package com.huawei.agpengine.impl;

/* access modifiers changed from: package-private */
public class CoreStringArray {
    private transient long agpCptr;
    transient boolean isAgpCmemOwn;

    CoreStringArray(long cptr, boolean isCmemoryOwn) {
        this.isAgpCmemOwn = isCmemoryOwn;
        this.agpCptr = cptr;
    }

    static long getCptr(CoreStringArray obj) {
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
                CoreJni.deleteCoreStringArray(this.agpCptr);
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
    public String get(int index) {
        if (index >= 0 && index < doSize()) {
            return doGet(index);
        }
        throw new IndexOutOfBoundsException();
    }

    /* access modifiers changed from: package-private */
    public String set(int index, String e) {
        if (index >= 0 && index < doSize()) {
            return doSet(index, e);
        }
        throw new IndexOutOfBoundsException();
    }

    /* access modifiers changed from: package-private */
    public boolean add(String e) {
        if (e != null) {
            doAdd(e);
            return true;
        }
        throw new NullPointerException();
    }

    /* access modifiers changed from: package-private */
    public void add(int index, String e) {
        if (index < 0 || index > doSize()) {
            throw new IndexOutOfBoundsException();
        } else if (e != null) {
            doAdd(index, e);
        } else {
            throw new NullPointerException();
        }
    }

    /* access modifiers changed from: package-private */
    public String remove(int index) {
        if (index >= 0 && index < doSize()) {
            return doRemove(index);
        }
        throw new IndexOutOfBoundsException();
    }

    /* access modifiers changed from: package-private */
    public int size() {
        return doSize();
    }

    CoreStringArray() {
        this(CoreJni.newCoreStringArray0(), true);
    }

    CoreStringArray(CoreStringArray other) {
        this(CoreJni.newCoreStringArray1(getCptr(other), other), true);
    }

    /* access modifiers changed from: package-private */
    public long capacity() {
        return CoreJni.capacityInCoreStringArray(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void reserve(long count) {
        CoreJni.reserveInCoreStringArray(this.agpCptr, this, count);
    }

    CoreStringArray(int count, String value) {
        this(CoreJni.newCoreStringArray2(count, value), true);
    }

    private boolean doIsEmpty() {
        return CoreJni.doIsEmptyInCoreStringArray(this.agpCptr, this);
    }

    private void doClear() {
        CoreJni.doClearInCoreStringArray(this.agpCptr, this);
    }

    private int doSize() {
        return CoreJni.doSizeInCoreStringArray(this.agpCptr, this);
    }

    private void doAdd(String x) {
        CoreJni.doAddInCoreStringArray0(this.agpCptr, this, x);
    }

    private void doAdd(int index, String x) {
        CoreJni.doAddInCoreStringArray1(this.agpCptr, this, index, x);
    }

    private String doRemove(int index) {
        return CoreJni.doRemoveInCoreStringArray(this.agpCptr, this, index);
    }

    private String doGet(int index) {
        return CoreJni.doGetInCoreStringArray(this.agpCptr, this, index);
    }

    private String doSet(int index, String val) {
        return CoreJni.doSetInCoreStringArray(this.agpCptr, this, index, val);
    }
}
