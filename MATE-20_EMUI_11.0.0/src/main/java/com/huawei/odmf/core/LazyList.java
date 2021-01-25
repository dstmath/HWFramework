package com.huawei.odmf.core;

import android.support.annotation.NonNull;
import com.huawei.odmf.exception.ODMFConcurrentModificationException;
import com.huawei.odmf.exception.ODMFIllegalArgumentException;
import com.huawei.odmf.exception.ODMFIllegalStateException;
import com.huawei.odmf.exception.ODMFNullPointerException;
import com.huawei.odmf.exception.ODMFUnsupportedOperationException;
import com.huawei.odmf.user.api.ObjectContext;
import com.huawei.odmf.utils.JudgeUtils;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

public class LazyList<E> implements List<E> {
    private static final int arraySize = 500;
    private ManagedObject baseObj;
    private String entityName;
    private List<E> lazyInsertList;
    private List<E> lazyRemoveList;
    private int listSize;
    private transient int modCount;
    private AObjectContext objectContext;
    private List<ObjectId> objectIdList;
    private int relationshipIndex;

    public LazyList(List<ObjectId> list, ObjectContext objectContext2, String str) {
        this(list, objectContext2, str, null);
    }

    public LazyList(List<ObjectId> list, ObjectContext objectContext2, String str, ManagedObject managedObject) {
        this.objectIdList = null;
        this.objectContext = null;
        this.listSize = 0;
        this.modCount = 0;
        this.baseObj = null;
        this.relationshipIndex = -1;
        if (objectContext2 == null || list == null || str == null) {
            throw new ODMFIllegalArgumentException("When new LazyList, at least one parameter is null");
        }
        this.objectIdList = list;
        this.objectContext = (AObjectContext) objectContext2;
        this.entityName = str;
        this.listSize = list.size();
        if (managedObject != null) {
            this.baseObj = managedObject;
            this.lazyInsertList = new ArrayList();
            this.lazyRemoveList = new ArrayList();
        }
    }

    public ManagedObject getBaseObj() {
        return this.baseObj;
    }

    public int getRelationshipIndex() {
        return this.relationshipIndex;
    }

    public void setRelationshipIndex(int i) {
        this.relationshipIndex = i;
    }

    @Override // java.util.List
    public E get(int i) {
        if (i >= 0 && i < this.listSize) {
            return (E) getObject(this.objectIdList.get(i));
        }
        throw new IndexOutOfBoundsException("index < 0 || index >= listSize");
    }

    @Override // java.util.List, java.util.Collection
    public int size() {
        return this.listSize;
    }

    @Override // java.util.List, java.util.Collection
    public boolean isEmpty() {
        return this.listSize == 0;
    }

    @Override // java.util.List, java.util.Collection
    public boolean add(E e) {
        ManagedObject managedObject;
        addObj(this.listSize, e);
        addUpdate();
        insertAdd((LazyList<E>) e);
        if (getRelationshipIndex() < 0 || (managedObject = this.baseObj) == null) {
            return true;
        }
        ((AManagedObject) managedObject).setRelationshipUpdateSignsTrue(getRelationshipIndex());
        return true;
    }

    @Override // java.util.List
    public void add(int i, E e) {
        ManagedObject managedObject;
        if (i < 0 || i > this.listSize) {
            throw new IndexOutOfBoundsException("index < 0 || index > listSize()");
        }
        addObj(i, e);
        addUpdate();
        insertAdd((LazyList<E>) e);
        if (getRelationshipIndex() >= 0 && (managedObject = this.baseObj) != null) {
            ((AManagedObject) managedObject).setRelationshipUpdateSignsTrue(getRelationshipIndex());
        }
    }

    public void addObj(int i, E e) {
        if (i < 0 || i > this.listSize) {
            throw new IndexOutOfBoundsException("index < 0 || index > listSize()");
        }
        checkValue(e);
        this.objectIdList.add(i, e.getObjectId());
        this.modCount++;
        this.listSize++;
    }

