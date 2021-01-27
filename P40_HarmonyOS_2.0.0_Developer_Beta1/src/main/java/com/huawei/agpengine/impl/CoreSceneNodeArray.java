package com.huawei.agpengine.impl;

/* access modifiers changed from: package-private */
public class CoreSceneNodeArray {
    private transient long agpCptr;
    transient boolean isAgpCmemOwn;

    CoreSceneNodeArray(long cptr, boolean isCmemoryOwn) {
        this.isAgpCmemOwn = isCmemoryOwn;
        this.agpCptr = cptr;
    }

    static long getCptr(CoreSceneNodeArray obj) {
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
                CoreJni.deleteCoreSceneNodeArray(this.agpCptr);
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
    public CoreSceneNode get(int index) {
        if (index >= 0 && index < doSize()) {
            return doGet(index);
        }
        throw new IndexOutOfBoundsException();
    }

    /* access modifiers changed from: package-private */
    public CoreSceneNode set(int index, CoreSceneNode e) {
        if (index >= 0 && index < doSize()) {
            return doSet(index, e);
        }
        throw new IndexOutOfBoundsException();
    }

    /* access modifiers changed from: package-private */
    public boolean add(CoreSceneNode e) {
        if (e != null) {
            doAdd(e);
            return true;
        }
        throw new NullPointerException();
    }

    /* access modifiers changed from: package-private */
    public void add(int index, CoreSceneNode e) {
        if (index < 0 || index > doSize()) {
            throw new IndexOutOfBoundsException();
        } else if (e != null) {
            doAdd(index, e);
        } else {
            throw new NullPointerException();
        }
    }

    /* access modifiers changed from: package-private */
    public CoreSceneNode remove(int index) {
        if (index >= 0 && index < doSize()) {
            return doRemove(index);
        }
        throw new IndexOutOfBoundsException();
    }

    /* access modifiers changed from: package-private */
    public int size() {
        return doSize();
    }

    CoreSceneNodeArray() {
        this(CoreJni.newCoreSceneNodeArray0(), true);
    }

    CoreSceneNodeArray(CoreSceneNodeArray other) {
        this(CoreJni.newCoreSceneNodeArray1(getCptr(other), other), true);
    }

    /* access modifiers changed from: package-private */
    public long capacity() {
        return CoreJni.capacityInCoreSceneNodeArray(this.agpCptr, this);
    }

    /* access modifiers changed from: package-private */
    public void reserve(long count) {
        CoreJni.reserveInCoreSceneNodeArray(this.agpCptr, this, count);
    }

    CoreSceneNodeArray(int count, CoreSceneNode value) {
        this(CoreJni.newCoreSceneNodeArray2(count, CoreSceneNode.getCptr(value), value), true);
    }

    private boolean doIsEmpty() {
        return CoreJni.doIsEmptyInCoreSceneNodeArray(this.agpCptr, this);
    }

    private void doClear() {
        CoreJni.doClearInCoreSceneNodeArray(this.agpCptr, this);
    }

    private int doSize() {
        return CoreJni.doSizeInCoreSceneNodeArray(this.agpCptr, this);
    }

    private void doAdd(CoreSceneNode x) {
        CoreJni.doAddInCoreSceneNodeArray0(this.agpCptr, this, CoreSceneNode.getCptr(x), x);
    }

    private void doAdd(int index, CoreSceneNode x) {
        CoreJni.doAddInCoreSceneNodeArray1(this.agpCptr, this, index, CoreSceneNode.getCptr(x), x);
    }

    private CoreSceneNode doRemove(int index) {
        long cptr = CoreJni.doRemoveInCoreSceneNodeArray(this.agpCptr, this, index);
        if (cptr == 0) {
            return null;
        }
        return new CoreSceneNode(cptr, false);
    }

    private CoreSceneNode doGet(int index) {
        long cptr = CoreJni.doGetInCoreSceneNodeArray(this.agpCptr, this, index);
        if (cptr == 0) {
            return null;
        }
        return new CoreSceneNode(cptr, false);
    }

    private CoreSceneNode doSet(int index, CoreSceneNode val) {
        long cptr = CoreJni.doSetInCoreSceneNodeArray(this.agpCptr, this, index, CoreSceneNode.getCptr(val), val);
        if (cptr == 0) {
            return null;
        }
        return new CoreSceneNode(cptr, false);
    }
}
