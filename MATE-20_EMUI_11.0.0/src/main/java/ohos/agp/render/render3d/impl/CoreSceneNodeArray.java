package ohos.agp.render.render3d.impl;

import java.util.AbstractList;
import java.util.RandomAccess;

/* access modifiers changed from: package-private */
public class CoreSceneNodeArray extends AbstractList<CoreSceneNode> implements RandomAccess {
    private transient long agpCptrCoreSceneNode;
    private final Object delLock;
    transient boolean isAgpCmemOwn;

    CoreSceneNodeArray(long j, boolean z) {
        this.delLock = new Object();
        this.isAgpCmemOwn = z;
        this.agpCptrCoreSceneNode = j;
    }

    static long getCptr(CoreSceneNodeArray coreSceneNodeArray) {
        if (coreSceneNodeArray == null) {
            return 0;
        }
        return coreSceneNodeArray.agpCptrCoreSceneNode;
    }

    /* access modifiers changed from: protected */
    @Override // java.lang.Object
    public void finalize() {
        delete();
    }

    /* access modifiers changed from: package-private */
    public void delete() {
        synchronized (this.delLock) {
            if (this.agpCptrCoreSceneNode != 0) {
                if (this.isAgpCmemOwn) {
                    this.isAgpCmemOwn = false;
                    CoreJni.deleteCoreSceneNodeArray(this.agpCptrCoreSceneNode);
                }
                this.agpCptrCoreSceneNode = 0;
            }
        }
    }

    static long getCptrAndSetMemOwn(CoreSceneNodeArray coreSceneNodeArray, boolean z) {
        if (coreSceneNodeArray != null) {
            synchronized (coreSceneNodeArray.delLock) {
                coreSceneNodeArray.isAgpCmemOwn = z;
            }
        }
        return getCptr(coreSceneNodeArray);
    }

    CoreSceneNodeArray(CoreSceneNode[] coreSceneNodeArr) {
        this();
        reserve((long) coreSceneNodeArr.length);
        for (CoreSceneNode coreSceneNode : coreSceneNodeArr) {
            add(coreSceneNode);
        }
    }

    CoreSceneNodeArray(Iterable<CoreSceneNode> iterable) {
        this();
        for (CoreSceneNode coreSceneNode : iterable) {
            add(coreSceneNode);
        }
    }

    @Override // java.util.AbstractList, java.util.List
    public CoreSceneNode get(int i) {
        return doGet(i);
    }

    public CoreSceneNode set(int i, CoreSceneNode coreSceneNode) {
        return doSet(i, coreSceneNode);
    }

    public boolean add(CoreSceneNode coreSceneNode) {
        this.modCount++;
        doAdd(coreSceneNode);
        return true;
    }

    public void add(int i, CoreSceneNode coreSceneNode) {
        this.modCount++;
        doAdd(i, coreSceneNode);
    }

    @Override // java.util.AbstractList, java.util.List
    public CoreSceneNode remove(int i) {
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

    CoreSceneNodeArray() {
        this(CoreJni.newCoreSceneNodeArray0(), true);
    }

    CoreSceneNodeArray(CoreSceneNodeArray coreSceneNodeArray) {
        this(CoreJni.newCoreSceneNodeArray1(getCptr(coreSceneNodeArray), coreSceneNodeArray), true);
    }

    /* access modifiers changed from: package-private */
    public long capacity() {
        return CoreJni.capacityInCoreSceneNodeArray(this.agpCptrCoreSceneNode, this);
    }

    /* access modifiers changed from: package-private */
    public void reserve(long j) {
        CoreJni.reserveInCoreSceneNodeArray(this.agpCptrCoreSceneNode, this, j);
    }

    @Override // java.util.AbstractCollection, java.util.List, java.util.Collection
    public boolean isEmpty() {
        return CoreJni.isEmptyInCoreSceneNodeArray(this.agpCptrCoreSceneNode, this);
    }

    @Override // java.util.AbstractList, java.util.AbstractCollection, java.util.List, java.util.Collection
    public void clear() {
        CoreJni.clearInCoreSceneNodeArray(this.agpCptrCoreSceneNode, this);
    }

    CoreSceneNodeArray(int i, CoreSceneNode coreSceneNode) {
        this(CoreJni.newCoreSceneNodeArray2(i, CoreSceneNode.getCptr(coreSceneNode), coreSceneNode), true);
    }

    private int doSize() {
        return CoreJni.doSizeInCoreSceneNodeArray(this.agpCptrCoreSceneNode, this);
    }

    private void doAdd(CoreSceneNode coreSceneNode) {
        CoreJni.doAddInCoreSceneNodeArray0(this.agpCptrCoreSceneNode, this, CoreSceneNode.getCptr(coreSceneNode), coreSceneNode);
    }

    private void doAdd(int i, CoreSceneNode coreSceneNode) {
        CoreJni.doAddInCoreSceneNodeArray1(this.agpCptrCoreSceneNode, this, i, CoreSceneNode.getCptr(coreSceneNode), coreSceneNode);
    }

    private CoreSceneNode doRemove(int i) {
        long doRemoveInCoreSceneNodeArray = CoreJni.doRemoveInCoreSceneNodeArray(this.agpCptrCoreSceneNode, this, i);
        if (doRemoveInCoreSceneNodeArray == 0) {
            return null;
        }
        return new CoreSceneNode(doRemoveInCoreSceneNodeArray, false);
    }

    private CoreSceneNode doGet(int i) {
        long doGetInCoreSceneNodeArray = CoreJni.doGetInCoreSceneNodeArray(this.agpCptrCoreSceneNode, this, i);
        if (doGetInCoreSceneNodeArray == 0) {
            return null;
        }
        return new CoreSceneNode(doGetInCoreSceneNodeArray, false);
    }

    private CoreSceneNode doSet(int i, CoreSceneNode coreSceneNode) {
        long doSetInCoreSceneNodeArray = CoreJni.doSetInCoreSceneNodeArray(this.agpCptrCoreSceneNode, this, i, CoreSceneNode.getCptr(coreSceneNode), coreSceneNode);
        if (doSetInCoreSceneNodeArray == 0) {
            return null;
        }
        return new CoreSceneNode(doSetInCoreSceneNodeArray, false);
    }

    private void doRemoveRange(int i, int i2) {
        CoreJni.doRemoveRangeInCoreSceneNodeArray(this.agpCptrCoreSceneNode, this, i, i2);
    }
}