    /* JADX DEBUG: Multi-variable search result rejected for r0v0, resolved type: java.util.ArrayList */
    /* JADX DEBUG: Multi-variable search result rejected for r1v0, resolved type: java.util.ArrayList */
    /* JADX WARN: Multi-variable type inference failed */
    @Override // java.util.List, java.util.Collection
    public boolean addAll(@NonNull Collection<? extends E> collection) {
        JudgeUtils.checkNull(collection);
        ArrayList arrayList = new ArrayList();
        List<E> arrayList2 = new ArrayList<>();
        for (Object obj : collection) {
            checkValue(obj);
            arrayList.add(((ManagedObject) obj).getObjectId());
            arrayList2.add(obj);
        }
        addObjAll(this.listSize, arrayList);
        addUpdate();
        insertAdd((List) arrayList2);
        return true;
    }

    /* JADX DEBUG: Multi-variable search result rejected for r0v2, resolved type: java.util.ArrayList */
    /* JADX DEBUG: Multi-variable search result rejected for r1v1, resolved type: java.util.ArrayList */
    /* JADX WARN: Multi-variable type inference failed */
    @Override // java.util.List
    public boolean addAll(int i, @NonNull Collection<? extends E> collection) {
        JudgeUtils.checkNull(collection);
        if (i < 0 || i > this.listSize) {
            throw new IndexOutOfBoundsException("Invalid index " + i + ", listSize is " + this.listSize);
        }
        ArrayList arrayList = new ArrayList();
        List<E> arrayList2 = new ArrayList<>();
        for (Object obj : collection) {
            checkValue(obj);
            arrayList.add(((ManagedObject) obj).getObjectId());
            arrayList2.add(obj);
        }
        addObjAll(i, arrayList);
        addUpdate();
        insertAdd((List) arrayList2);
        return true;
    }

    private void addObjAll(int i, Collection<ObjectId> collection) {
        this.objectIdList.addAll(i, collection);
        this.listSize += collection.size();
        this.modCount++;
    }

    /* JADX DEBUG: Multi-variable search result rejected for r3v0, resolved type: java.lang.Object */
    /* JADX WARN: Multi-variable type inference failed */
    @Override // java.util.List, java.util.Collection
    public boolean remove(Object obj) {
        ManagedObject managedObject;
        checkValue(obj);
        if (!this.objectIdList.remove(((ManagedObject) obj).getObjectId())) {
            return false;
        }
        this.listSize--;
        this.modCount++;
        addUpdate();
        insertRemove(obj);
        if (getRelationshipIndex() >= 0 && (managedObject = this.baseObj) != null) {
            ((AManagedObject) managedObject).setRelationshipUpdateSignsTrue(getRelationshipIndex());
        }
        return true;
    }

    @Override // java.util.List
    public E remove(int i) {
        ManagedObject managedObject;
        if (i < 0 || i >= this.listSize) {
            throw new IndexOutOfBoundsException();
        }
        this.listSize--;
        this.modCount++;
        addUpdate();
        E e = (E) getObject(this.objectIdList.remove(i));
        insertRemove(e);
        if (getRelationshipIndex() >= 0 && (managedObject = this.baseObj) != null) {
            ((AManagedObject) managedObject).setRelationshipUpdateSignsTrue(getRelationshipIndex());
        }
        return e;
    }

    /* JADX DEBUG: Multi-variable search result rejected for r5v0, resolved type: com.huawei.odmf.core.LazyList<E> */
    /* JADX WARN: Multi-variable type inference failed */
    @Override // java.util.List, java.util.Collection
    public boolean removeAll(@NonNull Collection<?> collection) {
        JudgeUtils.checkNull(collection);
        boolean z = false;
        for (Object obj : collection) {
            try {
                checkValue(obj);
                if (this.objectIdList.remove(((ManagedObject) obj).getObjectId())) {
                    this.listSize--;
                    this.modCount++;
                    if (!z) {
                        z = true;
                    }
                    insertRemove(obj);
                }
            } catch (ODMFIllegalArgumentException | ODMFNullPointerException unused) {
            }
        }
        if (z) {
            addUpdate();
        }
        return true;
    }

