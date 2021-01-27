package com.huawei.agpengine.impl;

import com.huawei.agpengine.impl.CoreResourceManager;

/* access modifiers changed from: package-private */
public class CoreResourceInfoArray {
    private transient long agpCptr;
    transient boolean isAgpCmemOwn;

    CoreResourceInfoArray(long cptr, boolean isCmemoryOwn) {
        this.isAgpCmemOwn = isCmemoryOwn;
        this.agpCptr = cptr;
    }

    static long getCptr(CoreResourceInfoArray obj) {
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
                CoreJni.deleteCoreResourceInfoArray(this.agpCptr);
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
    public CoreResourceManager.CoreResourceInfo get(int index) {
        if (index >= 0 && index < doSize()) {
            return doGet(index);
        }
        throw new IndexOutOfBoundsException();
    }

    /* access modifiers changed from: package-private */
    public CoreResourceManager.CoreResourceInfo set(int index, CoreResourceManager.CoreResourceInfo e) {
        if (index >= 0 && index < doSize()) {
            return doSet(index, e);
        }
        throw new IndexOutOfBoundsException();
    }

    /* access modifiers changed from: package-private */
    public boolean add(CoreResourceManager.CoreResourceInfo e) {
        if (e != null) {
            doAdd(e);
            return true;
        }
        throw new NullPointerException();
    }

    /* access modifiers changed from: package-private */
    public void add(int index, CoreResourceManager.CoreResourceInfo e) {
        if (index < 0 || index > doSize()) {
            throw new IndexOutOfBoundsException();
        } else if (e != null) {
            doAdd(index, e);
        } else {
            throw new NullPointerException();
        }
    }

    /* access modifiers changed from: package-private */
    public CoreResourceManager.CoreResourceInfo remove(int index) {
        if (index >= 0 && index < doSize()) {
            return doRemove(index);
        }
        throw new IndexOutOfBoundsException();
    }

    /* access modifiers changed from: package-private */
    public int size() {
        return doSize();
    }

    CoreResourceInfoArray() {
        this(CoreJni.newCoreResourceInfoArray0(), true);
    }

    CoreResourceInfoArray(CoreResourceInfoArray other) {
        this(CoreJni.newCoreResourceInfoArray1(getCptr(other), other), true);
    }

    /* access modifiers changed from: package-private */
    public long capacity() {
        return CoreJni.capacityInCoreResourceInfoArray(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void reserve(long count) {
        CoreJni.reserveInCoreResourceInfoArray(this.agpCptr, this, count);
    }

    CoreResourceInfoArray(int count, CoreResourceManager.CoreResourceInfo value) {
        this(CoreJni.newCoreResourceInfoArray2(count, CoreResourceManager.CoreResourceInfo.getCptr(value), value), true);
    }

    private boolean doIsEmpty() {
        return CoreJni.doIsEmptyInCoreResourceInfoArray(this.agpCptr, this);
    }

    private void doClear() {
        CoreJni.doClearInCoreResourceInfoArray(this.agpCptr, this);
    }

    private int doSize() {
        return CoreJni.doSizeInCoreResourceInfoArray(this.agpCptr, this);
    }

    private void doAdd(CoreResourceManager.CoreResourceInfo x) {
        CoreJni.doAddInCoreResourceInfoArray0(this.agpCptr, this, CoreResourceManager.CoreResourceInfo.getCptr(x), x);
    }

    private void doAdd(int index, CoreResourceManager.CoreResourceInfo x) {
        CoreJni.doAddInCoreResourceInfoArray1(this.agpCptr, this, index, CoreResourceManager.CoreResourceInfo.getCptr(x), x);
    }

    private CoreResourceManager.CoreResourceInfo doRemove(int index) {
        return new CoreResourceManager.CoreResourceInfo(CoreJni.doRemoveInCoreResourceInfoArray(this.agpCptr, this, index), true);
    }

    private CoreResourceManager.CoreResourceInfo doGet(int index) {
        return new CoreResourceManager.CoreResourceInfo(CoreJni.doGetInCoreResourceInfoArray(this.agpCptr, this, index), false);
    }

    private CoreResourceManager.CoreResourceInfo doSet(int index, CoreResourceManager.CoreResourceInfo val) {
        return new CoreResourceManager.CoreResourceInfo(CoreJni.doSetInCoreResourceInfoArray(this.agpCptr, this, index, CoreResourceManager.CoreResourceInfo.getCptr(val), val), true);
    }
}
