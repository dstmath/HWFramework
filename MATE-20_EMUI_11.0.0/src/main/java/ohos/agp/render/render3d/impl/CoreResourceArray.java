package ohos.agp.render.render3d.impl;

import java.util.AbstractList;
import java.util.RandomAccess;

/* access modifiers changed from: package-private */
public class CoreResourceArray extends AbstractList<CoreResourceHandle> implements RandomAccess {
    private transient long agpCptrResourceArray;
    private final Object delLock;
    transient boolean isAgpCmemOwn;

    CoreResourceArray(long j, boolean z) {
        this.delLock = new Object();
        this.isAgpCmemOwn = z;
        this.agpCptrResourceArray = j;
    }

    static long getCptr(CoreResourceArray coreResourceArray) {
        if (coreResourceArray == null) {
            return 0;
        }
        return coreResourceArray.agpCptrResourceArray;
    }

    /* access modifiers changed from: protected */
    @Override // java.lang.Object
    public void finalize() {
        delete();
    }

    /* access modifiers changed from: package-private */
    public void delete() {
        synchronized (this.delLock) {
            if (this.agpCptrResourceArray != 0) {
                if (this.isAgpCmemOwn) {
                    this.isAgpCmemOwn = false;
                    CoreJni.deleteCoreResourceArray(this.agpCptrResourceArray);
                }
                this.agpCptrResourceArray = 0;
            }
        }
    }

    static long getCptrAndSetMemOwn(CoreResourceArray coreResourceArray, boolean z) {
        if (coreResourceArray != null) {
            synchronized (coreResourceArray.delLock) {
                coreResourceArray.isAgpCmemOwn = z;
            }
        }
        return getCptr(coreResourceArray);
    }

    CoreResourceArray(CoreResourceHandle[] coreResourceHandleArr) {
        this();
        reserve((long) coreResourceHandleArr.length);
        for (CoreResourceHandle coreResourceHandle : coreResourceHandleArr) {
            add(coreResourceHandle);
        }
    }

    CoreResourceArray(Iterable<CoreResourceHandle> iterable) {
        this();
        for (CoreResourceHandle coreResourceHandle : iterable) {
            add(coreResourceHandle);
        }
    }

    @Override // java.util.AbstractList, java.util.List
    public CoreResourceHandle get(int i) {
        return doGet(i);
    }

    public CoreResourceHandle set(int i, CoreResourceHandle coreResourceHandle) {
        return doSet(i, coreResourceHandle);
    }

    public boolean add(CoreResourceHandle coreResourceHandle) {
        this.modCount++;
        doAdd(coreResourceHandle);
        return true;
    }

    public void add(int i, CoreResourceHandle coreResourceHandle) {
        this.modCount++;
        doAdd(i, coreResourceHandle);
    }

    @Override // java.util.AbstractList, java.util.List
    public CoreResourceHandle remove(int i) {
        this.modCount++;
        return doRemove(i);
    }

    /* access modifiers changed from: protected */
    @Override // java.util.AbstractList
    public void removeRange(int i, int i2) {
        this.modCount++;
        doRemoveRange(i, i2);
    }

    @Override // java.util.AbstractCollection, java.util.List, java.util.Collection
    public int size() {
        return doSize();
    }

    CoreResourceArray() {
        this(CoreJni.newCoreResourceArray0(), true);
    }

    CoreResourceArray(CoreResourceArray coreResourceArray) {
        this(CoreJni.newCoreResourceArray1(getCptr(coreResourceArray), coreResourceArray), true);
    }

    /* access modifiers changed from: package-private */
    public long capacity() {
        return CoreJni.capacityInCoreResourceArray(this.agpCptrResourceArray, this);
    }

    /* access modifiers changed from: package-private */
    public void reserve(long j) {
        CoreJni.reserveInCoreResourceArray(this.agpCptrResourceArray, this, j);
    }

    @Override // java.util.AbstractCollection, java.util.List, java.util.Collection
    public boolean isEmpty() {
        return CoreJni.isEmptyInCoreResourceArray(this.agpCptrResourceArray, this);
    }

    @Override // java.util.AbstractList, java.util.AbstractCollection, java.util.List, java.util.Collection
    public void clear() {
        CoreJni.clearInCoreResourceArray(this.agpCptrResourceArray, this);
    }

    CoreResourceArray(int i, CoreResourceHandle coreResourceHandle) {
        this(CoreJni.newCoreResourceArray2(i, CoreResourceHandle.getCptr(coreResourceHandle), coreResourceHandle), true);
    }

    private int doSize() {
        return CoreJni.doSizeInCoreResourceArray(this.agpCptrResourceArray, this);
    }

    private void doAdd(CoreResourceHandle coreResourceHandle) {
        CoreJni.doAddInCoreResourceArray0(this.agpCptrResourceArray, this, CoreResourceHandle.getCptr(coreResourceHandle), coreResourceHandle);
    }

    private void doAdd(int i, CoreResourceHandle coreResourceHandle) {
        CoreJni.doAddInCoreResourceArray1(this.agpCptrResourceArray, this, i, CoreResourceHandle.getCptr(coreResourceHandle), coreResourceHandle);
    }

    private CoreResourceHandle doRemove(int i) {
        return new CoreResourceHandle(CoreJni.doRemoveInCoreResourceArray(this.agpCptrResourceArray, this, i), true);
    }

    private CoreResourceHandle doGet(int i) {
        return new CoreResourceHandle(CoreJni.doGetInCoreResourceArray(this.agpCptrResourceArray, this, i), false);
    }

    private CoreResourceHandle doSet(int i, CoreResourceHandle coreResourceHandle) {
        return new CoreResourceHandle(CoreJni.doSetInCoreResourceArray(this.agpCptrResourceArray, this, i, CoreResourceHandle.getCptr(coreResourceHandle), coreResourceHandle), true);
    }

    private void doRemoveRange(int i, int i2) {
        CoreJni.doRemoveRangeInCoreResourceArray(this.agpCptrResourceArray, this, i, i2);
    }
}