    @Override // java.util.List, java.util.Collection
    public boolean contains(Object obj) {
        JudgeUtils.checkInstance(obj);
        return this.objectIdList.contains(((ManagedObject) obj).getObjectId());
    }

    @Override // java.util.List, java.util.Collection
    public boolean containsAll(@NonNull Collection<?> collection) {
        JudgeUtils.checkNull(collection);
        Iterator<?> it = collection.iterator();
        while (it.hasNext()) {
            if (!contains(it.next())) {
                return false;
            }
        }
        return true;
    }

    /* JADX DEBUG: Multi-variable search result rejected for r5v0, resolved type: com.huawei.odmf.core.LazyList<E> */
    /* JADX DEBUG: Multi-variable search result rejected for r0v0, resolved type: java.util.ArrayList */
    /* JADX WARN: Multi-variable type inference failed */
    @Override // java.util.List, java.util.Collection
    public boolean retainAll(@NonNull Collection<?> collection) {
        JudgeUtils.checkNull(collection);
        ArrayList arrayList = new ArrayList();
        for (ObjectId objectId : this.objectIdList) {
            ManagedObject object = getObject(objectId);
            if (!collection.contains(object)) {
                arrayList.add(objectId);
                insertRemove(object);
            }
        }
        if (arrayList.isEmpty()) {
            return false;
        }
        this.objectIdList.removeAll(arrayList);
        this.modCount += arrayList.size();
        this.listSize -= arrayList.size();
        addUpdate();
        return true;
    }

    /* JADX DEBUG: Multi-variable search result rejected for r3v0, resolved type: com.huawei.odmf.core.LazyList<E> */
    /* JADX WARN: Multi-variable type inference failed */
    @Override // java.util.List, java.util.Collection
    public void clear() {
        for (int i = 0; i < this.listSize; i++) {
            insertRemove(getObject(this.objectIdList.get(i)));
        }
        this.objectIdList.clear();
        this.modCount++;
        this.listSize = 0;
        addUpdate();
    }

    @Override // java.util.List
    public E set(int i, E e) {
        if (i < 0 || i >= this.listSize) {
            throw new IndexOutOfBoundsException("index < 0 || index >= listSize()");
        }
        checkValue(e);
        addUpdate();
        E e2 = (E) getObject(this.objectIdList.set(i, e.getObjectId()));
        insertRemove(e2);
        insertAdd((LazyList<E>) e);
        return e2;
    }

    /* JADX DEBUG: Multi-variable search result rejected for r0v1, resolved type: java.util.ArrayList */
    /* JADX WARN: Multi-variable type inference failed */
    @Override // java.util.List
    @NonNull
    public List<E> subList(int i, int i2) {
        if (i < 0 || i > i2 || i2 > this.listSize) {
            throw new IndexOutOfBoundsException("Invalid index ");
        }
        ArrayList arrayList = new ArrayList();
        while (i < i2) {
            arrayList.add(getObject(this.objectIdList.get(i)));
            i++;
        }
        return arrayList;
    }

    @Override // java.util.List
    public int indexOf(Object obj) {
        checkValue(obj);
        return this.objectIdList.indexOf(((ManagedObject) obj).getObjectId());
    }

    @Override // java.util.List
    public int lastIndexOf(Object obj) {
        checkValue(obj);
        return this.objectIdList.lastIndexOf(((ManagedObject) obj).getObjectId());
    }

    @Override // java.util.List
    @NonNull
    public ListIterator<E> listIterator() {
        if (this.objectIdList != null) {
            return new LazyListIterator(0);
        }
        throw new ODMFIllegalStateException("objectIdList has not been initialized.");
    }

