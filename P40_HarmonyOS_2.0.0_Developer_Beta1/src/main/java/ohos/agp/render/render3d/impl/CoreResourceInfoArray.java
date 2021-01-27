package ohos.agp.render.render3d.impl;

import java.util.AbstractList;
import java.util.List;
import java.util.RandomAccess;
import ohos.agp.render.render3d.impl.CoreResourceManager;

/* access modifiers changed from: package-private */
public class CoreResourceInfoArray extends AbstractList<CoreResourceManager.CoreResourceInfo> implements RandomAccess {
    private transient long agpCptrCoreResourceInfoArray;
    private final Object delLock;
    transient boolean isAgpCmemOwn;

    CoreResourceInfoArray(long j, boolean z) {
        this.delLock = new Object();
        this.isAgpCmemOwn = z;
        this.agpCptrCoreResourceInfoArray = j;
    }

    static long getCptr(CoreResourceInfoArray coreResourceInfoArray) {
        if (coreResourceInfoArray == null) {
            return 0;
        }
        return coreResourceInfoArray.agpCptrCoreResourceInfoArray;
    }

    /* access modifiers changed from: protected */
    @Override // java.lang.Object
    public void finalize() {
        delete();
    }

    /* access modifiers changed from: package-private */
    public void delete() {
        synchronized (this.delLock) {
            if (this.agpCptrCoreResourceInfoArray != 0) {
                if (this.isAgpCmemOwn) {
                    this.isAgpCmemOwn = false;
                    CoreJni.deleteCoreResourceInfoArray(this.agpCptrCoreResourceInfoArray);
                }
                this.agpCptrCoreResourceInfoArray = 0;
            }
        }
    }

    static long getCptrAndSetMemOwn(CoreResourceInfoArray coreResourceInfoArray, boolean z) {
        if (coreResourceInfoArray != null) {
            synchronized (coreResourceInfoArray.delLock) {
                coreResourceInfoArray.isAgpCmemOwn = z;
            }
        }
        return getCptr(coreResourceInfoArray);
    }

    CoreResourceInfoArray(List<CoreResourceManager.CoreResourceInfo> list) {
        this();
        reserve((long) list.size());
        addAll(list);
    }

    CoreResourceInfoArray(Iterable<CoreResourceManager.CoreResourceInfo> iterable) {
        this();
        for (CoreResourceManager.CoreResourceInfo coreResourceInfo : iterable) {
            add(coreResourceInfo);
        }
    }

    @Override // java.util.AbstractList, java.util.List
    public CoreResourceManager.CoreResourceInfo get(int i) {
        return doGet(i);
    }

    public CoreResourceManager.CoreResourceInfo set(int i, CoreResourceManager.CoreResourceInfo coreResourceInfo) {
        return doSet(i, coreResourceInfo);
    }

    public boolean add(CoreResourceManager.CoreResourceInfo coreResourceInfo) {
        this.modCount++;
        doAdd(coreResourceInfo);
        return true;
    }

    public void add(int i, CoreResourceManager.CoreResourceInfo coreResourceInfo) {
        this.modCount++;
        doAdd(i, coreResourceInfo);
    }

    @Override // java.util.AbstractList, java.util.List
    public CoreResourceManager.CoreResourceInfo remove(int i) {
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

    CoreResourceInfoArray() {
        this(CoreJni.newCoreResourceInfoArray0(), true);
    }

    CoreResourceInfoArray(CoreResourceInfoArray coreResourceInfoArray) {
        this(CoreJni.newCoreResourceInfoArray1(getCptr(coreResourceInfoArray), coreResourceInfoArray), true);
    }

    /* access modifiers changed from: package-private */
    public long capacity() {
        return CoreJni.capacityInCoreResourceInfoArray(this.agpCptrCoreResourceInfoArray, this);
    }

    /* access modifiers changed from: package-private */
    public void reserve(long j) {
        CoreJni.reserveInCoreResourceInfoArray(this.agpCptrCoreResourceInfoArray, this, j);
    }

    @Override // java.util.AbstractCollection, java.util.List, java.util.Collection
    public boolean isEmpty() {
        return CoreJni.isEmptyInCoreResourceInfoArray(this.agpCptrCoreResourceInfoArray, this);
    }

    @Override // java.util.AbstractList, java.util.AbstractCollection, java.util.List, java.util.Collection
    public void clear() {
        CoreJni.clearInCoreResourceInfoArray(this.agpCptrCoreResourceInfoArray, this);
    }

    CoreResourceInfoArray(int i, CoreResourceManager.CoreResourceInfo coreResourceInfo) {
        this(CoreJni.newCoreResourceInfoArray2(i, CoreResourceManager.CoreResourceInfo.getCptr(coreResourceInfo), coreResourceInfo), true);
    }

    private int doSize() {
        return CoreJni.doSizeInCoreResourceInfoArray(this.agpCptrCoreResourceInfoArray, this);
    }

    private void doAdd(CoreResourceManager.CoreResourceInfo coreResourceInfo) {
        CoreJni.doAddInCoreResourceInfoArray0(this.agpCptrCoreResourceInfoArray, this, CoreResourceManager.CoreResourceInfo.getCptr(coreResourceInfo), coreResourceInfo);
    }

    private void doAdd(int i, CoreResourceManager.CoreResourceInfo coreResourceInfo) {
        CoreJni.doAddInCoreResourceInfoArray1(this.agpCptrCoreResourceInfoArray, this, i, CoreResourceManager.CoreResourceInfo.getCptr(coreResourceInfo), coreResourceInfo);
    }

    private CoreResourceManager.CoreResourceInfo doRemove(int i) {
        return new CoreResourceManager.CoreResourceInfo(CoreJni.doRemoveInCoreResourceInfoArray(this.agpCptrCoreResourceInfoArray, this, i), true);
    }

    private CoreResourceManager.CoreResourceInfo doGet(int i) {
        return new CoreResourceManager.CoreResourceInfo(CoreJni.doGetInCoreResourceInfoArray(this.agpCptrCoreResourceInfoArray, this, i), false);
    }

    private CoreResourceManager.CoreResourceInfo doSet(int i, CoreResourceManager.CoreResourceInfo coreResourceInfo) {
        return new CoreResourceManager.CoreResourceInfo(CoreJni.doSetInCoreResourceInfoArray(this.agpCptrCoreResourceInfoArray, this, i, CoreResourceManager.CoreResourceInfo.getCptr(coreResourceInfo), coreResourceInfo), true);
    }

    private void doRemoveRange(int i, int i2) {
        CoreJni.doRemoveRangeInCoreResourceInfoArray(this.agpCptrCoreResourceInfoArray, this, i, i2);
    }
}
