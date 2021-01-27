package com.huawei.agpengine.impl;

import com.huawei.agpengine.impl.CoreResourceCreator;

/* access modifiers changed from: package-private */
public class CoreResourceCreatorInfoArray {
    private transient long agpCptr;
    transient boolean isAgpCmemOwn;

    CoreResourceCreatorInfoArray(long cptr, boolean isCmemoryOwn) {
        this.isAgpCmemOwn = isCmemoryOwn;
        this.agpCptr = cptr;
    }

    static long getCptr(CoreResourceCreatorInfoArray obj) {
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
                CoreJni.deleteCoreResourceCreatorInfoArray(this.agpCptr);
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
    public CoreResourceCreator.CoreInfo get(int index) {
        if (index >= 0 && index < doSize()) {
            return doGet(index);
        }
        throw new IndexOutOfBoundsException();
    }

    /* access modifiers changed from: package-private */
    public CoreResourceCreator.CoreInfo set(int index, CoreResourceCreator.CoreInfo e) {
        if (index >= 0 && index < doSize()) {
            return doSet(index, e);
        }
        throw new IndexOutOfBoundsException();
    }

    /* access modifiers changed from: package-private */
    public boolean add(CoreResourceCreator.CoreInfo e) {
        if (e != null) {
            doAdd(e);
            return true;
        }
        throw new NullPointerException();
    }

    /* access modifiers changed from: package-private */
    public void add(int index, CoreResourceCreator.CoreInfo e) {
        if (index < 0 || index > doSize()) {
            throw new IndexOutOfBoundsException();
        } else if (e != null) {
            doAdd(index, e);
        } else {
            throw new NullPointerException();
        }
    }

    /* access modifiers changed from: package-private */
    public CoreResourceCreator.CoreInfo remove(int index) {
        if (index >= 0 && index < doSize()) {
            return doRemove(index);
        }
        throw new IndexOutOfBoundsException();
    }

    /* access modifiers changed from: package-private */
    public int size() {
        return doSize();
    }

    CoreResourceCreatorInfoArray() {
        this(CoreJni.newCoreResourceCreatorInfoArray0(), true);
    }

    CoreResourceCreatorInfoArray(CoreResourceCreatorInfoArray other) {
        this(CoreJni.newCoreResourceCreatorInfoArray1(getCptr(other), other), true);
    }

    /* access modifiers changed from: package-private */
    public long capacity() {
        return CoreJni.capacityInCoreResourceCreatorInfoArray(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void reserve(long count) {
        CoreJni.reserveInCoreResourceCreatorInfoArray(this.agpCptr, this, count);
    }

    CoreResourceCreatorInfoArray(int count, CoreResourceCreator.CoreInfo value) {
        this(CoreJni.newCoreResourceCreatorInfoArray2(count, CoreResourceCreator.CoreInfo.getCptr(value), value), true);
    }

    private boolean doIsEmpty() {
        return CoreJni.doIsEmptyInCoreResourceCreatorInfoArray(this.agpCptr, this);
    }

    private void doClear() {
        CoreJni.doClearInCoreResourceCreatorInfoArray(this.agpCptr, this);
    }

    private int doSize() {
        return CoreJni.doSizeInCoreResourceCreatorInfoArray(this.agpCptr, this);
    }

    private void doAdd(CoreResourceCreator.CoreInfo x) {
        CoreJni.doAddInCoreResourceCreatorInfoArray0(this.agpCptr, this, CoreResourceCreator.CoreInfo.getCptr(x), x);
    }

    private void doAdd(int index, CoreResourceCreator.CoreInfo x) {
        CoreJni.doAddInCoreResourceCreatorInfoArray1(this.agpCptr, this, index, CoreResourceCreator.CoreInfo.getCptr(x), x);
    }

    private CoreResourceCreator.CoreInfo doRemove(int index) {
        return new CoreResourceCreator.CoreInfo(CoreJni.doRemoveInCoreResourceCreatorInfoArray(this.agpCptr, this, index), true);
    }

    private CoreResourceCreator.CoreInfo doGet(int index) {
        return new CoreResourceCreator.CoreInfo(CoreJni.doGetInCoreResourceCreatorInfoArray(this.agpCptr, this, index), false);
    }

    private CoreResourceCreator.CoreInfo doSet(int index, CoreResourceCreator.CoreInfo val) {
        return new CoreResourceCreator.CoreInfo(CoreJni.doSetInCoreResourceCreatorInfoArray(this.agpCptr, this, index, CoreResourceCreator.CoreInfo.getCptr(val), val), true);
    }
}