    @Override // java.util.List
    @NonNull
    public ListIterator<E> listIterator(int i) {
        if (this.objectIdList != null) {
            return new LazyListIterator(i);
        }
        throw new ODMFIllegalStateException("objectIdList has not been initialized.");
    }

    @Override // java.util.List, java.util.Collection, java.lang.Iterable
    @NonNull
    public Iterator<E> iterator() {
        if (this.objectIdList != null) {
            return new LazyIterator();
        }
        throw new ODMFIllegalStateException("objectIdList has not been initialized.");
    }

    /* JADX DEBUG: Multi-variable search result rejected for r0v2, resolved type: java.util.ArrayList */
    /* JADX WARN: Multi-variable type inference failed */
    @Override // java.util.List, java.util.Collection
    @NonNull
    public Object[] toArray() {
        if (this.listSize <= arraySize) {
            ArrayList arrayList = new ArrayList();
            int size = this.objectIdList.size();
            for (int i = 0; i < size; i++) {
                arrayList.add(getObject(this.objectIdList.get(i)));
            }
            return arrayList.toArray();
        }
        throw new ODMFUnsupportedOperationException("This operation is not supported, returning a lots of objects will result in out of memory");
    }

    /* JADX DEBUG: Multi-variable search result rejected for r4v0, resolved type: T[] */
    /* JADX DEBUG: Multi-variable search result rejected for r4v6, resolved type: T[] */
    /* JADX WARN: Multi-variable type inference failed */
    @Override // java.util.List, java.util.Collection
    @NonNull
    public <T> T[] toArray(T[] tArr) {
        int length = tArr.length;
        int i = this.listSize;
        int i2 = 0;
        if (length >= i) {
            while (i2 < this.listSize) {
                tArr[i2] = getObject(this.objectIdList.get(i2));
                i2++;
            }
            return tArr;
        } else if (i <= arraySize) {
            T[] tArr2 = (T[]) ((Object[]) Array.newInstance(tArr.getClass().getComponentType(), this.listSize));
            while (i2 < this.listSize) {
                tArr2[i2] = getObject(this.objectIdList.get(i2));
                i2++;
            }
            return tArr2;
        } else {
            throw new ODMFUnsupportedOperationException("This operation is not supported, returning a lots of objects will result in out of memory");
        }
    }

    private class LazyIterator implements Iterator<E> {
        int cursor;
        int expectedModCount;
        int lazyLastRet;
        int lazyLimit;

        private LazyIterator() {
            this.lazyLimit = LazyList.this.listSize;
            this.expectedModCount = LazyList.this.modCount;
            this.lazyLastRet = -1;
            this.cursor = 0;
        }

        @Override // java.util.Iterator
        public boolean hasNext() {
            return this.cursor < this.lazyLimit;
        }

        @Override // java.util.Iterator
        public E next() {
            if (LazyList.this.modCount == this.expectedModCount) {
                int i = this.cursor;
                if (i < this.lazyLimit) {
                    this.cursor = i + 1;
                    this.lazyLastRet = i;
                    return (E) LazyList.this.get(this.lazyLastRet);
                }
                throw new NoSuchElementException();
            }
            throw new ODMFConcurrentModificationException();
        }

        @Override // java.util.Iterator
        public void remove() {
            if (this.lazyLastRet < 0) {
                throw new ODMFIllegalStateException();
            } else if (LazyList.this.modCount == this.expectedModCount) {
                try {
                    LazyList.this.remove(this.lazyLastRet);
                    this.cursor = this.lazyLastRet;
                    this.lazyLastRet = -1;
                    this.expectedModCount = LazyList.this.modCount;
                    this.lazyLimit--;
                } catch (IndexOutOfBoundsException unused) {
                    throw new ODMFConcurrentModificationException();
                }
            } else {
                throw new ODMFConcurrentModificationException();
            }
        }
    }

