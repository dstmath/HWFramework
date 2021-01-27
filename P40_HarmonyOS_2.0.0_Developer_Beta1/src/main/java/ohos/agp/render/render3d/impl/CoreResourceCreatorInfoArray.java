package ohos.agp.render.render3d.impl;

import java.util.AbstractList;
import java.util.List;
import java.util.RandomAccess;
import ohos.agp.render.render3d.impl.CoreResourceCreator;

/* access modifiers changed from: package-private */
public class CoreResourceCreatorInfoArray extends AbstractList<CoreResourceCreator.CoreInfo> implements RandomAccess {
    private transient long agpCptrCoreResourceCreatorInfoArray;
    private final Object delLock;
    transient boolean isAgpCmemOwn;

    CoreResourceCreatorInfoArray(long j, boolean z) {
        this.delLock = new Object();
        this.isAgpCmemOwn = z;
        this.agpCptrCoreResourceCreatorInfoArray = j;
    }

    static long getCptr(CoreResourceCreatorInfoArray coreResourceCreatorInfoArray) {
        if (coreResourceCreatorInfoArray == null) {
            return 0;
        }
        return coreResourceCreatorInfoArray.agpCptrCoreResourceCreatorInfoArray;
    }

    /* access modifiers changed from: protected */
    @Override // java.lang.Object
    public void finalize() {
        delete();
    }

    /* access modifiers changed from: package-private */
    public void delete() {
        synchronized (this.delLock) {
            if (this.agpCptrCoreResourceCreatorInfoArray != 0) {
                if (this.isAgpCmemOwn) {
                    this.isAgpCmemOwn = false;
                    CoreJni.deleteCoreResourceCreatorInfoArray(this.agpCptrCoreResourceCreatorInfoArray);
                }
                this.agpCptrCoreResourceCreatorInfoArray = 0;
            }
        }
    }

    static long getCptrAndSetMemOwn(CoreResourceCreatorInfoArray coreResourceCreatorInfoArray, boolean z) {
        if (coreResourceCreatorInfoArray != null) {
            synchronized (coreResourceCreatorInfoArray.delLock) {
                coreResourceCreatorInfoArray.isAgpCmemOwn = z;
            }
        }
        return getCptr(coreResourceCreatorInfoArray);
    }

    public CoreResourceCreatorInfoArray(List<CoreResourceCreator.CoreInfo> list) {
        this();
        reserve((long) list.size());
        addAll(list);
    }

    CoreResourceCreatorInfoArray(Iterable<CoreResourceCreator.CoreInfo> iterable) {
        this();
        for (CoreResourceCreator.CoreInfo coreInfo : iterable) {
            add(coreInfo);
        }
    }

    @Override // java.util.AbstractList, java.util.List
    public CoreResourceCreator.CoreInfo get(int i) {
        return doGet(i);
    }

    public CoreResourceCreator.CoreInfo set(int i, CoreResourceCreator.CoreInfo coreInfo) {
        return doSet(i, coreInfo);
    }

    public boolean add(CoreResourceCreator.CoreInfo coreInfo) {
        this.modCount++;
        doAdd(coreInfo);
        return true;
    }

    public void add(int i, CoreResourceCreator.CoreInfo coreInfo) {
        this.modCount++;
        doAdd(i, coreInfo);
    }

    @Override // java.util.AbstractList, java.util.List
    public CoreResourceCreator.CoreInfo remove(int i) {
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

    CoreResourceCreatorInfoArray() {
        this(CoreJni.newCoreResourceCreatorInfoArray0(), true);
    }

    CoreResourceCreatorInfoArray(CoreResourceCreatorInfoArray coreResourceCreatorInfoArray) {
        this(CoreJni.newCoreResourceCreatorInfoArray1(getCptr(coreResourceCreatorInfoArray), coreResourceCreatorInfoArray), true);
    }

    /* access modifiers changed from: package-private */
    public long capacity() {
        return CoreJni.capacityInCoreResourceCreatorInfoArray(this.agpCptrCoreResourceCreatorInfoArray, this);
    }

    /* access modifiers changed from: package-private */
    public void reserve(long j) {
        CoreJni.reserveInCoreResourceCreatorInfoArray(this.agpCptrCoreResourceCreatorInfoArray, this, j);
    }

    @Override // java.util.AbstractCollection, java.util.List, java.util.Collection
    public boolean isEmpty() {
        return CoreJni.isEmptyInCoreResourceCreatorInfoArray(this.agpCptrCoreResourceCreatorInfoArray, this);
    }

    @Override // java.util.AbstractList, java.util.AbstractCollection, java.util.List, java.util.Collection
    public void clear() {
        CoreJni.clearInCoreResourceCreatorInfoArray(this.agpCptrCoreResourceCreatorInfoArray, this);
    }

    CoreResourceCreatorInfoArray(int i, CoreResourceCreator.CoreInfo coreInfo) {
        this(CoreJni.newCoreResourceCreatorInfoArray2(i, CoreResourceCreator.CoreInfo.getCptr(coreInfo), coreInfo), true);
    }

    private int doSize() {
        return CoreJni.doSizeInCoreResourceCreatorInfoArray(this.agpCptrCoreResourceCreatorInfoArray, this);
    }

    private void doAdd(CoreResourceCreator.CoreInfo coreInfo) {
        CoreJni.doAddInCoreResourceCreatorInfoArray0(this.agpCptrCoreResourceCreatorInfoArray, this, CoreResourceCreator.CoreInfo.getCptr(coreInfo), coreInfo);
    }

    private void doAdd(int i, CoreResourceCreator.CoreInfo coreInfo) {
        CoreJni.doAddInCoreResourceCreatorInfoArray1(this.agpCptrCoreResourceCreatorInfoArray, this, i, CoreResourceCreator.CoreInfo.getCptr(coreInfo), coreInfo);
    }

    private CoreResourceCreator.CoreInfo doRemove(int i) {
        return new CoreResourceCreator.CoreInfo(CoreJni.doRemoveInCoreResourceCreatorInfoArray(this.agpCptrCoreResourceCreatorInfoArray, this, i), true);
    }

    private CoreResourceCreator.CoreInfo doGet(int i) {
        return new CoreResourceCreator.CoreInfo(CoreJni.doGetInCoreResourceCreatorInfoArray(this.agpCptrCoreResourceCreatorInfoArray, this, i), false);
    }

    private CoreResourceCreator.CoreInfo doSet(int i, CoreResourceCreator.CoreInfo coreInfo) {
        return new CoreResourceCreator.CoreInfo(CoreJni.doSetInCoreResourceCreatorInfoArray(this.agpCptrCoreResourceCreatorInfoArray, this, i, CoreResourceCreator.CoreInfo.getCptr(coreInfo), coreInfo), true);
    }

    private void doRemoveRange(int i, int i2) {
        CoreJni.doRemoveRangeInCoreResourceCreatorInfoArray(this.agpCptrCoreResourceCreatorInfoArray, this, i, i2);
    }
}
