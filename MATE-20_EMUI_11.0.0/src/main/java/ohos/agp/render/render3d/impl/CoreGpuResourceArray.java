package ohos.agp.render.render3d.impl;

import java.util.AbstractList;
import java.util.RandomAccess;

/* access modifiers changed from: package-private */
public class CoreGpuResourceArray extends AbstractList<CoreGpuResourceHandle> implements RandomAccess {
    private transient long agpCptrCoreGpuResourceArray;
    transient boolean isAgpCmemOwn;
    private final Object lock;

    CoreGpuResourceArray(long j, boolean z) {
        this.lock = new Object();
        this.isAgpCmemOwn = z;
        this.agpCptrCoreGpuResourceArray = j;
    }

    CoreGpuResourceArray(CoreGpuResourceHandle[] coreGpuResourceHandleArr) {
        this();
        reserve((long) coreGpuResourceHandleArr.length);
        for (CoreGpuResourceHandle coreGpuResourceHandle : coreGpuResourceHandleArr) {
            add(coreGpuResourceHandle);
        }
    }

    CoreGpuResourceArray(Iterable<CoreGpuResourceHandle> iterable) {
        this();
        for (CoreGpuResourceHandle coreGpuResourceHandle : iterable) {
            add(coreGpuResourceHandle);
        }
    }

    CoreGpuResourceArray() {
        this(CoreJni.newCoreGpuResourceArray0(), true);
    }

    CoreGpuResourceArray(CoreGpuResourceArray coreGpuResourceArray) {
        this(CoreJni.newCoreGpuResourceArray1(getCptr(coreGpuResourceArray), coreGpuResourceArray), true);
    }

    CoreGpuResourceArray(int i, CoreGpuResourceHandle coreGpuResourceHandle) {
        this(CoreJni.newCoreGpuResourceArray2(i, CoreGpuResourceHandle.getCptr(coreGpuResourceHandle), coreGpuResourceHandle), true);
    }

    static long getCptr(CoreGpuResourceArray coreGpuResourceArray) {
        if (coreGpuResourceArray == null) {
            return 0;
        }
        return coreGpuResourceArray.agpCptrCoreGpuResourceArray;
    }

    /* access modifiers changed from: protected */
    @Override // java.lang.Object
    public void finalize() {
        delete();
    }

    /* access modifiers changed from: package-private */
    public void delete() {
        synchronized (this.lock) {
            if (this.agpCptrCoreGpuResourceArray != 0) {
                if (this.isAgpCmemOwn) {
                    this.isAgpCmemOwn = false;
                    CoreJni.deleteCoreGpuResourceArray(this.agpCptrCoreGpuResourceArray);
                }
                this.agpCptrCoreGpuResourceArray = 0;
            }
        }
    }

    static long getCptrAndSetMemOwn(CoreGpuResourceArray coreGpuResourceArray, boolean z) {
        if (coreGpuResourceArray != null) {
            synchronized (coreGpuResourceArray.lock) {
                coreGpuResourceArray.isAgpCmemOwn = z;
            }
        }
        return getCptr(coreGpuResourceArray);
    }

    @Override // java.util.AbstractList, java.util.List
    public CoreGpuResourceHandle get(int i) {
        return doGet(i);
    }

    public CoreGpuResourceHandle set(int i, CoreGpuResourceHandle coreGpuResourceHandle) {
        return doSet(i, coreGpuResourceHandle);
    }

    /* access modifiers changed from: protected */
    @Override // java.util.AbstractList
    public void removeRange(int i, int i2) {
        this.modCount++;
        doRemoveRange(i, i2);
    }

    public boolean add(CoreGpuResourceHandle coreGpuResourceHandle) {
        this.modCount++;
        doAdd(coreGpuResourceHandle);
        return true;
    }

    public void add(int i, CoreGpuResourceHandle coreGpuResourceHandle) {
        this.modCount++;
        doAdd(i, coreGpuResourceHandle);
    }

    @Override // java.util.AbstractList, java.util.List
    public CoreGpuResourceHandle remove(int i) {
        this.modCount++;
        return doRemove(i);
    }

    @Override // java.util.AbstractCollection, java.util.List, java.util.Collection
    public int size() {
        return doSize();
    }

    /* access modifiers changed from: package-private */
    public long capacity() {
        return CoreJni.capacityInCoreGpuResourceArray(this.agpCptrCoreGpuResourceArray, this);
    }

    /* access modifiers changed from: package-private */
    public void reserve(long j) {
        CoreJni.reserveInCoreGpuResourceArray(this.agpCptrCoreGpuResourceArray, this, j);
    }

    @Override // java.util.AbstractCollection, java.util.List, java.util.Collection
    public boolean isEmpty() {
        return CoreJni.isEmptyInCoreGpuResourceArray(this.agpCptrCoreGpuResourceArray, this);
    }

    @Override // java.util.AbstractList, java.util.AbstractCollection, java.util.List, java.util.Collection
    public void clear() {
        CoreJni.clearInCoreGpuResourceArray(this.agpCptrCoreGpuResourceArray, this);
    }

    private int doSize() {
        return CoreJni.doSizeInCoreGpuResourceArray(this.agpCptrCoreGpuResourceArray, this);
    }

    private void doAdd(CoreGpuResourceHandle coreGpuResourceHandle) {
        CoreJni.doAddInCoreGpuResourceArray0(this.agpCptrCoreGpuResourceArray, this, CoreGpuResourceHandle.getCptr(coreGpuResourceHandle), coreGpuResourceHandle);
    }

    private void doAdd(int i, CoreGpuResourceHandle coreGpuResourceHandle) {
        CoreJni.doAddInCoreGpuResourceArray1(this.agpCptrCoreGpuResourceArray, this, i, CoreGpuResourceHandle.getCptr(coreGpuResourceHandle), coreGpuResourceHandle);
    }

    private CoreGpuResourceHandle doRemove(int i) {
        return new CoreGpuResourceHandle(CoreJni.doRemoveInCoreGpuResourceArray(this.agpCptrCoreGpuResourceArray, this, i), true);
    }

    private CoreGpuResourceHandle doGet(int i) {
        return new CoreGpuResourceHandle(CoreJni.doGetInCoreGpuResourceArray(this.agpCptrCoreGpuResourceArray, this, i), false);
    }

    private CoreGpuResourceHandle doSet(int i, CoreGpuResourceHandle coreGpuResourceHandle) {
        return new CoreGpuResourceHandle(CoreJni.doSetInCoreGpuResourceArray(this.agpCptrCoreGpuResourceArray, this, i, CoreGpuResourceHandle.getCptr(coreGpuResourceHandle), coreGpuResourceHandle), true);
    }

    private void doRemoveRange(int i, int i2) {
        CoreJni.doRemoveRangeInCoreGpuResourceArray(this.agpCptrCoreGpuResourceArray, this, i, i2);
    }
}
