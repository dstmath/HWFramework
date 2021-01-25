package ohos.agp.render.render3d.impl;

import java.util.AbstractList;
import java.util.RandomAccess;

/* access modifiers changed from: package-private */
public class CoreStringArray extends AbstractList<String> implements RandomAccess {
    private transient long agpCptrCoreStringArray;
    private final Object delLock;
    transient boolean isAgpCmemOwn;

    CoreStringArray(long j, boolean z) {
        this.delLock = new Object();
        this.isAgpCmemOwn = z;
        this.agpCptrCoreStringArray = j;
    }

    CoreStringArray(String[] strArr) {
        this();
        reserve((long) strArr.length);
        for (String str : strArr) {
            add(str);
        }
    }

    CoreStringArray(Iterable<String> iterable) {
        this();
        for (String str : iterable) {
            add(str);
        }
    }

    CoreStringArray() {
        this(CoreJni.newCoreStringArray0(), true);
    }

    CoreStringArray(CoreStringArray coreStringArray) {
        this(CoreJni.newCoreStringArray1(getCptr(coreStringArray), coreStringArray), true);
    }

    CoreStringArray(int i, String str) {
        this(CoreJni.newCoreStringArray2(i, str), true);
    }

    static long getCptr(CoreStringArray coreStringArray) {
        if (coreStringArray == null) {
            return 0;
        }
        return coreStringArray.agpCptrCoreStringArray;
    }

    /* access modifiers changed from: protected */
    @Override // java.lang.Object
    public void finalize() {
        delete();
    }

    /* access modifiers changed from: package-private */
    public void delete() {
        synchronized (this.delLock) {
            if (this.agpCptrCoreStringArray != 0) {
                if (this.isAgpCmemOwn) {
                    this.isAgpCmemOwn = false;
                    CoreJni.deleteCoreStringArray(this.agpCptrCoreStringArray);
                }
                this.agpCptrCoreStringArray = 0;
            }
        }
    }

    static long getCptrAndSetMemOwn(CoreStringArray coreStringArray, boolean z) {
        if (coreStringArray != null) {
            synchronized (coreStringArray.delLock) {
                coreStringArray.isAgpCmemOwn = z;
            }
        }
        return getCptr(coreStringArray);
    }

    @Override // java.util.AbstractList, java.util.List
    public String remove(int i) {
        this.modCount++;
        return doRemove(i);
    }

    @Override // java.util.AbstractList, java.util.List
    public String get(int i) {
        return doGet(i);
    }

    public String set(int i, String str) {
        return doSet(i, str);
    }

    public boolean add(String str) {
        this.modCount++;
        doAdd(str);
        return true;
    }

    public void add(int i, String str) {
        this.modCount++;
        doAdd(i, str);
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

    /* access modifiers changed from: package-private */
    public long capacity() {
        return CoreJni.capacityInCoreStringArray(this.agpCptrCoreStringArray, this);
    }

    /* access modifiers changed from: package-private */
    public void reserve(long j) {
        CoreJni.reserveInCoreStringArray(this.agpCptrCoreStringArray, this, j);
    }

    @Override // java.util.AbstractCollection, java.util.List, java.util.Collection
    public boolean isEmpty() {
        return CoreJni.isEmptyInCoreStringArray(this.agpCptrCoreStringArray, this);
    }

    @Override // java.util.AbstractList, java.util.AbstractCollection, java.util.List, java.util.Collection
    public void clear() {
        CoreJni.clearInCoreStringArray(this.agpCptrCoreStringArray, this);
    }

    private int doSize() {
        return CoreJni.doSizeInCoreStringArray(this.agpCptrCoreStringArray, this);
    }

    private void doAdd(String str) {
        CoreJni.doAddInCoreStringArray0(this.agpCptrCoreStringArray, this, str);
    }

    private void doAdd(int i, String str) {
        CoreJni.doAddInCoreStringArray1(this.agpCptrCoreStringArray, this, i, str);
    }

    private String doRemove(int i) {
        return CoreJni.doRemoveInCoreStringArray(this.agpCptrCoreStringArray, this, i);
    }

    private String doGet(int i) {
        return CoreJni.doGetInCoreStringArray(this.agpCptrCoreStringArray, this, i);
    }

    private String doSet(int i, String str) {
        return CoreJni.doSetInCoreStringArray(this.agpCptrCoreStringArray, this, i, str);
    }

    private void doRemoveRange(int i, int i2) {
        CoreJni.doRemoveRangeInCoreStringArray(this.agpCptrCoreStringArray, this, i, i2);
    }
}