    /* access modifiers changed from: private */
    public class LazyListIterator extends LazyList<E>.LazyIterator implements ListIterator<E> {
        LazyListIterator(int i) {
            super();
            if (i < 0 || i > this.lazyLimit) {
                throw new IndexOutOfBoundsException();
            }
            this.cursor = i;
        }

        @Override // java.util.ListIterator
        public boolean hasPrevious() {
            return this.cursor != 0;
        }

        @Override // java.util.ListIterator
        public E previous() {
            if (LazyList.this.modCount == this.expectedModCount) {
                int i = this.cursor - 1;
                if (i >= 0) {
                    this.cursor = i;
                    this.lazyLastRet = i;
                    return (E) LazyList.this.get(this.lazyLastRet);
                }
                throw new NoSuchElementException();
            }
            throw new ODMFConcurrentModificationException();
        }

        @Override // java.util.ListIterator
        public int nextIndex() {
            return this.cursor;
        }

        @Override // java.util.ListIterator
        public int previousIndex() {
            return this.cursor - 1;
        }

        @Override // java.util.ListIterator
        public void set(E e) {
            if (this.lazyLastRet < 0) {
                throw new ODMFIllegalStateException();
            } else if (LazyList.this.modCount == this.expectedModCount) {
                try {
                    LazyList.this.set(this.lazyLastRet, e);
                } catch (IndexOutOfBoundsException unused) {
                    throw new ODMFConcurrentModificationException();
                }
            } else {
                throw new ODMFConcurrentModificationException();
            }
        }

        @Override // java.util.ListIterator
        public void add(E e) {
            if (LazyList.this.modCount == this.expectedModCount) {
                try {
                    int i = this.cursor;
                    LazyList.this.add(i, e);
                    this.cursor = i + 1;
                    this.lazyLastRet = -1;
                    this.expectedModCount = LazyList.this.modCount;
                    this.lazyLimit++;
                } catch (IndexOutOfBoundsException unused) {
                    throw new ODMFConcurrentModificationException();
                }
            } else {
                throw new ODMFConcurrentModificationException();
            }
        }
    }

    private ManagedObject getObject(ObjectId objectId) {
        if (objectId == null) {
            return null;
        }
        return this.objectContext.get(objectId);
    }

    private void checkValue(Object obj) {
        JudgeUtils.checkInstance(obj);
        checkEntity((ManagedObject) obj);
    }

    private void checkEntity(ManagedObject managedObject) {
        if (!this.entityName.equals(managedObject.getEntityName())) {
            throw new ODMFIllegalArgumentException(JudgeUtils.INCOMPATIBLE_OBJECTS_NOT_ALLOWED_MESSAGE);
        }
    }

    private void addUpdate() {
        ManagedObject managedObject = this.baseObj;
        if (managedObject != null) {
            this.objectContext.update(managedObject);
        }
    }

    public boolean clearModify() {
        if (this.baseObj == null) {
            return false;
        }
        this.lazyInsertList.clear();
        this.lazyRemoveList.clear();
        return true;
    }

    private void insertAdd(E e) {
        List<E> list = this.lazyInsertList;
        if (list != null && !JudgeUtils.isContainedObject(list, e)) {
            this.lazyInsertList.add(e);
        }
    }

    private void insertAdd(List<E> list) {
        if (this.lazyInsertList != null) {
            int size = list.size();
            for (int i = 0; i < size; i++) {
                if (!JudgeUtils.isContainedObject(this.lazyInsertList, list.get(i))) {
                    this.lazyInsertList.add(list.get(i));
                }
            }
        }
    }

    private void insertRemove(E e) {
        List<E> list = this.lazyRemoveList;
        if (list != null && !JudgeUtils.isContainedObject(list, e)) {
            this.lazyRemoveList.add(e);
        }
    }

    public List<E> getInsertList() {
        return this.lazyInsertList;
    }

    public List<E> getRemoveList() {
        return this.lazyRemoveList;
    }
}
