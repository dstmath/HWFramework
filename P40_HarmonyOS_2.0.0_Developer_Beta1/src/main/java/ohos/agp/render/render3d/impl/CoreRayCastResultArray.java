package ohos.agp.render.render3d.impl;

import java.util.AbstractList;
import java.util.Arrays;
import java.util.RandomAccess;

/* access modifiers changed from: package-private */
public class CoreRayCastResultArray extends AbstractList<CoreRayCastResult> implements RandomAccess {
    private transient long agpCptrRayCastResultArray;
    private final Object delLock;
    transient boolean isAgpCmemOwn;

    CoreRayCastResultArray(long j, boolean z) {
        this.delLock = new Object();
        this.isAgpCmemOwn = z;
        this.agpCptrRayCastResultArray = j;
    }

    static long getCptr(CoreRayCastResultArray coreRayCastResultArray) {
        if (coreRayCastResultArray == null) {
            return 0;
        }
        return coreRayCastResultArray.agpCptrRayCastResultArray;
    }

    /* access modifiers changed from: protected */
    @Override // java.lang.Object
    public void finalize() {
        delete();
    }

    /* access modifiers changed from: package-private */
    public void delete() {
        synchronized (this.delLock) {
            if (this.agpCptrRayCastResultArray != 0) {
                if (this.isAgpCmemOwn) {
                    this.isAgpCmemOwn = false;
                    CoreJni.deleteCoreRayCastResultArray(this.agpCptrRayCastResultArray);
                }
                this.agpCptrRayCastResultArray = 0;
            }
        }
    }

    static long getCptrAndSetMemOwn(CoreRayCastResultArray coreRayCastResultArray, boolean z) {
        if (coreRayCastResultArray != null) {
            synchronized (coreRayCastResultArray.delLock) {
                coreRayCastResultArray.isAgpCmemOwn = z;
            }
        }
        return getCptr(coreRayCastResultArray);
    }

    CoreRayCastResultArray(Iterable<CoreRayCastResult> iterable) {
        this();
        for (CoreRayCastResult coreRayCastResult : iterable) {
            add(coreRayCastResult);
        }
    }

    CoreRayCastResultArray(CoreRayCastResult[] coreRayCastResultArr) {
        this();
        reserve((long) coreRayCastResultArr.length);
        addAll(Arrays.asList(coreRayCastResultArr));
    }

    @Override // java.util.AbstractList, java.util.List
    public CoreRayCastResult get(int i) {
        return doGet(i);
    }

    public CoreRayCastResult set(int i, CoreRayCastResult coreRayCastResult) {
        return doSet(i, coreRayCastResult);
    }

    public boolean add(CoreRayCastResult coreRayCastResult) {
        this.modCount++;
        doAdd(coreRayCastResult);
        return true;
    }

    public void add(int i, CoreRayCastResult coreRayCastResult) {
        this.modCount++;
        doAdd(i, coreRayCastResult);
    }

    @Override // java.util.AbstractList, java.util.List
    public CoreRayCastResult remove(int i) {
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

    CoreRayCastResultArray() {
        this(CoreJni.newCoreRayCastResultArray0(), true);
    }

    CoreRayCastResultArray(CoreRayCastResultArray coreRayCastResultArray) {
        this(CoreJni.newCoreRayCastResultArray1(getCptr(coreRayCastResultArray), coreRayCastResultArray), true);
    }

    /* access modifiers changed from: package-private */
    public long capacity() {
        return CoreJni.capacityInCoreRayCastResultArray(this.agpCptrRayCastResultArray, this);
    }

    /* access modifiers changed from: package-private */
    public void reserve(long j) {
        CoreJni.reserveInCoreRayCastResultArray(this.agpCptrRayCastResultArray, this, j);
    }

    @Override // java.util.AbstractCollection, java.util.List, java.util.Collection
    public boolean isEmpty() {
        return CoreJni.isEmptyInCoreRayCastResultArray(this.agpCptrRayCastResultArray, this);
    }

    @Override // java.util.AbstractList, java.util.AbstractCollection, java.util.List, java.util.Collection
    public void clear() {
        CoreJni.clearInCoreRayCastResultArray(this.agpCptrRayCastResultArray, this);
    }

    CoreRayCastResultArray(int i, CoreRayCastResult coreRayCastResult) {
        this(CoreJni.newCoreRayCastResultArray2(i, CoreRayCastResult.getCptr(coreRayCastResult), coreRayCastResult), true);
    }

    private int doSize() {
        return CoreJni.doSizeInCoreRayCastResultArray(this.agpCptrRayCastResultArray, this);
    }

    private void doAdd(CoreRayCastResult coreRayCastResult) {
        CoreJni.doAddInCoreRayCastResultArray0(this.agpCptrRayCastResultArray, this, CoreRayCastResult.getCptr(coreRayCastResult), coreRayCastResult);
    }

    private void doAdd(int i, CoreRayCastResult coreRayCastResult) {
        CoreJni.doAddInCoreRayCastResultArray1(this.agpCptrRayCastResultArray, this, i, CoreRayCastResult.getCptr(coreRayCastResult), coreRayCastResult);
    }

    private CoreRayCastResult doRemove(int i) {
        return new CoreRayCastResult(CoreJni.doRemoveInCoreRayCastResultArray(this.agpCptrRayCastResultArray, this, i), true);
    }

    private CoreRayCastResult doGet(int i) {
        return new CoreRayCastResult(CoreJni.doGetInCoreRayCastResultArray(this.agpCptrRayCastResultArray, this, i), false);
    }

    private CoreRayCastResult doSet(int i, CoreRayCastResult coreRayCastResult) {
        return new CoreRayCastResult(CoreJni.doSetInCoreRayCastResultArray(this.agpCptrRayCastResultArray, this, i, CoreRayCastResult.getCptr(coreRayCastResult), coreRayCastResult), true);
    }

    private void doRemoveRange(int i, int i2) {
        CoreJni.doRemoveRangeInCoreRayCastResultArray(this.agpCptrRayCastResultArray, this, i, i2);
    }
}
